#!/bin/bash
#echo "USENIX24" | sudo -S ./start_core.sh 

if [ "$EUID" -ne 0 ]
	then echo "Need to run as root"
	exit
fi

echo "Launching start_core.sh"

echo "Killing any already running srsepc process"
pkill -9 -f open5gs
pkill -9 -f 5gc
ps -ef | grep open5gs | grep -v grep | awk '{print $2}' | xargs kill -2

echo "Killing the core_statelearner server listening on port 60000"

echo "Killing done!"

sleep 1

source_dir=`pwd`
cd /home/kai/Desktop/5GBaseChecker/StateSynth/modified_cellular_stack/5GBaseChecker_Core

./build/tests/app/5gc ./build/configs/sample.yaml &> /tmp/core_fuzzing.log &

cd "$source_dir"

echo "Finished launching start_core.sh"