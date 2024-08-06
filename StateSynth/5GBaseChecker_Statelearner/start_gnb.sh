#!/bin/bash
#echo "USENIX24" | sudo -S ./start_gnb.sh

if [ "$EUID" -ne 0 ]
	then echo "Need to run as root"
	exit
fi

echo "Launching start_gnb.sh"

echo "Killing any already running srsgnb process"
pkill -9 -f nr-softmodem
# ps -ef | grep srsenb | grep -v grep | awk '{print $2}' | xargs sudo kill -9

echo "Kiliing the enodeb_statelearner server listening on port 60000"
#sudo kill $(lsof -t -i:60001)
#kill -9 $(lsof -t -i:60001)

source_dir=`pwd`

cd /home/kai/Desktop/5GBaseChecker/StateSynth/modified_cellular_stack/5GBaseChecker_OAI_gnb/cmake_targets/ran_build/build

rm /tmp/OAIgNB_fuzzing.log

./nr-softmodem -O ../../../targets/PROJECTS/GENERIC-NR-5GC/CONF/gnb.sa.band78.fr1.106PRB.usrpb210.conf -E --sa --usrp-tx-thread-config 1 --continuous-tx &> /tmp/OAIgNB_fuzzing.log &

#sleep 1
#
#sleep 1

cd "$source_dir"

echo "srsenb is running in the background"
echo "log is saved to /tmp/OAIgNB_fuzzing.log"
echo "Finished lauching start_gnb.sh"

