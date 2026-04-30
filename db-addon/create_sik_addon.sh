#!/bin/bash

DB="sik_temporary"
USER="root"
MAIN="../sik.sql"
OUT="sik+addon.sql"

mariadb -u$USER -e "DROP DATABASE $DB;"
mariadb -u$USER -e "CREATE DATABASE $DB;"

echo "Importing $MAIN..."
mariadb -u$USER $DB < "$MAIN"

for f in *.sql; do
    if [ "$f" != "$MAIN" ] && [ "$f" != "$OUT" ]; then
        echo "Importing $f..."
        mariadb -u$USER --init-command='SET FOREIGN_KEY_CHECKS=0;' $DB < "$f"
    fi
done

echo "Extracting structure..."
mariadb-dump -u$USER --no-data $DB > "$OUT"

mariadb -u$USER -e "DROP DATABASE $DB;"

echo "Done! Structure saved to $OUT"
