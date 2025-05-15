package com.freshworks.freddy.insights.constant;

public class ExceptionConstant {
    private ExceptionConstant() {
        throw new IllegalStateException("ExceptionConstant class");
    }

    public static final String NOT_VALID_ID = "Not a valid identifier.";
    public static final String NOT_VALID_PLATFORM = "Not a valid platform.";
    public static final String NOT_VALID_MODEL = "Not a valid Model.";
    public static final String NOT_VALID_RULE = "Not a valid rule.";
    public static final String NOT_VALID_HEADER = "Not a valid header.";
    public static final String NOT_VALID_URL = "Not a valid url.";
    public static final String NOT_VALID_METHOD = "NOT a valid method.";
    public static final String NOT_VALID_SERVICE = "Not a valid service.";
    public static final String NOT_VALID_DESCRIPTION = "Not a valid description.";
    public static final String NOT_VALID_PARAMS = "Not a valid params.";
    public static final String NOT_VALID_TEMPLATE = "Not a valid template.";
    public static final String NOT_VALID_VERSION = "Not a valid version.";
    public static final String NOT_VALID_EMAIL = "Not a valid email.";
    public static final String NOT_VALID_TENANT = "Not a valid tenant.";
    public static final String NOT_VALID_TENANT_FORMATTER = "Not a valid tenant %s.";
    public static final String NOT_VALID_DATE = "Not a valid date requested.";
    public static final String NOT_VALID_STATUS = "Not a valid status requested.";
    public static final String NOT_VALID_MODEL_ID = "Not a valid model id.";
    public static final String NOT_VALID_PROMPT_ID = "Not a valid prompt id.";
    public static final String NOT_VALID_MIMETYPE = "Not a valid mime-type.";
    public static final String NOT_VALID_INSIGHT_ID = "Not a valid insight id.";
    public static final String NOT_VALID_BUNDLE_ID = "Not a valid bundle id.";
    public static final String UNIQUE_LANGUAGE_CODE = "It should have unique language codes";
    public static final String CONFLICT_PARENT_LANGUAGE_CODE = "Conflict with parent language code";
    public static final String CONFLICT_DIALOGUE_SOURCE_INTENT_HANDLER = "Record already exist for given value %s.";
    public static final String NO_RECORD_FOR_ID = "No record found for given identifier.";
    public static final String NO_SERVICE_EXIST = "Service doesn't exist : %s";
    public static final String TEMPLATES_KEY_IN_RULE_BODY = "Rule should not have template keys";
    public static final String NO_MODEL_EXIST = "Model doesn't exist";
    public static final String FORBIDDEN = "Not Acceptable.";
    public static final String HTTP_CLIENT_ERROR = "Http client exception : %s .";
    public static final String ARCHIVED_RECORD = "Record is archived for given identifier";
    public static final String DUPLICATE_RECORD_FOR_ID = "Record already exist for given identifier.";
    public static final String PARSING_ERROR = "Unable to parse the request payload.";
    public static final String ERROR_IN_INDEXING = "Error in indexing document.";
    public static final String ERROR_IN_PARSING_QUERY_HASH = "Error in parsing the queryHash.";
    public static final String NOT_VALID_NAME = "Invalid name %s .";
    public static final String NOT_VALID_TEMPLATE_KEY = "Invalid template Key";
    public static final String NOT_VALID_FEATURES = "Features can not be null.";
    public static final String NOT_VALID_BODY = "Rule body can not be null.";
    public static final String NOT_VALID_RESPONSE_PARSER = "Invalid response parser.";
    public static final String NOT_VALID_PARAM_NAME = "Params name can not be null or empty.";
    public static final String NOT_VALID_PARAM_DATA_TYPE = "Params data type can not be null or empty.";
    public static final String NOT_VALID_PATH_PARAM = "Path params data can not be null or empty.";
    public static final String SUPER_ADMIN_ONLY_ACCESS_FOR_FALLBACK_INTENT_HANDLER
            = "only super admin can access fallback handler";
    public static final String SUPER_ADMIN_ONLY_ACCESS_FOR_COPILOT_INTENT_HANDLER
            = "only super admin can access copilot intent handler";
    public static final String REQUIRES_DIALOGUE_SOURCE_WHEN_ADDING_DIALOGUE_SOURCE_PAGES
            = "dialogue source can't be null when dialogue source page is provided";
    public static final String NOT_VALID_MODEL_MAPPING_KEY = "Model mapping key can not be null or empty.";
    public static final String NOT_VALID_MODEL_MAPPING_VALUE = "Model mapping value can not be null or empty.";
    public static final String NOT_VALID_MODEL_MAPPING_REQUEST_TYPE
            = "Model mapping request type should be among body or header.";
    public static final String NOT_VALID_LIST_SIZE = "The list size exceeds the maximum allowed limit of 50.";
    public static final String NOT_VALID_PROMOTE_REGION_KEY = "Region can not be null.";
    public static final String NOT_VALID_PROMOTE_REGION_AUTH = "Auth key can not be null or empty.";
    public static final String NOT_VALID_INTENT_KIND = "Intent kind can not be null. Accepted value: JAR/SERVICE";
    public static final String NOT_VALID_INTENT_RULE_METHOD = "Not a valid method under rule. Accepted value: get/post";
    public static final String NOT_VALID_INTENT_RULE = "For Kind: SERVICE, rule attribute is must";
    public static final String NOT_VALID_TEXT = "Text can not be null or empty";
    public static final String DESCRIPTION_CONSTRAINT_VIOLATION = "Description modification not allowed";
    public static final String NOT_VALID_INSIGHT_NAME = "Not a valid insight name.";
    public static final String NOT_VALID_USER_ID = "Not a valid user id.";
    public static final String NOT_VALID_INSIGHT_PROMPTS = "Not a valid insight prompts.";
    public static final String NOT_ACTIVE_INSIGHT = "Not an active insight.";
    public static final String NOT_VALID_PROMPT_NAME = "Not a valid prompt name.";
    public static final String NOT_VALID_DATE_FORMAT
            = "Not a valid date format.";
    public static final String NOT_VALID_LLM_ROLE
            = "Not a valid LLM role. It should be among user, assistant or system.";
    public static final String NOT_VALID_LLM_CONTENT = "Not a valid LLM content.";
    public static final String NOT_VALID_RESPONSE_CODE = "Not a valid response code.";
    public static final String NOT_VALID_ACCEPT_LANGUAGE
            = "Not a valid language code : %s, It should be in one the following values %s, "
            + "Please reach out to the freddy ai platform team on this if team needs to support this language code";
    public static final String INVALID_PARAM = "Invalid parameter";
    public static final String NOT_VALID_INTENT_HANDLER_ID = "not a valid intent handler id";
    public static final String EXTERNAL_SERVICE_API_ERROR = "AI-Platform encountered an external service API failure";
    public static final String NOT_VALID_TEXT_IN_TRANSLATED_TEXT = "Text can not be null or empty in translated texts";
    public static final String NOT_VALID_TITLE_IN_TRANSLATED_TEXT
            = "Title can not be null or empty in translated texts";
    public static final String LANGUAGE_CODES_LIMIT_EXCEEDED
            = "Accept language should not contain more than 3 languageCode";
    public static final String NOT_VALID_TEMPLATE_KEYS
            = "The template keys are invalid as they are not present in service created.";
    public static final String NOT_VALID_RUN_SERVICE_VERSION
            = "The provided run service version is not valid; it must be either V1 or "
            + "V2. Please contact the platform developers for assistance";
    public static final String NOT_VALID_FEEDBACK_FEATURE = "Feature can not be null or empty";
    public static final String NOT_VALID_FEEDBACK = "Feedback should be either helpful or unhelpful";
    public static final String NOT_A_VALID_LANGUAGE_CODE
            = "Invalid language code, it should be in ISO standard format and should "
            + "not contains _ in it";
    public static final String NOT_A_VALID_LANGUAGE_CODE_IN_TRANSLATED_FIELD
            = "Invalid language code in Translated field, it should be "
            + "in ISO standard format and should not contains _ in it";
    public static final String NOT_VALID_STREAM_SERVICE = "Method should be stream post %s";
    public static final String STREAM_NOT_SUPPORTED = "Please use the stream api to access stream service %s";
    public static final String INSIGHT_DISMISSED = "Insight already dismissed.";
}
