#######################################################################
# Generate transition guards for next event
# Author: Natasha Yogananda Jeppu, natasha.yogananda.jeppu@cs.ox.ac.uk
#         University of Oxford
#######################################################################

import argparse
import numpy as np
import pickle
import re
import statistics as stat
import subprocess
import sys
import time

from os.path import abspath
from termcolor import colored

import tree

def pre_process(trace):
	trace_set = dict.fromkeys([i[0] for i in trace if i[0]!='trace'],0)

	j = 0
	for j in range(len(trace)):
		if trace[j][0] == 'trace':
			continue
		if any([x == '-' for x in trace[j][1:]]):
			continue
		temp1 = trace[j][1:]
		if trace[j+1][0] == 'trace':
			temp1.append('-')
		else:
			temp1.append(trace[j+1][0])
		if(trace_set[trace[j][0]] == 0):
			trace_set[trace[j][0]] = [temp1]
		else:
			trace_set[trace[j][0]].append(temp1)

	input_dict = {'trace_set': trace_set}

	return input_dict

def post_process(output,next_event,data_type,trace_dict,enum_val):
	event_keys = trace_dict['event_keys']
	output_split = output.split()
	syn_event = dict.fromkeys([str(x) for x in next_event],0)

	for i in range(len(output_split)-1,-1,-1):
		if (output_split[i] in ['(+','(-','(*','(<=','(>=','(=','(>','(<']):
			if output_split[i] == '(=':
				output_split[i : i+3] = ['(' + output_split[i+1] + ' == ' + output_split[i+2]]
			elif output_split[i] == '(-':
				if output_split[i+1].count('(') != output_split[i+1].count(')'):
					output_split[i : i+2] = ['(-' + output_split[i+1]]
				else:
					output_split[i : i+3] = ['(' + output_split[i+1] + ' ' + output_split[i].replace('(','') + ' ' + output_split[i+2]]
			else:
				output_split[i : i+3] = ['(' + output_split[i+1] + ' ' + output_split[i].replace('(','') + ' ' + output_split[i+2]]
			
		elif output_split[i] == '(not':
			output_split[i : i+2] = [' '.join(output_split[i : i+2])]
		elif output_split[i] == '(abs':
			if '(' in output_split[i+1]:
				output_split[i : i+2] = ['(abs' + output_split[i+1]]
			else:
				output_split[i : i+2] = ['(abs(' + output_split[i+1] + ')']
		elif (output_split[i] in ['(and','(or']):
			output_split[i : i+3] = ['(' + output_split[i+1] + ' ' + output_split[i].replace('(','') + ' ' + output_split[i+2]]

	cond = []
	arg1 = []
	arg2 = []
	expr = []

	for i in range(len(output_split)-1,-1,-1):
		if (output_split[i] == '(ite'):
			cond.append(output_split[i+1])
			arg1.append(output_split[i+2])
			arg2.append(output_split[i+3])
			output_split[i : i+4] = [' '.join(output_split[i:i+4])]
			expr.append(output_split[i])

	print("\nSynth solution:")
	print(output)
	print(next_event)

	print("Conditions: ")
	print(cond)
	print("Arg1: ")
	print(arg1)
	print("Arg2: ")
	print(arg2)
	print("Expr: ")
	print(expr)

	e_char = []
	[e_char.append(x) for x in arg1 if ('(ite' not in x)]
	[e_char.append(x) for x in arg2 if ('(ite' not in x)]

	e_int = np.unique([int(re.findall('[0-9]+',x)[0]) for x in e_char])
	print("next_event: ")
	print(e_int)

	last_ind = len(cond) - 1
	root = tree.Node(cond[last_ind])
	root.insert(expr[last_ind], expr, cond, arg1, arg2)

	temp = []
	expressions = []
	root.PrintTree(temp, expressions)

	dict_expr = {}
	for e in expressions:
		dict_expr.setdefault(e[0],[])
		dict_expr[e[0]].append(e[1:])
	print(dict_expr)

	for j in e_int:
		if j == 0:
			continue
		expr_split = dict_expr[str(j)]
		temp = []
		for x in expr_split:
			if len(x) > 1:
				temp.append('(' + ' and '.join(x) + ')')
			else:
				temp.append(x[0])
		if len(temp) > 1:
			final_expr = '(' + ' or '.join(temp) + ')'
		else:
			final_expr = temp[0]
		syn_event[str(j)] = final_expr

	return syn_event



