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
    matched TEXT
);
