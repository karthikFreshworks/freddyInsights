local DEFAULT_TENANT_MAX_TOKENS = redis.call('GET', "default_tenant_max_token") or '0'
local DEFAULT_CUSTOMER_MAX_TOKENS = redis.call('GET', "default_customer_max_token") or '0'
local DEFAULT_MODEL_MAX_TOKENS = redis.call('GET', "default_model_max_token") or '0'
local DEFAULT_EXPIRY = redis.call('GET', 'expiry') or '60'

local tenant_tokens_key = KEYS[1]
local tenant_tokens_max_key = KEYS[2]
local customer_tokens_key = KEYS[3]
local customer_tokens_max_key = KEYS[4]
local model_tokens_key = KEYS[5]
local model_tokens_max_key = KEYS[6]

if(redis.call('GET', "rate_limit") ~= '1') then
    return {0, 0, "", redis.call('GET', tenant_tokens_key), redis.call('GET', customer_tokens_key), redis.call('GET', model_tokens_key)}
end

-- Current input tokens to be used, optional
local input_tokens = tonumber(ARGV[1] or '0')

-- Current tokens used by the tenant
local tenant_current_tokens = tonumber(redis.call('GET', tenant_tokens_key) or '0')
-- Maximum tokens allowed for the tenant, customizable per tenant by setting the key
local tenant_max_tokens = tonumber(redis.call('GET', tenant_tokens_max_key) or DEFAULT_TENANT_MAX_TOKENS)
-- Maximum tokens allowed for the model, customizable per model by setting the key
local model_max_tokens = tonumber(redis.call('GET', model_tokens_max_key) or DEFAULT_MODEL_MAX_TOKENS)

if (tenant_current_tokens + input_tokens > tenant_max_tokens) then
    -- Return the wait time, so clients can retry after the expiry
    return {1, redis.call('TTL', tenant_tokens_key), 'TENANT_RATELIMITED', redis.call('GET',
            tenant_tokens_key),
            redis.call('GET', customer_tokens_key), redis.call('GET', model_tokens_key)}
end

-- Do not allow if we exceed the customer limit
-- Current tokens used by the customer
local customer_current_tokens = tonumber(redis.call('GET', customer_tokens_key) or 0)
-- Maximum tokens allowed for the customer, customizable per customer by setting the key
local customer_max_tokens = tonumber(redis.call('GET', customer_tokens_max_key) or DEFAULT_CUSTOMER_MAX_TOKENS)
if (customer_current_tokens + input_tokens > customer_max_tokens) then
    -- Return the wait time, so clients can retry after the expiry
    return {1, redis.call('TTL', customer_tokens_key), "CUSTOMER_RATELIMITED", redis.call('GET', tenant_tokens_key), redis.call('GET', customer_tokens_key), redis.call('GET', model_tokens_key)}
end

-- Do not allow if we exceed the model limit
-- Current tokens used by the customer
local model_current_tokens = tonumber(redis.call('GET', model_tokens_key) or 0)
-- Maximum tokens allowed for the model, customizable per model by setting the key
local model_max_tokens = tonumber(redis.call('GET', model_tokens_max_key) or DEFAULT_MODEL_MAX_TOKENS)
if (model_current_tokens + input_tokens > model_max_tokens) then
    -- Return the wait time, so clients can retry after the expiry
    return {1, redis.call('TTL', model_tokens_key), "MODEL_RATELIMITED", redis.call('GET', tenant_tokens_key), redis.call('GET', customer_tokens_key), redis.call('GET', model_tokens_key)}
end

-- We are good to go, update the tokens and return

-- The expiry is "rolling" here, if we want fixed window, we can check if it
-- exists and then call the EXPIRE conditionally

if redis.call('INCRBY', tenant_tokens_key, input_tokens) == input_tokens then
    redis.call('EXPIRE', tenant_tokens_key, DEFAULT_EXPIRY)
end
if redis.call('INCRBY', customer_tokens_key, input_tokens) == input_tokens then
    redis.call('EXPIRE', customer_tokens_key, DEFAULT_EXPIRY)
end
if redis.call('INCRBY', model_tokens_key, input_tokens) == input_tokens then
    redis.call('EXPIRE', model_tokens_key, DEFAULT_EXPIRY)
end

return {0, 0, "", redis.call('GET', tenant_tokens_key), redis.call('GET', customer_tokens_key), redis.call('GET', model_tokens_key)}