def gen_syn(input_dict,trace_dict):

	trace_set = input_dict['trace_set']
	event_types = trace_dict['event_types']
	event_keys = trace_dict['event_keys']
	const_grammar = trace_dict['const_grammar']

	trace_events = dict.fromkeys(event_types.keys(),0)

	enum_val = {}

	for i in trace_set.keys():
		print('\n---------------' + i + '----------------')
		print(event_types[i])
		
		if not trace_set[i]:
			continue
		temp, idx = np.unique(trace_set[i],axis=0, return_index=True)
		temp = temp[np.argsort(idx)]
		temp = [list(x) for x in temp]

		last_ind = len(temp[0]) - 1
		value = []

		next_event = np.unique([x[last_ind] for x in temp if x[last_ind] != '-'])
		data_type = [x.split(':') for x in event_types[i][0]]
		enum_type = [x for x in data_type if x[1] == 'E']
		
		for x in enum_type:
			ind = data_type.index(x)
			values = [y[ind] for y in temp]
			if x[0] not in enum_val.keys():
				enum_val[x[0]] = []
			[enum_val[x[0]].append(y) for y in values]
			enum_val[x[0]] = list(np.unique(enum_val[x[0]]))

		if(const_grammar):
			if 'nil' in const_grammar:
				value = []
			else:
				for x in const_grammar:
					value.append(int(x))
		else:
			value = [1,2]
			for x in range(last_ind):
				if(data_type[x][1] == 'N'):
					data = [round(float(y[x])) for y in temp if y[last_ind]!=i]
					data, idx = np.unique(data,return_index=True)
					data = list(data[np.argsort(idx)])

					# intuition: for active learning, newly added trace
					# contains important info for predicate generation
					data.reverse()
					value.append(data[0]) 

					value.append(int(round(np.min(data))))
					value.append(int(round(np.mean(data))))
					value.append(int(round(np.max(data))))
					if(len(data) > 1):
						value.append(int(round(np.std(data))))

		value, idx = np.unique(value, return_index=True)
		value = list(value[np.argsort(idx)])

		if(len(next_event) == 0):
			continue

		trace_events[i] = dict.fromkeys(next_event,0)
		if len(next_event) == 1:
			continue
		for j in next_event:
			if j == i:
				continue

			f = open(full_path + 'aux_files/gen_event.sl','w')
			f.write("(set-logic LIA)\n")

			for x in enum_type:
				f.write("(declare-datatypes ((" + x[0] + "_t 0))\n")
				f.write("	((")
				for y in enum_val[x[0]]:
					f.write("(" + x[0] + y + ") ")
				f.write(")))\n")

			f.write("(synth-fun next (")

			for x in data_type:
				if x[1] == 'E':
					f.write("(" + x[0] + " " + x[0] + "_t)")
					continue
				f.write("(" + x[0] + " ")
				if(x[1] == 'N'):
					f.write("Int) ")
				elif(x[1] == 'S'):
					f.write("Bool) ")

			f.write(") Int\n")
			f.write("	((Start Int) ")

			num_data = [x for x in data_type if x[1] == 'N']
			if(num_data  or value):
				f.write(" (Var Int) ")

			for k in range(len(enum_type)):
				f.write("(EnumVar" + str(k) + " " + enum_type[k][0] + "_t) ")
			f.write("(StartBool Bool))\n")

			f.write("	((Start Int (\n")
			for k in range(2):
				f.write("				 " + str(k) + "\n")
			f.write("				 (ite StartBool Start Start)")
			f.write("))\n\n")

			if(num_data  or value):
				f.write("	(Var Int (\n")
				for x in value:
					if x < 0:
						f.write("				 (- " + str(x*-1) + ")\n")
					else:
						f.write("				 " + str(x) + "\n")

				for k in range(len(num_data)):
					f.write("			 " + num_data[k][0] + "\n")

				f.write("\
				(abs Var)						\n\
			 	(+ Var Var)						\n\
			 	(- Var Var)						\n\
			 	(* Var Var)")
				f.write("))\n\n")

			for k in range(len(enum_type)):
				f.write("(EnumVar" + str(k) + " " + enum_type[k][0] + "_t (\n")
				f.write("	" + enum_type[k][0] + "\n")
				for y in enum_val[enum_type[k][0]]:
					f.write(enum_type[k][0] + y + "\n")
				f.write("))\n\n") 


			f.write("	 (StartBool Bool (\n")
			bool_data = [x for x in data_type if x[1] == 'S']
			for k in range(len(bool_data)):
				f.write("				 	 " + bool_data[k][0] + "\n")

			for k in range(len(enum_type)):
				f.write("	 ( = EnumVar" + str(k) + " EnumVar" + str(k) + ")\n")
			
			if(num_data or value):
				f.write("\
					 (> Var Var)						\n\
					 (>= Var Var)						\n\
					 (< Var Var)						\n\
					 (<= Var Var)						\n\
					 (= Var Var)						\n")

			f.write("\
					 (and StartBool StartBool)			\n\
					 (or  StartBool StartBool)				\n\
					 (not StartBool)")

			f.write("))))\n\n")

			count = 0
			while(count < len(temp)):
				if(temp[count][last_ind] == '-'):
					count = count + 1
					continue;
				f.write("(constraint (= (next ")
				for x in range(last_ind):
					if(data_type[x][1] == 'N'):
						val = round(float(temp[count][x]))
						if val < 0:
							f.write("(- " + str(val*-1) + ") ")
						else:
							f.write(str(val) + " ")
					elif(data_type[x][1] == 'S'):
						if (temp[count][x] == 'true'):
							f.write("true ")
						else:
							f.write("false ")
					elif(data_type[x][1] == 'E'):
						f.write(data_type[x][0] + temp[count][x] + " ")
				if temp[count][last_ind] == j:
					f.write(") 1))\n")
				else:
					f.write(") 0))\n")

				count = count + 1
			f.write("\n(check-synth)\n")
			f.close()

			p = subprocess.Popen('cvc4 '+ full_path + 'aux_files/gen_event.sl', stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
			try:
				output,o_err = p.communicate(timeout=300)
				p.kill()
			except subprocess.TimeoutExpired:
				p.kill()
				print(colored("[WARNING] TIMEOUT",'magenta'))
				trace_events[i][j] = 0
				print(colored(j + ': ' + str(trace_events[i][j]),'green'))
				continue
			except subprocess.CalledProcessError:
				p.kill()
				print(colored("[ERROR] FAILED",'magenta'))
				trace_events[i][j] = '[ERROR] FAILED'
				print(colored(j + ': ' + trace_events[i][j],'green'))
				continue

			if 'unknown' in str(output):
				print(colored("[ERROR] FAILED",'magenta'))
				trace_events[i][j] = '[ERROR] FAILED'
				print(colored(j + ': ' + trace_events[i][j],'green'))
				continue

			output = str(output).replace('b\'unsat\\n','').replace('\'','')

			op_reg = '(define-fun next ('

			for x in data_type:
				op_reg = op_reg + '(' + x[0] + ' '
				if(x[1] == 'N'):
					op_reg = op_reg + 'Int)'
				elif(x[1] == 'S'):
					op_reg = op_reg + 'Bool)'
				elif(x[1] == 'E'):
					op_reg = op_reg + x[0] + '_t)'
				if(data_type.index(x) < len(data_type) - 1):
					op_reg = op_reg + ' '

			op_reg = op_reg + ') Int '
			output = output.replace(op_reg,'').replace(')\\n','')
			output = with_let(output)

			for k in range(len(enum_type)):
				for y in enum_val[enum_type[k][0]]:
					reg_rep_str = enum_type[k][0] + y
					output = output.replace(reg_rep_str,y)
					
			output = post_process(output,[0,1],data_type,trace_dict,enum_val)
			trace_events[i][j] = output['1']
			print(colored(j + ': ' + trace_events[i][j],'green'))

	return trace_events

def with_let(model):
	if '_let_' not in model:
		return model

	expr_list = model.split()

	ind1 = 0
	ind2 = 0
	open_count = 0
	close_count = 0
	found = False
	let_list = []
	for i in range(len(expr_list)):
		x = expr_list[i]
		if found:
			open_count += x.count('(')
			close_count += x.count(')')
			if open_count == close_count and open_count!=0:
				ind2 = i
				let_list.append(expr_list[ind1:ind2+1])
				found = False
				open_count = 0
				close_count = 0

		if '(_let_' in x:
			found = True
			ind1 = i
			open_count = x.count('(')

	model = model.replace('(let ','')
	let_pairs = []
	for let_expr_list in let_list:
		let_str = ' '.join(let_expr_list)
		model = model.replace(let_str,'')
		let_expr = re.findall('\_let\_[0-9]+',let_str)[0]
		replace_with = let_str.replace(let_expr_list[0] + ' ','')
		open_count = let_expr_list[0].count('(')
		replace_with = replace_with[:len(replace_with)-open_count]
		let_pairs.append([let_expr, replace_with])

	let_pairs.reverse()
	for pair in let_pairs:
		model = model.replace(pair[0],pair[1])
	model.strip()

	return model


def parse_args():
	parser = argparse.ArgumentParser()
	required_parse = parser.add_argument_group('required arguments')
	required_parse.add_argument('-i','--input_file', metavar = 'INPUT_FILENAME', required = True,
            help='Input trace file for data update predicate generation')
	parser.add_argument('-dv', '--dvar_list', metavar = 'EVENT_NAME DEPENDENT_VARIABLE_LIST', action='append', nargs='+', default=[],
            help='Variables that affect update variable behaviour. Use \'-dv help\' for possible options. Use -dv [all] <var_list> to set variables for all events')
	parser.add_argument('-c','--const', metavar = 'GRAMMAR_CONST', default=[], nargs='+',
            help='Constants to be added to grammar for SyGus CVC4. By default uses average and std dev values\
            of the constants in trace. Use "nil" to not use any constants.')

	hyperparams = parser.parse_args()
	return hyperparams

def main():

	hyperparams = parse_args()
	print(hyperparams)

	trace_filename = hyperparams.input_file
	var_list = hyperparams.dvar_list
	const_grammar = hyperparams.const

	f = open(trace_filename,'r')
	events_raw = f.readlines()
	f.close()

	events = [x.replace('\n','') for x in events_raw]
	events_split = [x.split() for x in events]

	trace_id = [ind for ind in range(len(events_split)) if events_split[ind][0] == 'trace']
	type_id = [ind for ind in range(len(events_split)) if events_split[ind][0] == 'types']

	event_types = dict.fromkeys([events_split[ind][0] for ind in range(type_id[0]+1,trace_id[0])],0)
	event_keys = list(event_types.keys())

	for i in event_types:
		temp = []
		temp = [events_split[ind][1:] for ind in range(type_id[0]+1,trace_id[0]) if events_split[ind][0] == i]
		event_types[i] = temp

	for x in var_list:
		if(x[0] == '[all]'):
			for y in event_types:
				if(any(True for z in x[1:] if z not in event_types[y][0])):
					print(colored("\n[ERROR] Wrong dependent variable option",'red'))
					print(colored("[HELP]",'green') + " Possible options:")
					print(event_types)
					exit()
		elif(any(True for y in x[1:] if y not in event_types[x[0]][0])):
			print(colored("\n[ERROR] Wrong dependent variable option",'red'))
			print(colored("[HELP]",'green') + " Possible options:")
			print(event_types)
			exit()

	if(['help'] in var_list):
		print(colored("[HELP]",'green') + " Possible options:")
		print(event_types)
		exit(0)

	if(var_list):
		if(var_list[0][0] == '[all]'):
			temp_list = []
			for i in event_keys:
				temp = [i]
				for j in var_list[0][1:]:
					temp.append(j)
				temp_list.append(temp)
			var_list = temp_list

		print(var_list)
		var_list_ind = dict.fromkeys(event_keys,0)
		for i in var_list:
			temp = []
			temp = [event_types[i[0]][0].index(x) + 1 for x in event_types[i[0]][0] if x in i[1:]]
			temp = list(np.unique(temp))
			var_list_ind[i[0]] = [0] + temp

		print(var_list_ind)
		temp = []
		for x in events_split:
			if(len(x) > 1):
				if(var_list_ind[x[0]] == 0):
					temp.append(x)
				else:
					temp.append([x[ind] for ind in var_list_ind[x[0]]])
			else:
				temp.append([x[0]])

		events_split = temp

		trace_id = [ind for ind in range(len(events_split)) if events_split[ind][0] == 'trace']
		type_id = [ind for ind in range(len(events_split)) if events_split[ind][0] == 'types']

		event_types = dict.fromkeys([events_split[ind][0] for ind in range(type_id[0]+1,trace_id[0])],0)
		event_keys = list(event_types.keys())

		for i in event_types:
			temp = []
			temp = [events_split[ind][1:] for ind in range(type_id[0]+1,trace_id[0]) if events_split[ind][0] == i]
			event_types[i] = temp

	trace_dict = {'trace_id':trace_id, 'type_id':type_id, 'event_keys':event_keys, 'event_types':event_types, 'const_grammar':const_grammar}

	f = open(trace_filename.replace('.txt','') + '_events.txt','w')
	temp = events_split[trace_id[0]+1:]

	if(len(temp) < 2):
		f.write("start\n")
		f.close()
		exit(0)

	input_dict = pre_process(temp)
	trace_events = gen_syn(input_dict,trace_dict)
	
	# uncomment to dump generated predicates into '.pkl' file
	pickle_f = open(trace_filename.replace('.txt','.pkl'),'wb')
	pickle.dump(trace_events,pickle_f)
	pickle_f.close()

	print('\n')
	print(colored(trace_events,'green'))

	f.write("start\n")
	for j in range(1,len(temp)):
		if temp[j-1][0] == 'trace':
			continue
		if temp[j][0] == 'trace':
			f.write("start\n")
			continue
		if temp[j][0] == temp[j-1][0]:
			f.write(temp[j][0] + '\n')
			continue
		if(trace_events[temp[j-1][0]] == 0):
			f.write(temp[j][0] + '\n')
		elif(trace_events[temp[j-1][0]][temp[j][0]] == 0):
			f.write(temp[j][0] + '\n')
		else:
			f.write(trace_events[temp[j-1][0]][temp[j][0]] + ': ' + temp[j][0] + '\n')
	f.close()

full_path = abspath(__file__).replace('syn_next_event.py','')
if __name__ == '__main__':
	start_time = time.time()
	main()
	end_time = time.time()
	print('\n\nTime taken: ' + str(end_time - start_time))
