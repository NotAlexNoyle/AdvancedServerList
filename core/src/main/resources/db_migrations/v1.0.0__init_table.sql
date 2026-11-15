
-- Create the initial Database with the structure.
CREATE TABLE IF NOT EXISTS player_cache (
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  ip VARCHAR(9) NOT NULL,
  uuid VARCHAR(36) NOT NULL,
  name VARCHAR(16) NOT NULL
);