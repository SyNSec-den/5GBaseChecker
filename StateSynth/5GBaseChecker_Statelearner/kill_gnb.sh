#!/bin/bash

#echo "USENIX24" | sudo -S ./kill_gnb.sh 

if [ "$EUID" -ne 0 ]
	then echo "Need to run as root"
	exit
fi

source_dir=`pwd`

echo "Killing OAI gNB"

pkill -9 -f nr-softmodem
pkill -9 -f srsenb
# ps -ef | grep srsenb | grep -v grep | awk '{print $2}' | xargs sudo kill -9

/usr/local/lib/uhd/utils/b2xx_fx3_utils --reset-device

#for i in /sys/bus/pci/drivers/xhci_hcd/*:*; do
#
#  [ -e "$i" ] || continue
#
#  echo "${i##*/}" > "${i%/*}/unbind"
#
#  echo "${i##*/}" > "${i%/*}/bind"
#
#done

echo "Kiliing the enodeb_statelearner server listening on port 60001"
#kill -9 $(lsof -t -i:60001)

echo "Killed OAI gNB"


