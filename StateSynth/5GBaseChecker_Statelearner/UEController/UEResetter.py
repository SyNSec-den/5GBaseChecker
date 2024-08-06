#!/usr/bin/python
# import socket programming library
import os
import socket
import subprocess
import threading
import time
import setproctitle

# import thread module
from threading import *

global device
global environment
print_lock = threading.Lock()



def airplane_mode_on():
    print('--- airplane_mode_on ---')
    is_on = str(subprocess.check_output("adb shell settings get global airplane_mode_on", shell=True))[2]
    print(type(is_on))
    print(is_on)
    if is_on == '1':
        print('The device is already in airplane mode.')
        return
    os.system("adb shell am start -a android.settings.AIRPLANE_MODE_SETTINGS")
    time.sleep(0.5)
    # developer option -> pointer location
    os.system("adb shell input tap 906 439") 



if __name__ == '__main__':
   print('Enabling airplane mode')
   airplane_mode_on()  
   time.sleep(1)
