box.cfg{
    listen = tonumber(os.getenv('PORT')),
    memtx_memory = tonumber(os.getenv('TARANTOOL_MEMORY')) * 1024 * 1024,
    log_level = tonumber(os.getenv('LOG_LEVEL'))
}

local user = os.getenv('USERNAME')
local password = os.getenv('PASSWORD')

box.once('init_user', function()
    box.schema.user.create(user, { password = password })
    box.schema.user.grant('kv_user', 'read,write,execute,create,drop', 'universe')
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