ALTER TABLE IF EXISTS discord_category DROP CONSTRAINT IF EXISTS discord_guild_id_fkey;
ALTER TABLE IF EXISTS discord_media_request DROP CONSTRAINT IF EXISTS discord_category_id_fkey;
ALTER TABLE IF EXISTS discord_provider_from_discord DROP CONSTRAINT IF EXISTS discord_category_id_fkey;
ALTER TABLE IF EXISTS discord_provider_from_vk DROP CONSTRAINT IF EXISTS discord_category_id_fkey;


DROP INDEX IF EXISTS discord_guild_id_idx;
DROP INDEX IF EXISTS creation_timestamp_idx;


CREATE SEQUENCE IF NOT EXISTS discord_category_id_seq AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;
CREATE SEQUENCE IF NOT EXISTS discord_guild_id_seq AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;
CREATE SEQUENCE IF NOT EXISTS discord_media_request_id_seq AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;
CREATE SEQUENCE IF NOT EXISTS discord_provider_from_discord_id_seq AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;
CREATE SEQUENCE IF NOT EXISTS discord_provider_from_vk_id_seq AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;
CREATE SEQUENCE IF NOT EXISTS discord_trusted_suggester_id_seq AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;


CREATE TABLE IF NOT EXISTS discord_guild (
    id BIGINT NOT NULL,
    discord_id BIGINT NOT NULL,
    prefix VARCHAR(32) NOT NULL,
    CONSTRAINT discord_guild_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS discord_category (
    id BIGINT NOT NULL,
    approval_channel_id BIGINT,
    enabled BOOLEAN NOT NULL,
    end_time TIME WITHOUT TIME ZONE,
    last_send_timestamp TIMESTAMP WITHOUT TIME ZONE,
    name VARCHAR(32),
    negative_approval_emoji CHAR(1),
    positive_approval_emoji CHAR(1),
    reserve_days INTEGER,
    sending_channel_id BIGINT,
    start_time TIME WITHOUT TIME ZONE,
    guild_id BIGINT NOT NULL,
    CONSTRAINT discord_category_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS discord_media_request (
    id BIGINT NOT NULL,
    content VARCHAR(255) NOT NULL,
    creation_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    category_id BIGINT NOT NULL,
    CONSTRAINT discord_media_request_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS discord_provider_from_discord (
    id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL,
    name VARCHAR(32),
    suggestion_channel_id BIGINT,
    category_id BIGINT NOT NULL,
    CONSTRAINT discord_provider_from_discord_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS discord_provider_from_vk (
    id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL,
    group_id INTEGER,
    name VARCHAR(32),
    token VARCHAR(255),
    category_id BIGINT NOT NULL,
    CONSTRAINT discord_provider_from_vk_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS discord_trusted_suggester (
    id BIGINT NOT NULL,
    user_id BIGINT,
    category_id BIGINT NOT NULL,
    CONSTRAINT discord_trusted_suggester_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS discord_user_vk_connection (
    discord_user_id BIGINT NOT NULL,
    vk_user_id INTEGER,
    CONSTRAINT discord_user_vk_connection_pkey PRIMARY KEY (discord_user_id)
);


ALTER TABLE discord_category ADD IF NOT EXISTS guild_id BIGINT;
ALTER TABLE discord_media_request ADD IF NOT EXISTS category_id BIGINT;
ALTER TABLE discord_provider_from_discord ADD IF NOT EXISTS category_id BIGINT;
ALTER TABLE discord_provider_from_vk ADD IF NOT EXISTS category_id BIGINT;


DO $$                  
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns WHERE table_schema = CURRENT_SCHEMA() AND table_name = 'discord_category' AND column_name = 'discord_guild_id'
        ) THEN
            UPDATE discord_category SET guild_id = discord_guild_id;
            ALTER TABLE discord_category DROP COLUMN discord_guild_id;
            ALTER TABLE discord_category ALTER COLUMN guild_id SET NOT NULL;
        END IF;
    END
$$;

DO $$                  
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns WHERE table_schema = CURRENT_SCHEMA() AND table_name = 'discord_media_request' AND column_name = 'discord_category_id'
        ) THEN
            UPDATE discord_media_request SET category_id = discord_category_id;
            ALTER TABLE discord_media_request DROP COLUMN discord_category_id;
            ALTER TABLE discord_media_request ALTER COLUMN category_id SET NOT NULL;
        END IF;
    END
$$;

DO $$                  
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns WHERE table_schema = CURRENT_SCHEMA() AND table_name = 'discord_provider_from_discord' AND column_name = 'discord_category_id'
        ) THEN
            UPDATE discord_provider_from_discord SET category_id = discord_category_id;
            ALTER TABLE discord_provider_from_discord DROP COLUMN discord_category_id;
            ALTER TABLE discord_provider_from_discord ALTER COLUMN category_id SET NOT NULL;
        END IF;
    END
$$;

DO $$                  
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns WHERE table_schema = CURRENT_SCHEMA() AND table_name = 'discord_provider_from_vk' AND column_name = 'discord_category_id'
        ) THEN
            UPDATE discord_provider_from_vk SET category_id = discord_category_id;
            ALTER TABLE discord_provider_from_vk DROP COLUMN discord_category_id;
            ALTER TABLE discord_provider_from_vk ALTER COLUMN category_id SET NOT NULL;
        END IF;
    END
$$;


ALTER TABLE discord_category DROP CONSTRAINT IF EXISTS guild_id_fkey;
ALTER TABLE discord_category ADD CONSTRAINT guild_id_fkey FOREIGN KEY (guild_id) REFERENCES discord_guild (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE discord_media_request DROP CONSTRAINT IF EXISTS category_id_fkey;
ALTER TABLE discord_media_request ADD CONSTRAINT category_id_fkey FOREIGN KEY (category_id) REFERENCES discord_category (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE discord_provider_from_discord DROP CONSTRAINT IF EXISTS category_id_fkey;
ALTER TABLE discord_provider_from_discord ADD CONSTRAINT category_id_fkey FOREIGN KEY (category_id) REFERENCES discord_category (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE discord_provider_from_vk DROP CONSTRAINT IF EXISTS category_id_fkey;
ALTER TABLE discord_provider_from_vk ADD CONSTRAINT category_id_fkey FOREIGN KEY (category_id) REFERENCES discord_category (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE discord_trusted_suggester DROP CONSTRAINT IF EXISTS category_id_fkey;
ALTER TABLE discord_trusted_suggester ADD CONSTRAINT category_id_fkey FOREIGN KEY (category_id) REFERENCES discord_category (id) ON UPDATE NO ACTION ON DELETE NO ACTION;


ALTER TABLE discord_guild DROP CONSTRAINT IF EXISTS discord_guild_discord_id_unique;
ALTER TABLE discord_guild ADD CONSTRAINT discord_guild_discord_id_unique UNIQUE (discord_id);


CREATE INDEX IF NOT EXISTS discord_category_guild_id_idx ON discord_category(guild_id);
CREATE INDEX IF NOT EXISTS discord_media_request_creation_timestamp_idx ON discord_media_request(creation_timestamp);
CREATE INDEX IF NOT EXISTS discord_user_vk_connection_vk_user_id_idx ON discord_user_vk_connection(vk_user_id);
