#######################################################################
# Generate predicates for data update across transitions
# Author: Natasha Yogananda Jeppu, natasha.yogananda.jeppu@cs.ox.ac.uk
#         University of Oxford
#######################################################################


import numpy as np
import subprocess
import time
import argparse
import re
import statistics as stat

from os.path import abspath
from termcolor import colored

def pre_process(trace,trace_dict):
	var_list_ind = trace_dict['var_list_ind']
	event_types = trace_dict['event_types']
	update_var = trace_dict['update_var']
	trace_set = dict.fromkeys([i[0] for i in trace if i[0] != 'trace'],0)

	for j in range(len(trace)-1):
		if trace[j][0] == 'trace' or trace[j+1][0] == 'trace':
			continue

		update_ind = event_types[trace[j+1][0]][0].index(update_var) + 1
		if trace[j+1][update_ind] == '-':
			continue
		if any([trace[j][ind] == '-' for ind in var_list_ind[trace[j][0]]]):
			continue
		
		if(var_list_ind[trace[j][0]] == 0):
			temp1 = trace[j][1:]
		else:
			temp1 = []
			x = trace[j]
			[temp1.append(x[ind]) for ind in var_list_ind[trace[j][0]]]

		temp1.append(trace[j+1][update_ind])
		if(trace_set[trace[j][0]] == 0):
			trace_set[trace[j][0]] = [temp1]
		else:
			trace_set[trace[j][0]].append(temp1)

	input_dict = {'trace_set': trace_set}

	return input_dict

def syn_int_file(input_syn,j,enum_val):
	temp = input_syn['input']
	data_type = input_syn['data_type']
	value = input_syn['value']
	window = input_syn['window']

	last_ind = len(temp[0]) - 1

	enum_type = list(enum_val.keys())

	f = open(full_path + 'aux_files/gen_event_update_enum.sl','w')
	f.write("(set-logic LIA)\n")

	for type_t in data_type:
		if type_t[1] == 'E':
			f.write("(declare-datatypes ((" + type_t[0] + "_t 0))\n")
			f.write("	((")
			for i in range(len(enum_val[type_t[0]])):
				y = enum_val[type_t[0]][i]
				f.write("(" + type_t[0] + y + ")")
				if i < len(enum_val[type_t[0]])-1:
					f.write(" ")
			f.write(")))\n")

	f.write("\n(synth-fun next (")

	for i in range(len(data_type)):
		x = data_type[i]
		f.write("(" + x[0] + " ")
		if(x[1] == 'N'):
			f.write("Int)")
		elif(x[1] == 'S'):
			f.write("Bool)")
		elif(x[1] == 'E'):
			f.write(x[0] + "_t)")
		if i < len(data_type)-1:
			f.write(" ")

	f.write(") Int\n")
	f.write("\n	((Start Int) (StartBool Bool)")
	for k in range(len(enum_type)):
		f.write(" (EnumVar" + str(k) + " " + enum_type[k] + "_t)")
	f.write(")\n")

	f.write("\n	((Start Int (\n")

	num_data = [x for x in data_type if x[1] == 'N']
	for k in range(len(num_data)):
		f.write("		" + num_data[k][0] + "\n")

	for x in value:
		if x < 0:
			f.write("		(- " + str(x*-1) + ")\n")
		else:
			f.write("		" + str(x) + "\n")

	f.write("\
		(abs Start) 							\n\
		(+ Start Start)						\n\
		(- Start Start)						\n\
		(* Start Start)\n")

	f.write("		(ite StartBool Start Start)")
	f.write("))\n\n")

	f.write("	 (StartBool Bool (\n")
	bool_data = [x for x in data_type if x[1] == 'S']
	for k in range(len(bool_data)):
		f.write("			" + bool_data[k][0] + "\n")

	for k in range(len(enum_type)):
		f.write("	 	(= EnumVar" + str(k) + " EnumVar" + str(k) + ")\n")

	f.write("\
		(> Start Start)						\n\
		(>= Start Start)						\n\
		(< Start Start)						\n\
		(<= Start Start)						\n\
		(= Start Start)						\n\
		(and StartBool StartBool)			\n\
		(or  StartBool StartBool)				\n\
		(not StartBool)))\n\n")

	for k in range(len(enum_type)):
		f.write("	(EnumVar" + str(k) + " " + enum_type[k] + "_t (\n")
		f.write("		" + enum_type[k] + "\n")
		for i in range(len(enum_val[enum_type[k]])):
			y = enum_val[enum_type[k]][i]
			f.write("		" + enum_type[k] + y)
			if i < len(enum_val[enum_type[k]])-1:
				f.write("\n")
		f.write("))\n\n")

	f.write("))\n\n")

	count = 0
	while(count < window):
		if(j+count == len(temp)):
			break;
		f.write("(constraint (= (next ")
		for x in range(last_ind):
			if(data_type[x][1] == 'N'):
				val = round(float(temp[j+count][x]))
				if val < 0:
					f.write("(- " + str(val*-1) + ") ")
				else:
					f.write(str(val) + " ")
			elif(data_type[x][1] == 'S'):
				f.write(temp[j+count][x] + " ")
			elif(data_type[x][1] == 'E'):
				f.write(data_type[x][0] + temp[j+count][x] + ' ')

		val = round(float(temp[j+count][last_ind]))
		if val < 0:
			f.write(") (- " + str(val*-1) + ")))\n")
		else:
			f.write(") " + str(val) + "))\n")

		count = count + 1
			 
	f.write("\n(check-synth)\n")

	f.close()


