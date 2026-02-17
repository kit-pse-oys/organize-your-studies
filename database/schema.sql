create table cost_matrices (
                               is_outdated boolean not null,
                               last_updated timestamp(6) not null,
                               matrixid uuid not null,
                               taskid uuid not null unique,
                               costs jsonb not null,
                               primary key (matrixid)
);

create table free_times (
                            end_time time(6) not null,
                            specific_date date,
                            start_time time(6) not null,
                            slotid uuid not null,
                            user_id uuid not null,
                            recurrence_type_discriminator varchar(31) not null,
                            title varchar(255) not null,
                            weekday varchar(255),
                            primary key (slotid),
                            constraint chk_free_times_weekday
                                check (weekday is null or weekday in ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'))
);

create table learning_plans (
                                week_end date not null,
                                week_start date not null,
                                planid uuid not null,
                                user_id uuid not null,
                                primary key (planid),
                                constraint uq_learning_plans_user_week unique (user_id, week_start)
);

create table learning_preferences (
                                      break_duration_minutes integer not null,
                                      deadline_buffer_days integer not null,
                                      max_daily_workload_hours integer not null,
                                      max_unit_duration_minutes integer not null,
                                      min_unit_duration_minutes integer not null,
                                      preference_id uuid not null,
                                      primary key (preference_id)
);

create table learning_units (
                                actual_duration_minutes integer,
                                end_time timestamp(6) not null,
                                start_time timestamp(6) not null,
                                ratingid uuid unique,
                                taskid uuid not null,
                                unitid uuid not null,
                                status varchar(255) not null check (status in ('PLANNED','COMPLETED','MISSED')),
                                primary key (unitid),
                                constraint chk_learning_units_end_after_start check (end_time > start_time),
                                constraint chk_learning_units_actual_duration_nonneg check (actual_duration_minutes is null or actual_duration_minutes >= 0)
);

create table modules (
                         moduleid uuid not null,
                         user_id uuid not null,
                         color_hex_code varchar(255),
                         description varchar(255),
                         priority varchar(255) not null check (priority in ('LOW','MEDIUM','HIGH')),
                         title varchar(255) not null,
                         primary key (moduleid)
);

create table plan_units (
                            planid uuid not null,
                            unitid uuid not null,
                            primary key (planid, unitid)
);

create table preferred_time_slots (
                                      preference_id uuid not null,
                                      time_slot varchar(255) check (time_slot in ('MORNING','FORENOON','NOON','AFTERNOON','EVENING')),
                                      primary key (preference_id, time_slot)
);

create table preferred_week_days (
                                     preference_id uuid not null,
                                     day_of_week varchar(255) check (day_of_week in ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
                                     primary key (preference_id, day_of_week)
);

create table ratings (
                         ratingid uuid not null,
                         achievement varchar(255) check (achievement in ('NONE','POOR','PARTIAL','GOOD','EXCELLENT')),
                         concentration varchar(255) check (concentration in ('VERY_LOW','LOW','MEDIUM','HIGH','VERY_HIGH')),
                         duration_perception varchar(255) check (duration_perception in ('MUCH_TOO_SHORT','TOO_SHORT','IDEAL','TOO_LONG','MUCH_TOO_LONG')),
                         primary key (ratingid)
);

create table tasks (
                       cycle_weeks integer,
                       fixed_deadline date,
                       weekly_duration_minutes integer not null,
                       end_time timestamp(6),
                       first_deadline timestamp(6),
                       time_frame_end timestamp(6),
                       time_frame_start timestamp(6),
                       moduleid uuid not null,
                       taskid uuid not null,
                       taskcategory varchar(31) not null,
                       title varchar(255) not null,
                       primary key (taskid),
                       constraint chk_tasks_weekly_duration_nonneg check (weekly_duration_minutes >= 0),
                       constraint chk_tasks_time_frame_valid check (time_frame_start is null or time_frame_end is null or time_frame_end > time_frame_start)
);

create table users (
                       refresh_token_expiration timestamp(6),
                       preferences_preference_id uuid unique,
                       user_id uuid not null,
                       auth_provider varchar(31) not null,
                       google_sub varchar(255),
                       password_hash varchar(255),
                       refresh_token_hash varchar(255),
                       user_type varchar(255) check (user_type in ('LOCAL','GOOGLE')),
                       username varchar(255),
                       primary key (user_id)
);

alter table if exists cost_matrices
    add constraint fk_cost_matrices_task
    foreign key (taskid) references tasks (taskid)
    on delete cascade;

alter table if exists free_times
    add constraint fk_free_times_user
    foreign key (user_id) references users (user_id)
    on delete cascade;

alter table if exists learning_plans
    add constraint fk_learning_plans_user
    foreign key (user_id) references users (user_id)
    on delete cascade;

alter table if exists learning_units
    add constraint fk_learning_units_rating
    foreign key (ratingid) references ratings (ratingid)
    on delete set null;

alter table if exists learning_units
    add constraint fk_learning_units_task
    foreign key (taskid) references tasks (taskid)
    on delete cascade;

alter table if exists modules
    add constraint fk_modules_user
    foreign key (user_id) references users (user_id)
    on delete cascade;

alter table if exists plan_units
    add constraint fk_plan_units_unit
    foreign key (unitid) references learning_units (unitid)
    on delete cascade;

alter table if exists plan_units
    add constraint fk_plan_units_plan
    foreign key (planid) references learning_plans (planid)
    on delete cascade;

alter table if exists preferred_time_slots
    add constraint fk_preferred_time_slots_pref
    foreign key (preference_id) references learning_preferences (preference_id)
    on delete cascade;

alter table if exists preferred_week_days
    add constraint fk_preferred_week_days_pref
    foreign key (preference_id) references learning_preferences (preference_id)
    on delete cascade;

alter table if exists tasks
    add constraint fk_tasks_module
    foreign key (moduleid) references modules (moduleid)
    on delete cascade;

alter table if exists users
    add constraint fk_users_preferences
    foreign key (preferences_preference_id) references learning_preferences (preference_id)
    on delete set null;
