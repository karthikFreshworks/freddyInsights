package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(AbstractAIBaseApiHelper.API_V1)
public abstract class AbstractAIBaseApiHelper {
    public static final String API_V1 = "/v1";
    public static final String API_V2 = "/v2";

    @Autowired
    protected AIRequestContextHelper aiRequestContext;

    protected void validatePathParam(String input) {
        if (StringUtils.isEmpty(input.trim())) {
            throw new AIResponseStatusException(ExceptionConstant.NOT_VALID_PATH_PARAM,
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.NOT_ACCEPTABLE);
        }
    }
}
