#!/bin/bash

# Count the number of lines in learning_queries.log
learning_count=$(wc -l < learning_queries.log)

# Count the number of lines in equivalence_queries.log
equivalence_count=$(wc -l < equivalence_queries.log)

# Output the counts
echo "Number of learning queries: $learning_count"
echo "Number of equivalence queries: $equivalence_count"
