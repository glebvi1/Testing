insert into users(id, activated_code, full_name, password, username)
    values(0, null, 'Gleb', '$2a$08$j.5Oqxj2149kN3FF47dY.O2qzjNoJpBdIqImyeH3jT5YP9vqARFie',
           's-admin@admin.admin');

insert into users_roles (users_id, roles)
    values (0, 'SYSTEM_ADMIN');
