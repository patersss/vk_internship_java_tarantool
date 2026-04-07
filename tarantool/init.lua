box.cfg{
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