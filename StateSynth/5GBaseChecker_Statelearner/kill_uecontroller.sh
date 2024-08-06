#!/bin/bash
#echo "USENIX24" | sudo -S ./kill_core.sh 

if [ "$EUID" -ne 0 ]
	then echo "Need to run as root"
	exit
fi

echo "Killing uecontroller"
pkill -9 -f uecontroller

echo "Killed uecontroller"