package com.freshworks.freddy.insights.helper;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FileHelper {
    /**
     * Reads the content of a file located at the specified path and returns it as a String.
     * The path is relative to the package of the AICommonHelper class.
     *
     * @param path the relative path to the file to be read
     * @return the content of the file as a String
     * @throws IOException           if an I/O error occurs while reading the file
     * @throws FileNotFoundException if the file specified by the path is not found
     */
    public static String getFileContent(String path) throws IOException {
        try (InputStream inputStream = AICommonHelper.class.getResourceAsStream(File.separator + path)) {
            if (inputStream == null) {
                throw new FileNotFoundException(
                        String.format(
                                "File not found: %s. Make sure the file exists in the specified location.", path));
            }

            ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                resultStream.write(buffer, 0, length);
            }
            log.info(String.format("Loaded file content from: %s", path));
            return resultStream.toString(StandardCharsets.UTF_8);
        }
    }
}
