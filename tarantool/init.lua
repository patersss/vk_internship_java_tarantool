box.cfg{
    listen = tonumber(os.getenv('PORT')),
    memtx_memory = 512 * 1024 * 1024,  -- 256 MB, хватит на 5M мелких записей
    log_level = 5
}

box.once('init_user', function()
    box.schema.user.create('kv_user', { password = 'kv_password' })
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