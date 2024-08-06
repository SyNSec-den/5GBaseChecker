#!/bin/bash


if [ "$EUID" -ne 0 ]
	then echo "Need to run as root"
	exit
fi

source_dir=`pwd`

pkill -15 -f srsue
pkill -9 -f srsenb
pkill -9 -f 5gc
ps -ef | grep open5gs | grep -v grep | awk '{print $2}' | xargs kill -9


