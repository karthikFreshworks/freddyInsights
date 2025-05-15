local DEFAULT_TENANT_MAX_TOKENS = redis.call('GET', "default_tenant_max_token") or '0'
local DEFAULT_CUSTOMER_MAX_TOKENS = redis.call('GET', "default_customer_max_token") or '0'
local DEFAULT_MODEL_MAX_TOKENS = redis.call('GET', "default_model_max_token") or '0'
local DEFAULT_EXPIRY = redis.call('GET', 'expiry') or '60'

-- We need to pass all the keys used here explicitly to be compatible with
-- redis cluster mode
local tenant_tokens_key = KEYS[1]
local tenant_tokens_max_key = KEYS[2]
local customer_tokens_key = KEYS[3]
local customer_tokens_max_key = KEYS[4]
local model_tokens_key = KEYS[5]
local model_tokens_max_key = KEYS[6]

local total_tokens = tonumber(ARGV[1] or 0)

if(redis.call('GET', "rate_limit") ~= '1') then
    return {0, 0, "", redis.call('GET', tenant_tokens_key), redis.call('GET', customer_tokens_key), redis.call('GET', model_tokens_key)}
end

if total_tokens > 0 then
    if redis.call('INCRBY', tenant_tokens_key, tonumber(total_tokens)) == total_tokens then
        redis.call('EXPIRE', tenant_tokens_key, DEFAULT_EXPIRY)
    end
    if redis.call('INCRBY', customer_tokens_key, tonumber(total_tokens)) == total_tokens then
        redis.call('EXPIRE', customer_tokens_key, DEFAULT_EXPIRY)
    end
    if redis.call('INCRBY', model_tokens_key, tonumber(total_tokens)) == total_tokens then
        redis.call('EXPIRE', model_tokens_key, DEFAULT_EXPIRY)
    end
end

local tenant_max_token = tonumber(redis.call('GET', tenant_tokens_max_key) or DEFAULT_TENANT_MAX_TOKENS)
local remaining_tenant_token = tenant_max_token - tonumber(redis.call('GET', tenant_tokens_key))
local customer_max_token = tonumber(redis.call('GET', customer_tokens_max_key) or DEFAULT_CUSTOMER_MAX_TOKENS)
local remaining_customer_token = customer_max_token - tonumber(redis.call('GET', customer_tokens_key))
local model_max_token =  tonumber(redis.call('GET', model_tokens_max_key) or DEFAULT_MODEL_MAX_TOKENS)
local remaining_model_token = model_max_token - tonumber(redis.call('GET', model_tokens_key))

return {redis.call('GET', tenant_tokens_key), redis.call('GET', customer_tokens_key), redis.call('GET',
        model_tokens_key), remaining_tenant_token, remaining_customer_token, remaining_model_token}
