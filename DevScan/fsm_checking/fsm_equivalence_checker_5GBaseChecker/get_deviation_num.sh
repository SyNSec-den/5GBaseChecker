#!/bin/bash

# Path to the JSON file
FILE="./deviant-queries.json"

# Count the number of "input_symbols" in the JSON file
count=$(grep -o '"input_symbols"' "$FILE" | wc -l)

# Print the count
echo "The number of deviations in the file: $count"
