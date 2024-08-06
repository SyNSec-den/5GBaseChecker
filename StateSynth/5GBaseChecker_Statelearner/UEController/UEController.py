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
    if is_on == '1':
        print('The device is already in airplane mode.')
        return
    os.system("adb shell settings put global airplane_mode_on 1")
    time.sleep(1)

def airplane_mode_on_oppo():
    print('--- airplane_mode_on ---')
    is_on = str(subprocess.check_output("adb shell settings get global airplane_mode_on", shell=True))[2]
    print(type(is_on))
    print(is_on)
    if is_on == '1':
        print('The device is already in airplane mode.')
        return
#     os.system("adb shell am start -a android.settings.AIRPLANE_MODE_SETTINGS")
    time.sleep(0.5)
    # developer option -> pointer location
    os.system("adb shell input tap 926 1457")  # BlackShark




def airplane_mode_off():
    print('--- airplane_mode_off ---')
    is_on = str(subprocess.check_output("adb shell settings get global airplane_mode_on", shell=True))[2]
    if is_on == '0':
        print('The device is not in airplane mode.')
        return

    os.system("adb shell settings put global airplane_mode_on 0")
    time.sleep(1)

def airplane_mode_off_oppo():
    print('--- airplane_mode_off ---')
    is_on = str(subprocess.check_output("adb shell settings get global airplane_mode_on", shell=True))[2]
    if is_on == '0':
        print('The device is not in airplane mode.')
        return
#     os.system("adb shell am start -a android.settings.AIRPLANE_MODE_SETTINGS")
    time.sleep(0.5)
    os.system("adb shell input tap 926 1457")  # BlackShark


def handle_reset(client_socket):
    # turn on the airplane mode
    print('--- START: Handling RESET command ---')

#     airplane_mode_on()
    airplane_mode_on_oppo()
    client_socket.send(('DONE\n').encode('utf-8'))
    print('### DONE: Handling RESET command ###')

def handle_enable_s1_oppo(client_socket):
    # stop and start cellular connectivity => turn on and then off the airplane mode

    # airplane mode must be on before calling this function

    print('--- START: Handling ENABLE_S1 command ---')
    print('Enabling airplane mode')
    airplane_mode_on_oppo()  # Already sleep for 1 sec in airplane_mode_on()
    time.sleep(2)
    print('Disabling airplane mode')  # Already sleep for 1 sec in airplane_mode_on()
    airplane_mode_off_oppo()

    #client_socket.send('DONE\n')
    print('### DONE: Handling ENABLE_S1 command ###')
    return

def handle_reboot(client_socket):
    os.system('adb reboot')

def handle_enable_s1(client_socket: socket.socket):
    # stop and start cellular connectivity => turn on and then off the airplane mode
    print('--- START: Handling ENABLE_S1 command ---')
    print('Enabling airplane mode')
    airplane_mode_on()  # Already sleep for 1 sec in airplane_mode_on()
    time.sleep(1) # lets try with 5 second delay.
    print('Disabling airplane mode')  # Already sleep for 1 sec in airplane_mode_on()
    airplane_mode_off()

    #client_socket.send('DONE\n')
    print('### DONE: Handling ENABLE_S1 command ###')
    return



####################################################################################################
# thread fuction
def client_handler(cs: socket.socket):
    # reboot_env(device,environment)
    while True:

        # data received from client
        data = cs.recv(1024)
        if not data:
            print('Bye')
            # lock released on exit
            print_lock.release()
            break

        command = str(data).lower()

        if "reset" in command:
            handle_reset(cs)

        elif "enable_s1" in command:
            handle_enable_s1_oppo(cs)

        elif "ue_reboot" in command:
            # handle_ue_reboot(client_socket)
            pass

        elif "adb_server_restart" in command:
            # handle_adb_server_restart(client_socket)
            pass
        else:
            print('command is ' + str(command))

    cs.close()
    # print('--- AIRPLANE MODE ON BEFORE EXIT ---')
    # airplane_mode_on()


def Main():
    proc_title = "uecontroller"
    setproctitle.setproctitle(proc_title)
    proc_title = setproctitle.getproctitle()

    host = "127.0.0.1"
    port = 61000
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((host, port))
    print("socket binded to post", port)

    # put the socket into listening mode
    s.listen(5)
    print("socket is listening")

    # establish connection with client
    client_socket, addr = s.accept()

    # lock acquired by client
    print_lock.acquire()
    print('Connected to :', addr[0], ':', addr[1])

    # Start a new thread and return its identifier
    t = Thread(target=client_handler, args=(client_socket,))
    t.start()
    t.join()




if __name__ == '__main__':
   Main()