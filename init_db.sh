#!/bin/sh

DB_FILE="/app/data/mydatabase.db"

if [ ! -f "$DB_FILE" ]; then
    echo "Initializing database..."
    # 在这里执行初始化数据库的命令，例如：
    sqlite3 "$DB_FILE" 'CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT);'
    echo "Database initialized."
else
    echo "Database file already exists. Skipping initialization."
fi
