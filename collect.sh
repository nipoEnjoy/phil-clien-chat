#!/bin/bash
OUTPUT="project_dump.txt"
> "$OUTPUT"

# Исключаем бинарные, служебные и Maven Wrapper
EXCLUDE_PATTERNS="\.git$|\.git/|/target/|/build/|\.idea/|\.mvn/|/mvnw$|/mvnw\.cmd$|/\.mvn/|/\.DS_Store"

if command -v git &> /dev/null && [ -d .git ]; then
    FILES=$(git ls-files | grep -vE "$EXCLUDE_PATTERNS")
else
    FILES=$(find . -type f -not -path './.git/*' -not -path './target/*' -not -path './build/*' -not -path './.idea/*' -not -path './.mvn/*' -not -name 'mvnw' -not -name 'mvnw.cmd' -not -name '.DS_Store')
fi

echo "$FILES" | while IFS= read -r file; do
    echo "===== $file =====" >> "$OUTPUT"
    cat "$file" >> "$OUTPUT"
    echo -e "\n\n" >> "$OUTPUT"
done

echo "Сохранено в $OUTPUT (Maven Wrapper исключён)"