def syn_bool_file(input_syn,j,enum_val):
	temp = input_syn['input']
	data_type = input_syn['data_type']
	value = input_syn['value']
	window = input_syn['window']

	last_ind = len(temp[0]) - 1

	enum_type = list(enum_val.keys())

	f = open(full_path + 'aux_files/gen_event_update_enum.sl','w')
	f.write("(set-logic LIA)\n")

	for type_t in data_type:
		if type_t[1] == 'E':
			f.write("(declare-datatypes ((" + type_t[0] + "_t 0))\n")
			f.write("	((")
			for i in range(len(enum_val[type_t[0]])):
				y = enum_val[type_t[0]][i]
				f.write("(" + type_t[0] + y + ")")
				if i < len(enum_val[type_t[0]])-1:
					f.write(" ")
			f.write(")))\n")

	f.write("\n(synth-fun next (")

	for i in range(len(data_type)):
		x = data_type[i]
		f.write("(" + x[0] + " ")
		if(x[1] == 'N'):
			f.write("Int)")
		elif(x[1] == 'S'):
			f.write("Bool)")
		elif(x[1] == 'E'):
			f.write(x[0] + "_t)")
		if i < len(data_type)-1:
			f.write(" ")

	f.write(") Bool\n")
	f.write("	((Start Bool) (Start_Int Int)")
	for k in range(len(enum_type)):
		f.write(" (EnumVar" + str(k) + " " + enum_type[k] + "_t)")
	f.write(")\n")

	f.write("	((Start Bool (\n")
	f.write("		true\n")
	f.write("		false\n")
	bool_data = [x for x in data_type if x[1] == 'S']
	for k in range(len(bool_data)):
		f.write("		" + bool_data[k][0] + "\n")

	for k in range(len(enum_type)):
		f.write("	 	(= EnumVar" + str(k) + " EnumVar" + str(k) + ")\n")

	f.write("\
		(> Start_Int Start_Int)						\n\
		(>= Start_Int Start_Int)						\n\
		(< Start_Int Start_Int)						\n\
		(<= Start_Int Start_Int)						\n\
		(= Start_Int Start_Int)						\n\
		(and Start Start)			\n\
		(or  Start Start)				\n\
		(not Start)))\n\n")

	f.write("	 (Start_Int Int (\n")
	num_data = [x for x in data_type if x[1] == 'N']
	for k in range(len(num_data)):
		f.write("		" + num_data[k][0] + "\n")

	for x in value:
		if x < 0:
			f.write("		(- " + str(x*-1) + ")\n")
		else:
			f.write("		" + str(x) + "\n")

	f.write("\
		(abs Start_Int)								\n\
		(+ Start_Int Start_Int)						\n\
		(- Start_Int Start_Int)						\n\
		(* Start_Int Start_Int)\n")

	f.write("		(ite Start Start_Int Start_Int)))\n\n")

	for k in range(len(enum_type)):
		f.write("	 (EnumVar" + str(k) + " " + enum_type[k] + "_t (\n")
		f.write("		" + enum_type[k] + "\n")
		for i in range(len(enum_val[enum_type[k]])):
			y = enum_val[enum_type[k]][i]
			f.write("		" + enum_type[k] + y)
			if i < len(enum_val[enum_type[k]])-1:
				f.write("\n")
		f.write("))\n\n")
	
	f.write("))\n\n")

	count = 0
	while(count < window):
		if(j+count == len(temp)):
			break;
		f.write("(constraint (= (next ")
		for x in range(last_ind):
			if(data_type[x][1] == 'N'):
				val = round(float(temp[j+count][x]))
				if val < 0:
					f.write("(- " + str(val*-1) + ") ")
				else:
					f.write(str(val) + " ")
			elif(data_type[x][1] == 'S'):
				f.write(str(temp[j+count][x]) + ' ')
			elif(data_type[x][1] == 'E'):
				f.write(data_type[x][0] + temp[j+count][x] + ' ')

		f.write(") " + str(temp[j+count][last_ind]) + "))\n")

		count = count + 1
			 
	f.write("\n(check-synth)\n")

	f.close()

