#!/bin/bash

# Specify the file path
file_path="./CEStore/input"

# Check if the file exists
if [ -f "$file_path" ]; then
    # Truncate the file to zero size, effectively deleting all content but keeping the file
    truncate -s 0 "$file_path"
    echo "File content has been deleted."
else
    echo "Error: File does not exist."
fi