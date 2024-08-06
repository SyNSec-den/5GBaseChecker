#!/bin/bash

if [ "$EUID" -ne 0 ]
	then echo "Need to run as root"
	exit
fi

echo "Killing open5gs"
pkill -9 -f 5gc
echo "Killing any already running srsepc process"
ps -ef | grep open5gs | grep -v grep | awk '{print $2}' | xargs kill -9

echo "Kiliing the core_statelearner server listening on port 60000"
# kill -9 $(lsof -t -i:60000)

echo "Killed open5gs"