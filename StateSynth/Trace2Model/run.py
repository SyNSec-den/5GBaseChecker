#######################################################################
# Run example benchmarks
# Author: Natasha Yogananda Jeppu, natasha.yogananda.jeppu@cs.ox.ac.uk
#         University of Oxford
#######################################################################

import sys
import os
from os import listdir
from os.path import isfile, join
import time

from os.path import abspath
from termcolor import colored
import argparse

def parse_args():
    parser = argparse.ArgumentParser()
    required_parse = parser.add_argument_group('required arguments')
    required_parse.add_argument('-gen_o','--gen_option', metavar = 'MODEL_GEN_OPTION', required = True, choices=['incr','non-incr'],
            help='Automata learning option : incr (incremental), non-incr (non-incremental)')
    required_parse.add_argument('-mt','--model_type', metavar = 'MODEL_TYPE', required = True, choices=['dfa','nfa'],
            help='Generate NFA or DFA.')
    parser.add_argument('-syn', '--syn_type', metavar = 'SYNTHESIS_TYPE', choices=['guard','update',''], default='',
            help='Predicate synthesis: guard, update')

    hyperparams = parser.parse_args()
    return hyperparams

def main():
	hyperparams = parse_args()

	gen_option = hyperparams.gen_option
	mt = hyperparams.model_type
	syn = hyperparams.syn_type

	if(syn != ''):
		if(syn == 'guard'):
			os.system('python3 ' + full_path + 'syn_next_event.py -i ' + full_path + 'benchmarks/Predicate_Synth/heartbeat.txt -c 0')
			os.system('python3 ' + full_path + 'syn_next_event.py -i ' + full_path + 'benchmarks/Predicate_Synth/hvactemp.txt -c nil')

			# uncomment for more examples
			# os.system('python3 ' + full_path + 'syn_next_event.py -i ' + full_path + 'benchmarks/Predicate_Synth/isaalarm.txt -c nil')
			# os.system('python3 ' + full_path + 'syn_next_event.py -i ' + full_path + 'benchmarks/Predicate_Synth/sensor.txt -c nil')
			# os.system('python3 ' + full_path + 'syn_next_event.py -i ' + full_path + 'benchmarks/Predicate_Synth/simple_alarm.txt -c nil')
		elif(syn == 'update'):
			os.system('python3 ' + full_path + 'syn_event_update.py -i ' + full_path + 'benchmarks/Predicate_Synth/uart.txt -v x:N -c 0 1')
			os.system('python3 ' + full_path + 'syn_event_update.py -i ' + full_path + 'benchmarks/Predicate_Synth/integrator.txt -v op:N -c 0')
		

	mypath = full_path + 'benchmarks/SoC/'
	onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f)) and '.txt' in f]

	result = []

	if(syn == ''):
		for f in onlyfiles:
			print("\nRunning example: " + f)

			start_time = time.time()
			cmd = 'python3 learn_model.py -i ' + mypath + f
			if gen_option == 'incr':
				cmd = cmd + ' --incr'
			if mt == 'dfa':
				cmd = cmd + ' --dfa'
			print(cmd)
			os.system(cmd)
			end_time = time.time()

			temp = [f,end_time-start_time]
			result.append(temp)
			print("\n\n")

		for x in result:
			print(x[0] + ': ' + str(x[1]))

	else:
		if(syn == 'guard'):
			cmd = 'python3 learn_model.py -i ' + full_path + 'benchmarks/Predicate_Synth/heartbeat_events.txt'
			if gen_option == 'incr':
				cmd = cmd + ' --incr'
			if mt == 'dfa':
				cmd = cmd + ' --dfa'
			os.system(cmd)
			
			cmd = 'python3 learn_model.py -i ' + full_path + 'benchmarks/Predicate_Synth/hvactemp_events.txt'
			if gen_option == 'incr':
				cmd = cmd + ' --incr'
			if mt == 'dfa':
				cmd = cmd + ' --dfa'
			os.system(cmd)
		else:
			cmd = 'python3 learn_model.py -i ' + full_path + 'benchmarks/Predicate_Synth/uart_events.txt'
			if gen_option == 'incr':
				cmd = cmd + ' --incr'
			if mt == 'dfa':
				cmd = cmd + ' --dfa'
			os.system(cmd)
			
			cmd = 'python3 learn_model.py -i ' + full_path + 'benchmarks/Predicate_Synth/integrator_events.txt'
			if gen_option == 'incr':
				cmd = cmd + ' --incr'
			if mt == 'dfa':
				cmd = cmd + ' --dfa'
			os.system(cmd)

full_path = abspath(__file__).replace('run.py','')

if __name__ == '__main__':
	start_time = time.time()
	main()
	end_time = time.time()
	print('\n\nTime taken: ' + str(end_time - start_time))





