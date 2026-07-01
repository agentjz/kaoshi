create table users (
  id bigint primary key auto_increment,
  username varchar(64) not null,
  display_name varchar(64) not null,
  password_hash varchar(120) not null,
  status varchar(20) not null,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  deleted_at datetime null,
  constraint uk_users_username unique (username)
);

create table roles (
  id bigint primary key auto_increment,
  code varchar(64) not null,
  name varchar(64) not null,
  description varchar(255) null,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint uk_roles_code unique (code)
);

create table permissions (
  id bigint primary key auto_increment,
  code varchar(128) not null,
  name varchar(64) not null,
  description varchar(255) null,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint uk_permissions_code unique (code)
);

create table user_roles (
  user_id bigint not null,
  role_id bigint not null,
  created_at datetime not null default current_timestamp,
  primary key (user_id, role_id),
  constraint fk_user_roles_user foreign key (user_id) references users (id),
  constraint fk_user_roles_role foreign key (role_id) references roles (id)
);

create table role_permissions (
  role_id bigint not null,
  permission_id bigint not null,
  created_at datetime not null default current_timestamp,
  primary key (role_id, permission_id),
  constraint fk_role_permissions_role foreign key (role_id) references roles (id),
  constraint fk_role_permissions_permission foreign key (permission_id) references permissions (id)
);

create table system_configs (
  id bigint primary key auto_increment,
  config_key varchar(128) not null,
  config_value varchar(1000) not null,
  description varchar(255) null,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint uk_system_configs_key unique (config_key)
);

create table login_audits (
  id bigint primary key auto_increment,
  user_id bigint null,
  username varchar(64) not null,
  success bit not null,
  ip_address varchar(64) null,
  user_agent varchar(255) null,
  failure_reason varchar(255) null,
  created_at datetime not null default current_timestamp,
  index idx_login_audits_user_id (user_id),
  index idx_login_audits_username (username)
);

insert into users (id, username, display_name, password_hash, status)
values (1, 'admin', '系统管理员', '$2a$10$KSY7KAawfe6xK/gp4bXt2erxEFQe0w4kSVfgM1/ZVJobedbZrQZq6', 'ACTIVE');

insert into roles (id, code, name, description)
values
  (1, 'ADMIN', '系统管理员', '平台初始化管理员'),
  (2, 'EXAM_MANAGER', '考务管理员', '维护考试、试卷、题库和成绩'),
  (3, 'STUDENT', '考生', '参加考试和查看成绩');

insert into permissions (id, code, name, description)
values
  (1, 'system:admin', '系统管理', '访问系统管理能力'),
  (2, 'exam:manage', '考试管理', '维护考试发布和考务任务'),
  (3, 'paper:manage', '试卷管理', '维护试卷和组卷'),
  (4, 'question:manage', '题库管理', '维护题库和试题'),
  (5, 'exam:take', '参加考试', '进入考试端作答'),
  (6, 'result:view', '成绩查看', '查看成绩和作答结果');

insert into user_roles (user_id, role_id) values (1, 1);

insert into role_permissions (role_id, permission_id)
select 1, id from permissions;

insert into system_configs (config_key, config_value, description)
values ('platform.name', 'kaoshi', '平台名称');

