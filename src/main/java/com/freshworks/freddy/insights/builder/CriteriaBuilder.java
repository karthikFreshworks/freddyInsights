package com.freshworks.freddy.insights.builder;

import com.freshworks.freddy.insights.constant.DocumentDBConstant;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Getter
public class CriteriaBuilder extends AbstractAIBaseHelper {
    private final Criteria criteria;

    private CriteriaBuilder(Builder builder) {
        this.criteria = builder.criteria;
    }

    public static class Builder {
        private final Criteria criteria;

        public Builder(List<TenantEnum> tenants) {
            criteria = Criteria.where(DocumentDBConstant.TENANT);
            criteria.in(tenants);
        }

        public Builder(String bundleName) {
            criteria = Criteria.where(DocumentDBConstant.BUNDLE);
            criteria.is(bundleName);
        }

        public Builder id(String id) {
            if (StringUtils.isNotEmpty(id)) {
                criteria.and(DocumentDBConstant.ID).is(id);
            }
            return this;
        }

        public Builder account(String accountId) {
            if (StringUtils.isNotEmpty(accountId)) {
                criteria.and(DocumentDBConstant.ACCOUNT_ID).is(accountId);
            }
            return this;
        }

        public Builder group(String group) {
            if (StringUtils.isNotEmpty(group)) {
                criteria.and(DocumentDBConstant.GROUP).is(group);
            }
            return this;
        }

        public Builder name(String name) {
            if (StringUtils.isNotEmpty(name)) {
                criteria.and(DocumentDBConstant.NAME).is(name);
            }
            return this;
        }

        public Builder version(String version) {
            if (StringUtils.isNotEmpty(version)) {
                criteria.and(DocumentDBConstant.VERSION).is(version);
            }
            return this;
        }

        public Builder range(String startTime, String endTime) {
            var dateFormat = new SimpleDateFormat(DocumentDBConstant.DATE_FORMAT);

            try {
                if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
                    Date start = dateFormat.parse(startTime);
                    Date end = dateFormat.parse(endTime);
                    criteria.and(DocumentDBConstant.CREATED_AT).gte(start).lte(end);
                } else if (StringUtils.isNotEmpty(startTime)) {
                    Date start = dateFormat.parse(startTime);
                    criteria.and(DocumentDBConstant.CREATED_AT).gte(start);
                } else if (StringUtils.isNotEmpty(endTime)) {
                    Date end = dateFormat.parse(endTime);
                    criteria.and(DocumentDBConstant.CREATED_AT).lte(end);
                }
            } catch (ParseException e) {
                var msg = String.format(" Provide date range in format: %s", DocumentDBConstant.DATE_FORMAT);
                throw new AIResponseStatusException(ExceptionConstant.NOT_VALID_DATE_FORMAT + msg,
                        HttpStatus.BAD_REQUEST, ErrorCode.NOT_ACCEPTABLE);
            }
            return this;
        }

        public Builder prompt(String promptId) {
            if (StringUtils.isNotEmpty(promptId)) {
                criteria.and(DocumentDBConstant.PROMPT_ID).is(promptId);
            }
            return this;
        }

        public Builder languageCode(LanguageCodeEnum languageCode) {
            if (languageCode != null) {
                criteria.and(DocumentDBConstant.LANGUAGE_CODE).is(languageCode);
            }
            return this;
        }

        public Builder isNotTrueOrNotPresentFallback() {
            criteria.orOperator(
                    Criteria.where(DocumentDBConstant.IS_FALLBACK_HANDLER).ne(true),
                    Criteria.where(DocumentDBConstant.IS_FALLBACK_HANDLER).exists(false));
            return this;
        }

        public Builder isNotIn(String key, String value) {
            criteria.orOperator(
                    Criteria.where(key).ne(value));
            return this;
        }

        public CriteriaBuilder build() {
            return new CriteriaBuilder(this);
        }
    }
}
