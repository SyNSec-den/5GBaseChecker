#!/bin/bash
#echo "USENIX24" | sudo -S ./start_ue.sh

if [ "$EUID" -ne 0 ]
	then echo "Need to run as root"
	exit
fi

echo "Launching start_ue.sh"

echo "Killing any already running srsue process"
pkill -9 -f srsue
# ps -ef | grep srsue | grep -v grep | awk '{print $2}' | xargs sudo kill -9

#sudo kill $(lsof -t -i:60001)
#kill -9 $(lsof -t -i:60001)

#source_dir=`pwd`

#cd /home/wtw/Desktop/clean/srs2210/srsRAN_4G/build.new/srsue/src

#rm /tmp/ue_fuzzing.log

SRSUE_SRC="/home/kai/Desktop/5GBaseChecker/StateSynth/clean/srsRAN_4G/build/srsue/src"
#SRSUE_SRC="/home/wtw/Desktop/clean/srs2210/srsRAN_4G/build/srsue/src"
CONFIGS="/home/kai/Desktop/5GBaseChecker/StateSynth/clean/srsRAN_4G/build/srsue/src"

"${SRSUE_SRC}"/srsue "${CONFIGS}"/ue.conf > /tmp/ue_fuzzing.log &

sleep 1

#cd "$source_dir"

echo "srsue is running in the background"
echo "log is saved to /tmp/ue_fuzzing.log"
echo "Finished lauching start_ue.sh"