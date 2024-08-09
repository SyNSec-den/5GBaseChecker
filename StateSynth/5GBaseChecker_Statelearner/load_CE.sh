#!/bin/bash

# Check for two command line arguments
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <source file> <destination file>"
    exit 1
fi

# Assign the command line arguments to variables
source_file="$1"
destination_file="$2"

# Check if the source file exists
if [ ! -f "$source_file" ]; then
    echo "Error: Source file does not exist."
    exit 1
fi

# Copy the contents of the source file to the destination file
cp "$source_file" "$destination_file"

echo "Contents copied from $source_file to $destination_file."
