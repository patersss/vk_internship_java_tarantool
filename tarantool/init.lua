box.cfg {
    listen = tonumber(os.getenv('TARANTOOL_PORT')),
    memtx_memory = tonumber(os.getenv('TARANTOOL_MEMORY')) * 1024 * 1024,
    log_level = tonumber(os.getenv('TARANTOOL_LOG_LEVEL'))
}

local user = os.getenv('TARANTOOL_USER_NAME')
local password = os.getenv('TARANTOOL_USER_PASSWORD')

box.once('init_user', function()
    box.schema.user.create(user, { password = password })
    box.schema.user.grant(user, 'read,write,execute,create,drop', 'universe')
end)

box.once('init_schema', function()
    local kv = box.schema.space.create('KV', {
        format = {
            { name = 'key',   type = 'string' },
            { name = 'value', type = 'varbinary', is_nullable = true },
        },
        if_not_exists = true,
    })

    kv:create_index('primary', {
        type = 'TREE',
        parts = { 'key' },
        if_not_exists = true,
    })
end)

function kv_put(key, value)
    box.space.KV:replace({ key, value })
end

function kv_get(key)
    return box.space.KV:get(key)
end

function kv_delete(key)
    box.space.KV:delete(key)
end

function kv_count()
    return box.space.KV:count()
end

function kv_range_page(key_since, key_to, limit, after_key)
    local result = {}
    local count = 0
    local start_key = after_key or key_since
    local iterator = after_key and 'GT' or 'GE'

    for _, tuple in box.space.KV.index.primary:pairs(start_key, {iterator = iterator}) do
        if tuple[1] >= key_to then
            break
        end
        table.insert(result, tuple)
        count = count + 1
        if count >= limit then
            break
        end
    end
    return result
end