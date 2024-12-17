#!/bin/bash

DATABASE_FILE="database"
SQL_SCRIPT="DBdemo.sql"
SQLITE_EXE="sqlite3"

echo "Checking if \"$SQLITE_EXE\" is in PATH..."
if command -v "$SQLITE_EXE" &> /dev/null; then
  echo "$SQLITE_EXE" found!
else
  echo "$SQLITE_EXE" not found in PATH!! Please make sure it is installed and added to your PATH environment variable!!
  exit 1
fi

echo "Checking if \"$SQL_SCRIPT\" script exists..."
if [ ! -f "$SQL_SCRIPT" ]; then
  echo "SQL script file \"$SQL_SCRIPT\" not found!! Please make sure it is in the same directory as $DATABASE_FILE"
  exit 1
else
  echo "$SQL_SCRIPT" found!
fi

echo "Trying to execute $SQL_SCRIPT script..."
$SQLITE_EXE "$DATABASE_FILE" < "$SQL_SCRIPT"

if [ $? -eq 0 ]; then
  echo "Script executed successfully!"
else
  echo "Error executing script!!"
  exit 1
fi

echo "Press Enter to continue..."
read
