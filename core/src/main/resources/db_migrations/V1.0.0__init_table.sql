
-- Create the initial Database with the structure.
CREATE TABLE IF NOT EXISTS player_cache (
  ip VARCHAR(16) PRIMARY KEY NOT NULL,
  uuid VARCHAR(36) NOT NULL,
  name VARCHAR(16) NOT NULL
);