def syn_enum_file(input_syn,j,enum_val,update_var):
	temp = input_syn['input']
	data_type = input_syn['data_type']
	value = input_syn['value']
	window = input_syn['window']

	last_ind = len(temp[0]) - 1

	enum_type = list(enum_val.keys())

	f = open(full_path + 'aux_files/gen_event_update_enum.sl','w')
	f.write("(set-logic LIA)\n\n")

	for type_t in data_type:
		if type_t[1] == 'E':
			f.write("(declare-datatypes ((" + type_t[0] + "_t 0))\n")
			f.write("	((")
			for y in enum_val[type_t[0]]:
				f.write("(" + type_t[0] + y + ") ")
			f.write(")))\n")

	if update_var.split(':') not in data_type:
		type_t = update_var.split(':')
		f.write("(declare-datatypes ((" + type_t[0] + "_t 0))\n")
		f.write("	((")
		for y in enum_val[type_t[0]]:
			f.write("(" + type_t[0] + y + ") ")
		f.write(")))\n")

	f.write("\n(synth-fun next (")

	for x in data_type:
		if x[1] == 'E':
			f.write("(" + x[0] + " " + x[0] + "_t) ")
			continue
		f.write("(" + x[0] + " ")
		if(x[1] == 'N'):
			f.write("Int) ")
		elif(x[1] == 'S'):
			f.write("Bool) ")

	f.write(") " + update_var.split(':')[0] + "_t\n")
	f.write("	((Start " + update_var.split(':')[0] + "_t) ")
	for k in range(len(enum_type)):
		if enum_type[k] == update_var.split(':')[0]:
			continue
		f.write("(EnumVar" + str(k) + " " + enum_type[k] + "_t) ")
	f.write("(StartBool Bool) (Start_Int Int))\n")

	f.write("	((Start " + update_var.split(':')[0] + "_t (\n")
	if update_var.split(':') in data_type:
		f.write("				" + update_var.split(':')[0] + "\n")
	for y in enum_val[update_var.split(':')[0]]:
		f.write("				" + update_var.split(':')[0] + y + "\n")
	f.write("				(ite StartBool Start Start)))\n\n")


	for k in range(len(enum_type)):
		if enum_type[k] == update_var.split(':')[0]:
			continue
		f.write("	(EnumVar" + str(k) + " " + enum_type[k] + "_t (\n")
		f.write("				" + enum_type[k] + "\n")
		for y in enum_val[enum_type[k]]:
			f.write("				" + enum_type[k] + y + "\n")
		f.write("				(ite StartBool EnumVar" + str(k) + " EnumVar" + str(k) + ")))\n\n") 


	f.write("	(StartBool Bool (\n")
	f.write("				true\n")
	f.write("				false\n")
	f.write("				(= Start Start)\n")
	for k in range(len(enum_type)):
		if enum_type[k] == update_var.split(':')[0]:
			continue
		f.write("	 			(= EnumVar" + str(k) + " EnumVar" + str(k) + ")\n")

	bool_data = [x for x in data_type if x[1] == 'S']
	for k in range(len(bool_data)):
		f.write("			 	" + bool_data[k][0] + "\n")

	f.write("\
				(>= Start_Int Start_Int)\n\
				(<= Start_Int Start_Int)\n\
				(and StartBool StartBool)\n\
				(or  StartBool StartBool)\n\
				(not StartBool)")
	f.write("))\n\n")


	f.write("	(Start_Int Int (\n")
	f.write("\
				(abs Start_Int)		\n\
				(+ Start_Int Start_Int)\n\
				(- Start_Int Start_Int)\n\
				(* Start_Int Start_Int)\n")

	num_data = [x for x in data_type if x[1] == 'N']
	for k in range(len(num_data)):
		f.write("				" + num_data[k][0] + "\n")

	for x in value:
		f.write("				" + str(x) + "\n")

	f.write("				(ite StartBool Start_Int Start_Int)")


	
	f.write("))))\n\n")

	for x in data_type:
		if(x[1] == 'E'):
			f.write("(declare-var " + x[0] + " " + x[0] + "_t)\n")
		if(x[1] == 'N'):
			f.write("(declare-var " + x[0] + " Int)\n")
		elif(x[1] == 'S'):
			f.write("(declare-var " + x[0] + " Bool)\n")

	f.write("\n\n")

	count = 0
	while(count < window):
		if(j+count == len(temp)):
			break;
		f.write("(constraint (= (next ")
		for x in range(last_ind):
			if(data_type[x][1] == 'N'):
				f.write(str(round(float(temp[j+count][x]))) + " ")
			elif(data_type[x][1] == 'S'):
				f.write(str(temp[j+count][x]) + ' ')
			elif(data_type[x][1] == 'E'):
				f.write(data_type[x][0] + temp[j+count][x] + ' ')

		f.write(") " + update_var.split(':')[0] + temp[j+count][last_ind] + "))\n")

		count = count + 1
			 
	f.write("\n(check-synth)\n")

	f.close()

