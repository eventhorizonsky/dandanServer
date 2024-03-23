#!/bin/sh

DB_FILE="/mydatabase.db"

if [ ! -f "$DB_FILE" ]; then
    echo "Initializing database..."
    # 在这里执行初始化数据库的命令，例如：
    sqlite3 "$DB_FILE" 'CREATE TABLE IF NOT EXISTS video_metadata (
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
                        );'
    echo "Database initialized."
else
    echo "Database file already exists. Skipping initialization."
fi
