ALTER TABLE discord_category ADD bulk_submit BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE discord_category ALTER COLUMN name SET NOT NULL;
ALTER TABLE discord_category ALTER COLUMN positive_approval_emoji SET NOT NULL;
ALTER TABLE discord_category ALTER COLUMN negative_approval_emoji SET NOT NULL;

ALTER TABLE discord_provider_from_discord ALTER COLUMN name SET NOT NULL;

ALTER TABLE discord_provider_from_vk ALTER COLUMN name SET NOT NULL;

ALTER TABLE discord_trusted_suggester ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE discord_user_vk_connection ALTER COLUMN vk_user_id SET NOT NULL;
