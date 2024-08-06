#!/bin/bash
#echo "USENIX24" | sudo -S ./start_gnb.sh

if [ "$EUID" -ne 0 ]
	then echo "Need to run as root"
	exit
fi

echo "Launching start_gnb.sh"

echo "Killing any already running srsgnb process"
pkill -9 -f srsenb
# ps -ef | grep srsenb | grep -v grep | awk '{print $2}' | xargs sudo kill -9

echo "Kiliing the enodeb_statelearner server listening on port 60000"
#sudo kill $(lsof -t -i:60001)
#kill -9 $(lsof -t -i:60001)

source_dir=`pwd`

cd /home/kai/Desktop/5GBaseChecker/StateSynth/modified_cellular_stack/5GBaseChecker_srs_gnb/build/srsenb/src

rm /tmp/enb_fuzzing.log

./srsenb enb.conf &> /tmp/enb_fuzzing.log &

sleep 1

cd "$source_dir"

echo "srsenb is running in the background"
echo "log is saved to /tmp/enb_fuzzing.log"
echo "Finished lauching start_gnb.sh"