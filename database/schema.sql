CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE auth_provider AS ENUM ('local', 'google');
CREATE TYPE time_of_day AS ENUM ('morning', 'forenoon', 'noon', 'afternoon', 'evening');
CREATE TYPE module_priority AS ENUM ('high', 'neutral', 'low');
CREATE TYPE task_category AS ENUM ('exam', 'submission', 'other');
CREATE TYPE task_status AS ENUM ('active', 'completed');
CREATE TYPE unit_status AS ENUM ('planned', 'active', 'ended_early', 'missed', 'completed');
CREATE TYPE plan_status AS ENUM ('active', 'archived');
CREATE TYPE recurring_day AS ENUM ('mon','tue','wed','thu','fri','sat','sun');

CREATE TABLE users (
                       userid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username TEXT NOT NULL,
                       password_hash TEXT,
                       password_salt TEXT,
                       auth_provider auth_provider NOT NULL DEFAULT 'local',
                       google_sub TEXT,
                       refresh_token_hash TEXT,
                       refresh_token_expires_at TIMESTAMPTZ,
                       CONSTRAINT uq_users_google_sub UNIQUE (google_sub),
                       CONSTRAINT chk_users_auth_provider_fields
                           CHECK (
                               (auth_provider = 'local'
                                   AND password_hash IS NOT NULL
                                   AND password_salt IS NOT NULL
                                   AND google_sub IS NULL
                                   )
                                   OR
                               (auth_provider = 'google'
                                   AND password_hash IS NULL
                                   AND password_salt IS NULL
                                   AND google_sub IS NOT NULL
                                   )
                               ),
                       CONSTRAINT chk_users_refresh_token_consistent
                           CHECK (
                               (refresh_token_hash IS NULL AND refresh_token_expires_at IS NULL)
                                   OR
                               (refresh_token_hash IS NOT NULL AND refresh_token_expires_at IS NOT NULL)
                               )
);

CREATE UNIQUE INDEX uq_users_username_local
    ON users (username)
    WHERE auth_provider = 'local';

CREATE TABLE learning_preferences (
                                      prefid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      userid UUID UNIQUE NOT NULL REFERENCES users(userid) ON DELETE CASCADE,
                                      min_unit_duration_minutes INTEGER NOT NULL CHECK (min_unit_duration_minutes > 0),
                                      max_unit_duration_minutes INTEGER NOT NULL CHECK (max_unit_duration_minutes > 0),
                                      preferred_time_of_day time_of_day NOT NULL,
                                      max_daily_workload_hours INTEGER NOT NULL CHECK (max_daily_workload_hours > 0),
                                      deadline_buffer_days INTEGER NOT NULL CHECK (deadline_buffer_days >= 0),
                                      break_duration_minutes INTEGER NOT NULL CHECK (break_duration_minutes >= 0),
                                      CONSTRAINT chk_duration_min_le_max
                                          CHECK (min_unit_duration_minutes <= max_unit_duration_minutes)
);

CREATE TABLE modules (
                         moduleid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         userid UUID NOT NULL REFERENCES users(userid) ON DELETE CASCADE,
                         title TEXT NOT NULL,
                         description TEXT,
                         priority module_priority NOT NULL,
                         color_hex_code TEXT
);

CREATE INDEX idx_modules_userid ON modules(userid);

CREATE TABLE tasks (
                       taskid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       moduleid UUID NOT NULL REFERENCES modules(moduleid) ON DELETE CASCADE,
                       title TEXT NOT NULL,
                       taskcategory task_category NOT NULL,
                       weekly_effort_minutes INTEGER NOT NULL CHECK (weekly_effort_minutes > 0),
                       fixed_deadline DATE,
                       time_frame_start DATE,
                       time_frame_end DATE,
                       status task_status NOT NULL DEFAULT 'active',
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                       CONSTRAINT chk_task_timerange_valid
                           CHECK (
                               time_frame_start IS NULL OR time_frame_end IS NULL OR time_frame_start <= time_frame_end
                               )
);

CREATE INDEX idx_tasks_moduleid ON tasks(moduleid);

CREATE TABLE cost_matrices (
                               matrixid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               taskid UUID UNIQUE NOT NULL REFERENCES tasks (taskid) ON DELETE CASCADE,
                               costs JSONB NOT NULL DEFAULT '{}'::jsonb,
                               is_outdated BOOLEAN NOT NULL DEFAULT FALSE,
                               last_updated TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE learning_units (
                                unitid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                taskid UUID NOT NULL REFERENCES tasks(taskid) ON DELETE CASCADE,
                                start_time TIMESTAMP WITH TIME ZONE NOT NULL,
                                end_time TIMESTAMP WITH TIME ZONE NOT NULL,
                                actual_duration_minutes INTEGER,
                                status unit_status NOT NULL DEFAULT 'planned',
                                CONSTRAINT chk_unit_end_after_start
                                    CHECK (end_time > start_time),
                                CONSTRAINT chk_unit_actual_duration_nonneg
                                    CHECK (actual_duration_minutes IS NULL OR actual_duration_minutes >= 0)
);

CREATE INDEX idx_learning_units_taskid ON learning_units(taskid);
CREATE INDEX idx_learning_units_start_time ON learning_units(start_time);

CREATE TABLE ratings (
                         ratingid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         unitid UUID UNIQUE NOT NULL REFERENCES learning_units(unitid) ON DELETE CASCADE,
                         goal_achievement_score INTEGER CHECK (goal_achievement_score BETWEEN 1 AND 5),
                         perceived_duration_score INTEGER CHECK (perceived_duration_score BETWEEN 1 AND 3),
                         concentration_score INTEGER CHECK (concentration_score BETWEEN 1 AND 5)
);

CREATE TABLE free_times (
                            slotid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            userid UUID NOT NULL REFERENCES users(userid) ON DELETE CASCADE,
                            title TEXT NOT NULL,
                            start_time TIME NOT NULL,
                            end_time TIME NOT NULL,
                            weekday recurring_day,
                            specific_date DATE,
                            CONSTRAINT chk_freetime_time_order
                                CHECK (end_time > start_time),
                            CONSTRAINT chk_freetime_weekday_xor_date
                                CHECK (
                                    (weekday IS NOT NULL AND specific_date IS NULL)
                                        OR
                                    (weekday IS NULL AND specific_date IS NOT NULL)
                                    )
);

CREATE INDEX idx_free_times_userid_weekday ON free_times(userid, weekday);
CREATE INDEX idx_free_times_userid_specific_date ON free_times(userid, specific_date);

CREATE TABLE learning_plans (
                                planid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                userid UUID NOT NULL REFERENCES users(userid) ON DELETE CASCADE,
                                validity_week_start DATE NOT NULL,
                                validity_week_end DATE NOT NULL,
                                status plan_status NOT NULL DEFAULT 'active',
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                UNIQUE(userid, validity_week_start),
                                CONSTRAINT chk_plan_week_valid
                                    CHECK (validity_week_end >= validity_week_start)
);

CREATE INDEX idx_learning_plans_userid_week ON learning_plans(userid, validity_week_start);

CREATE TABLE plan_units (
                            planid UUID NOT NULL REFERENCES learning_plans(planid) ON DELETE CASCADE,
                            unitid UUID NOT NULL REFERENCES learning_units(unitid) ON DELETE CASCADE,
                            PRIMARY KEY (planid, unitid)
);