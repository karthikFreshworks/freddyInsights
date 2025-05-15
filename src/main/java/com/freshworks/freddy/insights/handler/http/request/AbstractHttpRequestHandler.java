package com.freshworks.freddy.insights.handler.http.request;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.io.Closer;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractHttpRequestHandler<A, R> implements RequestHandler<A, R> {
    public HttpEntity buildApacheMultipartEntity(AIServiceMO aiServiceMO) {
        List<MultipartFile> files = aiServiceMO.getMultipartFiles();
        Map<String, Object> formData = aiServiceMO.getTemplatedRuleBodyMap();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        Optional.ofNullable(formData).ifPresent(map ->
                map.forEach((key, value) -> Optional.ofNullable(value)
                        .filter(v -> !String.valueOf(v).isEmpty())
                        .ifPresent(v -> builder.addTextBody(key, String.valueOf(v), ContentType.TEXT_PLAIN))));

        try {
            Optional.ofNullable(files).ifPresent(list ->
                    list.forEach(file -> Optional.ofNullable(file)
                            .filter(f -> !f.isEmpty())
                            .ifPresent(f -> {
                                String contentType = f.getContentType() != null
                                        ? f.getContentType() : "application/octet-stream";
                                try {
                                    builder.addBinaryBody("file", f.getBytes(), ContentType.parse(contentType),
                                            f.getOriginalFilename());
                                } catch (IOException e) {
                                    handleIOException(e);
                                }
                            })));
        } catch (Exception e) {
            handleException(e);
        }
        return builder.build();
    }

    private void handleIOException(IOException e) {
        String message = String.format("Error building multipart body: %s", e.getMessage());
        log.error(message + " CAUSE : ", ExceptionHelper.stackTrace(e));
        throw new AIResponseStatusException(
                message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private void handleException(Exception e) {
        String message = String.format("Error building multipart body: %s", e.getMessage());
        log.error(message + " CAUSE : ", ExceptionHelper.stackTrace(e));
        throw new AIResponseStatusException(
                message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    protected Header[] apacheHttpHeaders(String[] headers) {
        if (headers.length % 2 != 0) {
            return new Header[0];
        }
        Header[] apacheHeaders = new Header[headers.length / 2];

        for (int i = 0; i < headers.length; i += 2) {
            String key = headers[i].trim();
            String value = headers[i + 1].trim();
            apacheHeaders[i / 2] =
                    new BasicHeader(key, value);
        }
        return apacheHeaders;
    }

    protected HttpUriRequest createHttRequest(
            HttpUriRequest request, AIServiceMO aiServiceMO, HttpEntity entity) {
        if (entity != null) {
            request.setEntity(entity);
        }
        Arrays.stream(apacheHttpHeaders(aiServiceMO.getRemoteRequestHeaders())).forEach(request::setHeader);
        return request;
    }

    public class CustomHttpEntity extends AbstractHttpEntity {
        private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile(
                "(?:\r?\ncontent-type: text/plain; charset=ISO-8859-1|\\r?\\ncontent-length:\\s*[0-9]+)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        private final HttpEntity wrappedEntity;

        public CustomHttpEntity(HttpEntity wrappedEntity) {
            super(wrappedEntity.getContentType(), wrappedEntity.getContentEncoding(), false);
            this.wrappedEntity = wrappedEntity;
        }

        @Override
        public long getContentLength() {
            return -1;
        }

        @Override
        public void writeTo(OutputStream outstream) throws IOException {
            if (outstream == null) {
                throw new IllegalArgumentException("Output stream must not be null.");
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            wrappedEntity.writeTo(byteArrayOutputStream);

            String contentTypeString = this.getContentType();
            Charset charset = extractCharset(contentTypeString);

            String originalContent = byteArrayOutputStream.toString(charset);
            String cleanedContent = CONTENT_LENGTH_PATTERN.matcher(originalContent).replaceAll("");

            outstream.write(cleanedContent.getBytes(charset));
        }

        private Charset extractCharset(String contentType) {
            if (contentType == null || contentType.isEmpty()) {
                return StandardCharsets.ISO_8859_1;
            }

            Pattern charsetPattern = Pattern.compile("charset=([^;]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = charsetPattern.matcher(contentType);

            if (matcher.find()) {
                String charsetName = matcher.group(1).trim();
                return Charset.forName(charsetName);
            } else {
                return StandardCharsets.ISO_8859_1;
            }
        }

        @Override
        public boolean isRepeatable() {
            return wrappedEntity.isRepeatable();
        }

        @Override
        public InputStream getContent() throws IOException, UnsupportedOperationException {
            return wrappedEntity.getContent();
        }

        @Override
        public boolean isStreaming() {
            return wrappedEntity.isStreaming();
        }

        @Override
        public void close() {
            if (wrappedEntity != null) {
                Closer.closeQuietly(wrappedEntity);
            }
        }
    }
}
