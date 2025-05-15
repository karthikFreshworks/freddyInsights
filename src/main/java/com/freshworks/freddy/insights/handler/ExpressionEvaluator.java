package com.freshworks.freddy.insights.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.codec.digest.DigestUtils;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
public class ExpressionEvaluator {
    private final Cache<String, Serializable> cache;

    public ExpressionEvaluator(@Value("${expression.cache.size}") int maxSize) {
        cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .build();
    }

    public <T> T evaluate(String expression, Map<?, ?> context, Class<T> tClass) {
        ParserContext parserContext = new ParserContext();
        Serializable compiledExpression = cache.get(getCacheKey(expression),
                (k) -> MVEL.compileExpression(expression, parserContext));
        return MVEL.executeExpression(compiledExpression, context, tClass);
    }

    static String getCacheKey(String expression) {
        return DigestUtils.sha256Hex(expression.getBytes());
    }
}