def gen_syn(input_dict,trace_dict):
	trace_set = input_dict['trace_set']
	event_types = trace_dict['event_types']
	event_keys = trace_dict['event_keys']
	update_var = trace_dict['update_var']
	synth_tool = trace_dict['synth_tool']
	const_grammar = trace_dict['const_grammar']
	var_list_ind = trace_dict['var_list_ind']

	trace_events = dict.fromkeys(event_types.keys(),0)

	enum_val = {}

	for i in trace_set.keys():
		event = []
		temp = trace_set[i]

		if(temp == 0):
			continue

		last_ind = len(temp[0]) - 1

		print(i)
		print(event_types[i])

		next_event = np.unique([x[last_ind] for x in temp])
		data_type = [event_types[i][0][ind].split(':') for ind in range(len(event_types[i][0])) if ind+1 in var_list_ind[i]]
		
		enum_type = [x for x in data_type if x[1] == 'E']

		for x in enum_type:
			ind = data_type.index(x)
			values = [y[ind] for y in temp]
			if x == update_var.split(':'):
				[values.append(y[last_ind]) for y in temp]
			if x[0] not in enum_val.keys():
				enum_val[x[0]] = []
			[enum_val[x[0]].append(y) for y in values]
			enum_val[x[0]] = list(np.unique(enum_val[x[0]]))

		if update_var.split(':')[1] == 'E' and update_var.split(':') not in enum_type:
			values = []
			[values.append(y[last_ind]) for y in temp]
			enum_val[update_var.split(':')[0]] = list(np.unique(values))


		if(const_grammar):
			if 'nil' in const_grammar:
				value = []
			else:
				value = []
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
		
		j=0
		found = True
		while(j <= len(temp)):
			predicates = {'1':0, '2':0, '3':0, '4':0, '5':0, '6':0, '7':0, '8':0, '9':0, '10':0};
			for window in range(2,10):
				if not found and len(event_keys) == 1:
					window = 1
				if(len(event_keys) != 1):
					window = len(temp)

				input_syn = {'input':temp, 'data_type':data_type, 'value':value, 'window':window}
				
				if(update_var.split(':')[1] == 'N'):
					syn_int_file(input_syn, j, enum_val)
				elif(update_var.split(':')[1] == 'S'):
					syn_bool_file(input_syn, j, enum_val)
				elif(update_var.split(':')[1] == 'E'):
					syn_enum_file(input_syn, j, enum_val, update_var)

				if(synth_tool == 'cvc4'):
					p = subprocess.Popen('cvc4 ' + full_path + 'aux_files/gen_event_update_enum.sl', shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
				elif(synth_tool == 'fastsynth'):
					p = subprocess.Popen('fastsynth ' + full_path + 'aux_files/gen_event_update_enum.sl', shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

				try:
					output, o_err = p.communicate(timeout = 15)
					if(synth_tool == 'fastsynth'):
						output = str(output).split('\\n')
					p.kill()
				except subprocess.TimeoutExpired:
					p.kill()
					print(colored("[WARNING] TIMEOUT",'magenta'))
					if(len(event_keys) != 1):
						event.append('[WARNING] TIMEOUT')
						break
					else:
						continue
				except subprocess.CalledProcessError:
					p.kill()
					print(colored("[WARNING] FAILED",'magenta'))
					exit(0)
					continue

				if(synth_tool == 'cvc4'):
					if 'unknown' in str(output):
						if(len(event_keys) != 1):
							event.append('[WARNING] UNKNOWN')
							j = j + window + 1
						elif not found:
							print(colored("[WARNING] unknown",'magenta'))
							event.append('[WARNING] UNKNOWN')
							j = j + 1
						break	

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

					if(update_var.split(':')[1] == 'N'):
						op_reg = op_reg + ') Int '
					elif(update_var.split(':')[1] == 'S'):
						op_reg = op_reg + ') Bool '
					elif(update_var.split(':')[1] == 'E'):
						op_reg = op_reg + ') ' + update_var.split(':')[0] + '_t '

					output = output.replace(op_reg,'').replace(')\\n','')
					output = with_let(output)
					update_ind = event_types[i][0].index(update_var)
					update_var_data_t = event_types[i][0][update_ind].split(':')
					if(len(event_keys) != 1):
						event.append(update_var_data_t[0] + '\' = ' + output)
						j = j + window + 1
						break
					predicates[str(window)] = update_var_data_t[0] + '\' = ' + output

				elif(synth_tool == 'fastsynth'):
					sol_found = [i for i in output if('SMT: synth_fun::next -> ' in i)]
					if not sol_found:
						if not found:
							print(colored("[WARNING] unknown",'magenta'))
							event.append('[WARNING] UNKNOWN')
							j = j + 1
						break

					expr = [i for i in output if('SMT: synth_fun::next -> ' in i)][0]
					temp_expr_simple = expr.replace('SMT: synth_fun::next -> ','')
					for x in data_type:
						temp_replace = temp_expr_simple.replace('|synth::parameter' + str(data_type.index(x)) + '|',x[0])
						temp_expr_simple = temp_replace
					update_ind = event_types[i][0].index(update_var)
					update_var_data_t = event_types[i][0][update_ind].split(':')
					if(len(event_keys) != 1):
						event.append(update_var_data_t[0] + '\' = ' + temp_expr_simple)
						j = j + window + 1
						break
					predicates[str(window)] = update_var_data_t[0] + '\' = ' + temp_expr_simple

			if(len(event_keys) != 1):
				break

			if not found:
				found = True
				event.append(predicates[str(1)])
				j = j + 1
				continue

			p_complexity = [len(predicates[x]) for x in predicates.keys() if predicates[x] != 0]
			if not p_complexity:
				found = False
				continue

			min_comp = min(p_complexity)
			p_ind = 2 + max([ind for ind in range(len(p_complexity)) if p_complexity[ind] == min_comp])

			if (j + p_ind) > len(temp):
				length = len(temp) - j
			else:
				length = p_ind
			j = j + p_ind

			[event.append(predicates[str(p_ind)]) for x in range(length)]

		trace_events[i] = event
		print(colored(event,'green'))

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
	model = model[:-1]
	return model


def parse_args():
	parser = argparse.ArgumentParser()
	required_parse = parser.add_argument_group('required arguments')
	required_parse.add_argument('-i','--input_file', metavar = 'INPUT_FILENAME', required=True,
            help='Input trace file for data update predicate generation')
	required_parse.add_argument('-v', '--var', metavar = 'UPDATE_VAR', required=True,
            help='Variable for data update predicate synthesis. Use \'-v help\' for possible options')
	parser.add_argument('-dv', '--dvar_list', metavar = 'EVENT_NAME DEPENDENT_VARIABLE_LIST', action='append', nargs='+', default=[],
            help='Variables that affect update variable behaviour. Use \'-dv help\' for possible options. Use -dv [all] <var_list> to set variables for all events')
	# fastsynth: does not support latest SMT lib format
	# parser.add_argument('-s','--synth_tool', metavar = 'SYNTHESIS_TOOL', default='fastsynth', choices = ['cvc4','fastsynth'],
 #            help='Synthesis tool for predicate generation: fastsynth or cvc4')
	parser.add_argument('-c','--const', metavar = 'GRAMMAR_CONST', default=[], nargs='+',
            help='Constants to be added to grammar for SyGus CVC4')

	hyperparams = parser.parse_args()
	return hyperparams



def main():

	hyperparams = parse_args()
	print(hyperparams)

	trace_filename = hyperparams.input_file
	update_var = hyperparams.var
	var_list = hyperparams.dvar_list
	const_grammar = hyperparams.const

	# fastsynth: does not support latest SMT lib format
	# synth_tool = hyperparams.synth_tool
	synth_tool = 'cvc4'
	

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

	for x in event_types:
		if(update_var not in event_types[x][0]):
			print(colored("\n[ERROR] Wrong update variable option",'red'))
			print(colored("[HELP]",'green') + " Possible options:")
			print(event_types)
			exit()

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

	if(['help'] in var_list or 'help' in update_var):
		print(colored("[HELP]",'green') + " Possible options:")
		print(event_types)
		exit()

	if(var_list):
		if(var_list[0][0] == '[all]'):
			temp_list = []
			for i in event_keys:
				temp = [i]
				temp = temp + var_list[0][1:]
				temp_list.append(temp)
			var_list = temp_list

		var_list_ind = dict.fromkeys(event_keys,0)
		for var in var_list:
			event = var[0]
			temp = []
			temp = [event_types[event][0].index(x) + 1 for x in event_types[event][0] if x in var[1:]]
			temp = list(np.unique(temp))
			var_list_ind[event] = temp
	else:
		var_list_ind = dict.fromkeys(event_keys,0)
		for i in event_types:
			var_list_ind[i] = list(range(1, len(event_types[i][0])+1))

	trace_dict = {'trace_id':trace_id, 'type_id':type_id, 'event_keys':event_keys, 'event_types':event_types, 'update_var':update_var, 'var_list_ind':var_list_ind, 'synth_tool':synth_tool, 'const_grammar':const_grammar}

	f = open(trace_filename.replace('.txt','') + '_events.txt','w')
	f.close()

	if(len(event_keys) == 1):
		for i in range(len(trace_id) - 1):
			f = open(trace_filename.replace('.txt','') + '_events.txt','a')
			trace = events_split[trace_id[i]+1:trace_id[i+1]]
			print("----------- Trace: " + str(i+1) + " ------\n" )
			if(not trace):
				continue

			if(len(trace) < 2):
				f.close()
				continue

			input_dict = pre_process(trace,trace_dict)
			trace_events = gen_syn(input_dict,trace_dict)
			print(colored(trace_events,'green'))

			if i == 0:
				f.write("start\n")

			update_var = trace_dict['update_var']
			pred_ind = 0
			for j in range(len(trace)-1):
				update_ind = event_types[trace[j+1][0]][0].index(update_var) + 1
				if trace[j+1][update_ind] == '-':
					f.write("[WARNING] MISSING\n")
					continue
				if any([trace[j][ind] == '-' for ind in var_list_ind[trace[j][0]]]):
					f.write("[WARNING] MISSING\n")
					continue
				f.write(trace_events[event_keys[0]][pred_ind] + '\n')
				pred_ind = pred_ind + 1

			assert(pred_ind == len(trace_events[event_keys[0]]))

			f.write("start\n") 
			f.close()
	else:
		f = open(trace_filename.replace('.txt','') + '_events.txt','w')
		f.close()

		f = open(trace_filename.replace('.txt','') + '_events.txt','a')
		trace = events_split[trace_id[0]+1:trace_id[len(trace_id)-1]]

		if(not trace):
			f.close()
			exit(0)

		if(len(trace) < 2):
			f.write("start\n")
			f.write(trace[0][0] + '\n')
			f.close()
		else:
			input_dict = pre_process(trace,trace_dict)
			trace_events = gen_syn(input_dict,trace_dict)
			print(colored(trace_events,'green'))

			f.write("start\n")
			update_var = trace_dict['update_var']
			for j in range(len(trace)-1):
				if trace[j][0] == 'trace':
					f.write("start\n")
					continue

				if trace[j+1][0] != 'trace':
					update_ind = event_types[trace[j+1][0]][0].index(update_var) + 1
					if trace[j+1][update_ind] == '-':
						f.write("[WARNING] MISSING\n")
						continue
					if any([trace[j][ind] == '-' for ind in var_list_ind[trace[j][0]]]):
						f.write("[WARNING] MISSING\n")
						continue

				if(trace_events[trace[j][0]] == 0):
					f.write(trace[j][0] + '\n')
					continue
				if(trace_events[trace[j][0]][0] == ''):
					f.write(trace[j][0] + '\n')
				else:
					f.write(trace[j][0] + ', ' + trace_events[trace[j][0]][0] + '\n')

			f.write("start\n") 
			f.close()

full_path = abspath(__file__).replace('syn_event_update.py','')
if __name__ == '__main__':
	start_time = time.time()
	main()
	end_time = time.time()
	print('\n\nTime taken: ' + str(end_time - start_time))


