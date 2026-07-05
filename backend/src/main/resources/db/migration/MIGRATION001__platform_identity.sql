create table departments (
  id bigint primary key auto_increment,
  parent_id bigint null,
  name varchar(128) not null,
  code varchar(64) not null,
  description varchar(500) null,
  status varchar(20) not null,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint uk_departments_code unique (code),
  constraint fk_departments_parent foreign key (parent_id) references departments (id),
  index idx_departments_parent (parent_id),
  index idx_departments_status (status)
);

create table users (
  id bigint primary key auto_increment,
  department_id bigint null,
  username varchar(64) not null,
  email varchar(255) null,
  email_verified bit not null default true,
  display_name varchar(64) not null,
  password_hash varchar(120) not null,
  status varchar(20) not null,
  registration_source varchar(32) not null default 'ADMIN_CREATED',
  approval_status varchar(20) not null default 'APPROVED',
  registered_at datetime null,
  last_login_at datetime null,
  must_change_password bit not null default false,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  deleted_at datetime null,
  constraint fk_users_department foreign key (department_id) references departments (id),
  constraint uk_users_username unique (username),
  constraint uk_users_email unique (email),
  index idx_users_approval_status (approval_status)
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

create table email_verification_codes (
  id bigint primary key auto_increment,
  email varchar(255) not null,
  purpose varchar(32) not null,
  code_hash varchar(120) not null,
  expires_at datetime not null,
  consumed_at datetime null,
  send_count int not null default 1,
  failed_attempt_count int not null default 0,
  last_sent_at datetime not null,
  locked_until datetime null,
  ip_address varchar(64) null,
  user_agent varchar(255) null,
  created_at datetime not null default current_timestamp,
  index idx_email_codes_email_purpose (email, purpose),
  index idx_email_codes_expires_at (expires_at)
);

create table audit_events (
  id bigint primary key auto_increment,
  actor_user_id bigint null,
  actor_username varchar(64) null,
  action varchar(64) not null,
  resource_type varchar(64) null,
  resource_id varchar(64) null,
  resource_title varchar(255) null,
  ip_address varchar(64) null,
  user_agent varchar(255) null,
  payload_json varchar(2000) null,
  created_at datetime not null default current_timestamp,
  index idx_audit_events_actor (actor_user_id),
  index idx_audit_events_action (action),
  index idx_audit_events_created_at (created_at)
);

insert into departments (id, parent_id, name, code, description, status)
values
  (1, null, '默认组织', 'DEFAULT', '系统初始化部门', 'ACTIVE'),
  (2, 1, '四级考生', 'CET4_CANDIDATES', 'CET4 初始化考生部门', 'ACTIVE');

insert into users (id, department_id, username, email, email_verified, display_name, password_hash, status, registration_source, approval_status, registered_at, must_change_password)
values
  (1, 1, 'admin', 'admin@example.com', true, '系统管理员', '$2a$10$KSY7KAawfe6xK/gp4bXt2erxEFQe0w4kSVfgM1/ZVJobedbZrQZq6', 'ACTIVE', 'ADMIN_CREATED', 'APPROVED', current_timestamp, false),
  (2, 2, 'zhangsan', 'zhangsan@example.com', true, '张三', '$2a$10$KSY7KAawfe6xK/gp4bXt2erxEFQe0w4kSVfgM1/ZVJobedbZrQZq6', 'ACTIVE', 'ADMIN_CREATED', 'APPROVED', current_timestamp, false);

insert into roles (id, code, name, description)
values
  (1, 'ADMIN', '系统管理员', '平台初始化管理员'),
  (2, 'EXAM_MANAGER', '考务管理员', '维护考试、题库和成绩'),
  (3, 'STUDENT', '考生', '参加考试和查看成绩');

insert into permissions (id, code, name, description)
values
  (1, 'system:admin', '系统管理', '访问系统管理能力'),
  (2, 'exam:manage', '考试管理', '维护考试发布和考务任务'),
  (4, 'question:manage', '题库管理', '维护题库和试题'),
  (5, 'exam:take', '参加考试', '进入考试端作答'),
  (6, 'result:view', '成绩查看', '查看成绩和作答结果'),
  (7, 'system:settings', '平台设置', '维护注册策略和邮件状态'),
  (8, 'system:audit', '审计日志', '查看平台审计事件'),
  (9, 'system:users', '用户管理', '维护用户和注册审核');

insert into user_roles (user_id, role_id)
values
  (1, 1),
  (2, 3);

insert into role_permissions (role_id, permission_id)
select 1, id from permissions;

insert into role_permissions (role_id, permission_id)
select 3, id from permissions where code in ('exam:take', 'result:view');

insert into system_configs (config_key, config_value, description)
values
  ('platform.name', 'kaoshi', '平台名称'),
  ('auth.registration.settings', '{"selfRegistrationEnabled":true,"emailVerificationRequired":true,"adminApprovalRequired":false,"defaultRoleCode":"STUDENT","defaultDepartmentId":2,"allowedEmailDomains":[],"termsText":""}', '注册策略');
