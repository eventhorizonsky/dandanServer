CREATE TABLE IF NOT EXISTS video_metadata(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_path TEXT,
    file_name TEXT,
    file_size INTEGER,
    hash_value TEXT,
    file_extension TEXT,
    episode_id INTEGER,
    anime_id INTEGER,
    anime_title TEXT,
    episode_title TEXT,
    type TEXT,
    type_description TEXT,
    shift INTEGER,
    join_time DATE,
    is_air TEXT,
    matched TEXT
);
CREATE TABLE config (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  key TEXT,
  value TEXT
);
CREATE TABLE scan_path (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  path TEXT
);
CREATE TABLE subtitles (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  path TEXT,
  video_id INTEGER,
  subtitle_name TEXT,
  type,TEXT,
  is_default integer
);