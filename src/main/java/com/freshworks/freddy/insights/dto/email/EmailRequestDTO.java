package com.freshworks.freddy.insights.dto.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmailRequestDTO {
    @JsonProperty(value = "from")
    private ObjectNode emailFrom;
    @JsonProperty(value = "to")
    private List<ObjectNode> emailTOList;
    @JsonProperty(value = "subject")
    private String subject;
    @JsonProperty(value = "text")
    private String emailBody;
    @JsonProperty(value = "html")
    private String emailBodyHtml;
    @JsonProperty(value = "headers")
    private ObjectNode headers;
    private String accountId;

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
