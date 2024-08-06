#######################################################################
# Model learning from trace data
# Author: Natasha Yogananda Jeppu, natasha.yogananda.jeppu@cs.ox.ac.uk
#         University of Oxford
#######################################################################

import sys
import re
import random
import numpy as np
import os
import json
import time
import itertools

# for displaying model
from transitions import *
from transitions.extensions import GraphMachine
from pygraphviz import *

from os.path import abspath
from termcolor import colored

import argparse
import copy

def text_preprocess(start_index,hyperparams,var,len_done=-1):
    if start_index != -1:
        events_tup_to_list = var['events_tup_to_list_internal']
    o_event_uniq = var['o_event_uniq']

    len_seq = hyperparams.window

    if start_index == -1:
        events = var['org_trace']
    elif len_done != -1:
        events = events_tup_to_list[start_index][len_done:]
    else:
        events = events_tup_to_list[start_index]
    event_uniq = list(set(events))
    event_uniq.sort(key=events.index)

    if o_event_uniq:
        for e in event_uniq:
            if e not in o_event_uniq:
                o_event_uniq.append(e)
        event_uniq = o_event_uniq

    event_id = []

    for i in range(len(events)):
        event_id.append(event_uniq.index(events[i])+1)

    seq_input = [];
    if(len(events) < len_seq):
        seq_input = [event_id]
    for i in range(len(events)-len_seq+1):
        ind = i
        seq_input.append(event_id[ind:ind+len_seq])

    seq_input_uniq,u_ind = np.unique(seq_input,return_index=True,axis=0)
    seq_input_uniq = seq_input_uniq[np.argsort(u_ind)]

    if start_index != -1:
        print("\n\n\n******************************************** Iteration:" + str(start_index))
        print(events)

        if ((len(seq_input_uniq)*len_seq) > len(event_id)):
            print(colored('[WARNING] Using non segmented','magenta'))
            seq_input_uniq = [event_id]
        else:
            print(colored('[WARNING] Using segmented','magenta'))

    input_dict = {'event_id':event_id, 'seq_input_uniq':seq_input_uniq, 'event_uniq':event_uniq, 'len_seq':len_seq}
    return input_dict


def gen_c_model(trace_input,constraint,num_states,start_index,init_model,var):

    seq_input_uniq = trace_input['seq_input_uniq']
    event_uniq = trace_input['event_uniq']
    incr = var['incr']

    f = open(C_gen_model,'w')

    data_type = {'seq' : 'uint16_t' if len(seq_input_uniq[0]) > 255 else 'uint8_t',
                 'num_seq' : 'uint16_t' if len(seq_input_uniq) > 255 else 'uint8_t',
                 'e_uniq' : 'uint16_t' if len(event_uniq) > 255 else 'uint8_t',
                 'num_states' : 'uint16_t' if num_states > 255 else 'uint8_t',
                 'init_model' : 'uint16_t' if len(init_model) > 255 else 'uint8_t',
                 'count' : 'uint16_t' if (len(seq_input_uniq[0])*len(seq_input_uniq)+len(init_model)) > 255 else 'uint8_t'}

    f.write("// ******************************************** Iteration:" + str(start_index) + "\n\n")
    f.write("#include<stdio.h>\n#include<stdbool.h>\n#include<stdint.h>\nvoid main()\n{\n\
    " + data_type['seq'] + " event_seq_length = " + str(len(seq_input_uniq[0])) + ";\n")

    f.write("   " + data_type['num_seq'] + " num_input = " + str(len(seq_input_uniq)) + ";\n")
    f.write("   " + data_type['e_uniq'] + " event_seq[" + str(len(seq_input_uniq)) + "][" + str(len(seq_input_uniq[0])) + "] = ")
    f.write("{")

    for i in range(len(seq_input_uniq)):
        f.write("{")
        j = -1
        for j in range(len(seq_input_uniq[0])-1):
            f.write("%d," %seq_input_uniq[i][j])
        j = j + 1
        f.write(str(seq_input_uniq[i][j]))
        if (i == len(seq_input_uniq)-1):
            f.write("}")
        else:
            f.write("},")
    f.write("};\n")

    
    f.write("   " + data_type['num_states'] + " num_states = " + str(num_states) + ";\n")
    f.write("   " + (data_type['num_states'] if num_states > len(event_uniq) else data_type['e_uniq']) + " t[" + str(len(seq_input_uniq[0])*len(seq_input_uniq)+len(init_model)) + "][3];\n")

    f.write("   " + data_type['count'] + " count=0;\n")

    if incr:
        f.write("   " + (data_type['num_states'] if num_states > len(event_uniq) else data_type['e_uniq']) + " t_gen[" + str(len(init_model)) + "][3] = ");
        f.write("{")

        for i in range(len(init_model)):
            f.write("{")
            for j in range(len(init_model[i])-1):
                f.write("%d," %init_model[i][j])
            j = j + 1
            f.write(str(init_model[i][j]))
            if (i == len(init_model)-1):
                f.write("}")
            else:
                f.write("},")
        f.write("};\n\n")

        f.write("   for(" + data_type['init_model'] + " i=0;i<" + str(len(init_model)) + ";i++)             \n\
        {                                                                       \n\
            t[count][0] = t_gen[i][0];                                          \n\
            t[count][1] = t_gen[i][1];                                          \n\
            t[count][2] = t_gen[i][2];                                  \n\
            count = count + 1;                                                  \n\
        }\n")

    f.write("                                                                       \n\
    for (" + data_type['num_seq'] + " i=0;i<num_input;i++)                                                  \n\
    {                                                                               \n\
        " + data_type['num_states'] + " start_state_var;                                                        \n\
        __CPROVER_assume(start_state_var <= num_states && start_state_var > 1);     \n\
        //assert(start_state_var <= num_states);                                        \n\n\
        t[count][0] = start_state_var;                                              \n\
        for (" + data_type['seq'] + " j=0;j<event_seq_length;j++)                                       \n\
        {                                                                           \n\
            " + data_type['num_states'] + " next_state_var;                                                     \n\
            t[count][1] = event_seq[i][j];                                          \n\
            if(event_seq[i][j] == 1)                                                \n\
                t[count][0] = 1;                                                    \n\
            __CPROVER_assume(next_state_var <= num_states && next_state_var > 1);   \n\
            //assert(next_state_var <= num_states);                                 \n\
            t[count][2] = next_state_var;                                           \n\
            count = count+1;                                                            \n\
            t[count][0] = t[count-1][2];                                            \n\
        }                                                                           \n\
    }\n\n")

    f.write("   bool in[" + str(num_states) + "][" + str(len(event_uniq)) + "] = {false};                                               \n\
    bool o[" + str(num_states) + "][" + str(len(event_uniq)) + "] = {false};                                                        \n\
                                                                                    \n\
    for (" + data_type['count'] + " i=0;i<count;i++)                                                    \n\
    {                                                                               \n\
        o[t[i][0]-1][t[i][1]-1] = true;                                         \n\
        in[t[i][2]-1][t[i][1]-1] = true;                                            \n\
    }\n")

    f.write("                                                                       \n\
    bool wrong_transition = false;                                                      \n\
    for (" + data_type['num_states'] + " i=0; i<num_states;i++)                                                     \n\
    {                                                                               \n\
    ")

    if not constraint:
        f.write("")
    else:
        for i in range(len(event_uniq)):
            constraint_i = [item[1] for item in constraint if item[0] == i+1]
            event_constraint = [constraint_i[ind] for ind in range(len(constraint_i)) if constraint_i[ind] > 0]
            if event_constraint:
                f.write("   if ")

                f.write("(in[i][ " + str(i) + "] && (")
            
                j = -1
                for j in range(len(event_constraint)-1):
                    f.write("o[i][" + str(event_constraint[j]-1) + "] || ")
                j = j + 1
                f.write("o[i][" + str(event_constraint[j]-1) + "]))\n")
                f.write("               wrong_transition = true;\n")
            
    f.write("   }\n")

    f.write("   assert(wrong_transition != false);\n")
    f.write('}')
    f.close()

def get_model(input_dict,init_model):

    found = 0
    f = open(C_gen_model_output,'r')
    out_cbmc = json.load(f)
    f.close()

    key = 'result'
    for x in out_cbmc:
        if key in x:
            result = x['result']

    key = 'status'
    
    for x in result:
        if key in x:
            if x[key] == "FAILURE":
                failed_out = x
                found = 1

    if not found:
        return(0,[])

    t = np.zeros(((len(input_dict['seq_input_uniq'][0])*len(input_dict['seq_input_uniq'])+len(init_model)),3),dtype=int)
    if (found):
        key = 'lhs'
        key2 = 'data'

        for x in failed_out['trace']:
            if (key in x) and (key2 in x['value']):
                if (re.search('t\[.*',x[key])):
                    ind = re.findall('[0-9]+',x[key])
                    if int(ind[0]) == (len(input_dict['seq_input_uniq'][0])*len(input_dict['seq_input_uniq'])+len(init_model)):
                        break
                    t[int(ind[0])][int(ind[1])] = int(x['value']['data'])
    
    t = np.unique(t,axis=0)
    t=[list(y) for y in t]
    return (found,t)

def get_seq_input_uniq(var):
    events_tup_to_list = var['events_tup_to_list']

    events_uniq_full = [j for i in events_tup_to_list for j in i]
    event_uniq = list(set(events_uniq_full))
    event_uniq.sort(key=events_uniq_full.index)
    var['event_uniq'] = event_uniq

    event_id = []

    for i in range(len(events_uniq_full)):
        event_id.append(event_uniq.index(events_uniq_full[i])+1)

    len_seq = 2

    seq_input = [];
    for i in range(len(event_id)-len_seq+1):
        ind = i
        seq_input.append(event_id[ind:ind+len_seq])

    seq_input_uniq = [list(i) for i in (np.unique(seq_input,axis=0))]
    var['seq_input_uniq'] = seq_input_uniq

def get_ce(trace_input,var,init_model):
    event_uniq_current_trace = []
    len_seq = 2

    event_uniq = list(range(1,len(trace_input['event_uniq'])+1))
    current_trace_events = list(np.unique(trace_input['event_id']))

    b = [list(i) for i in (itertools.product(event_uniq, repeat = len_seq))]
    not_in_seq = [x for x in b if x not in var['seq_input_uniq']]
    not_in_seq = [x for x in not_in_seq if x[0] in current_trace_events or x[1] in current_trace_events]

    found = 1
    if not not_in_seq:
        found = 0

    return (found,not_in_seq)

def plot_model(model,trace_input,num_states,hyperparams,nfa=True):

    trace_filename = hyperparams.input_file
    target_model_path = hyperparams.target
    order = hyperparams.order

    class vis_trace(object):
        def show_graph(self, **kwargs):
            name = trace_filename.replace('.txt','')
            if hyperparams.incr:
                name = name + '_incr_' + order
            if nfa:
                name = name + '_nfa.dot'
                #name = name + '_nfa.png'
            else:
                name = name + '_dfa.dot'
                #name = name + '_dfa.png'
            #self.get_graph().draw(name, format='png', prog='dot')
            self.get_graph().draw(name, format='dot', prog='dot')
            os.system('mv ' + name + ' ' + target_model_path)
            

    event_uniq = trace_input['event_uniq']
    event_id = trace_input['event_id']

    t = np.unique(model,axis=0)
    first_event = [ind for ind in range(len(model)) if model[ind][1] == event_id[0]]
    start_state = str(model[first_event[0]][0])
    states = []
    for i in model:
        states.append(str(i[0]))
        states.append(str(i[2]))

    states = list(np.unique(states))

    transitions = []
    for i in range(len(t)):
        temp_trans = [event_uniq[t[i][1]-1],str(t[i][0]),str(t[i][2])]
        transitions.append(temp_trans)

    model = vis_trace()
    machine = GraphMachine(model=model, 
                       states=states, 
                       transitions=transitions,
                       initial = '1',
                       show_auto_transitions=False,
                       title="trace_learn",
                       show_conditions=True)
    model.show_graph()

def nfa_traverse(model,trace, vis=[],find_vis=False):
    if not model:
        return (0, [])

    state = [1]
    temp = vis
    trans = []
    for i in range(len(trace)):
        if trace[i] == 1:
            state = [1]

        found = [x[2] for x in model if x[0] in state and x[1] == trace[i]]
        if find_vis:
            [trans.append(x) for x in model if x[0] in state and x[1] == trace[i]]
            [vis.append(x[0]) for x in model if x[0] in state and x[1] == trace[i]]
            if i == len(trace)-1 or trace[i+1] == 1:
                if len(found) == 1:
                    [vis.append(x) for x in found]
                else:
                    if not [x for x in found if x in vis]:
                        [vis.append(x) for x in found]
        if not found:
            if not find_vis:
                return (0,trace[0:i+1])
            return (0,trace[0:i+1],temp,[])
        else:
            state = found

    if find_vis:
        trans = [list(x) for x in np.unique(trans, axis=0)]
        return (1,[],vis,trans)
    return (1,[])

def nfa_to_dfa(nfa, full_event_id):
    def find_eq_states_to_merge(full_trans):
        states = list(full_trans.keys())
        event_len = len(full_trans[states[0]])

        to_be_merged = []
        outgoing = {}
        incoming = {}
        for i in range(len(states)):
            s1 = states[i]
            outgoing[s1] = [e_ind for e_ind in range(event_len)
                            if full_trans[s1][e_ind] != '']
            for e_ind in range(event_len):
                if full_trans[s1][e_ind] != '':
                    s_str = full_trans[s1][e_ind]
                    if s_str in incoming:
                        incoming[s_str].append(e_ind)
                        incoming[s_str] = list(np.unique(incoming[s_str]))
                    else:
                        incoming[s_str] = [e_ind]
            if s1 not in incoming:
                incoming[s1] = []

        for o1 in outgoing:
            if any([o1 in x for x in to_be_merged]):
                continue
            temp = [o1]
            temp1 = [o1]
            for o2 in outgoing:
                if any([o2 in x for x in to_be_merged]):
                    continue
                if(o1 == o2):
                    continue

                same_out = (all([x in outgoing[o1] for x in outgoing[o2]]) \
                    and all([x in incoming[o1] for x in incoming[o2]])) or \
                    (outgoing[o1] == outgoing[o2])
                if same_out:
                    temp.append(o2)

            if len(temp) > 1:
                if temp not in to_be_merged:
                    to_be_merged.append(temp)

        return to_be_merged, outgoing, incoming

    def check_if_subset(o1, o2, outgoing, incoming, full_trans):
        same_out = all([x in outgoing[o1] for x in outgoing[o2]])
        return same_out

    def subset_states_merge(full_trans, to_be_merged, outgoing, incoming):
        if not to_be_merged:
            return full_trans

        for x in to_be_merged:
            replace_with = x[0]
            for y in x[1:]:
                invalid_merge = False
                for i in outgoing[y]:
                    if full_trans[y][i] != '-' and full_trans[replace_with][i] != full_trans[y][i]:
                        if not check_if_subset(full_trans[replace_with][i], full_trans[y][i], outgoing, incoming, full_trans):
                            if not (full_trans[replace_with][i] == y and full_trans[y][i] == replace_with):
                                invalid_merge = True
                                break
                if not invalid_merge:
                    for key in full_trans:
                        for i in range(len(full_trans[key])):
                            full_trans[key][i] = replace_with if full_trans[key][i] == y else full_trans[key][i]
                    full_trans.pop(y)

        return full_trans

    def merge_states(full_trans, to_be_merged):
        states = list(full_trans.keys())
        event_len = len(full_trans[states[0]])

        if to_be_merged:
            new_state_str_list = {}
            for merge_states in to_be_merged:
                if any([s not in full_trans for s in merge_states]):
                    continue

                new_state = []
                for s in merge_states:
                    temp = [int(x) for x in s.split('_')]
                    [new_state.append(x) for x in temp]
                new_state = list(np.unique(new_state))
                new_state_str = '_'.join([str(x) for x in new_state])
                new_state_str_list[new_state_str] = merge_states.copy()
                
                for key in full_trans.keys():
                    for i in range(len(full_trans[key])):
                        if full_trans[key][i] in merge_states:
                            full_trans[key][i] = new_state_str

            for new_state_str in new_state_str_list:
                merge_states = new_state_str_list[new_state_str]
                for i in range(len(merge_states)):
                    if i == 0:
                        full_trans[new_state_str] = full_trans.pop(merge_states[i])
                    elif merge_states[i] != new_state_str:
                        full_trans.pop(merge_states[i])

        return full_trans

    def check_if_same_set(s, states):
        if not s[0] and not s[1]:
            return True
        if not s[0] or not s[1]:
            return False
        ind1 = [i for i in range(len(states)) if s[0] in states[i]]
        ind2 = [i for i in range(len(states)) if s[1] in states[i]]
        if not ind1 or not ind2:
            return False
        return ind1[0] == ind2[0]

    def minimize(full_trans):
        states_l = list(full_trans.keys())
        states = [states_l]
        event_len = len(full_trans[states_l[0]])

        if len(states_l) <= 1:
            return []

        new_states = []
        while True:
            for states_l in states:
                if len(states_l) == 1:
                    new_states.append(states_l)
                    continue
                i = 0
                while i < len(states_l)-1:
                    if i == 0:
                        state_pairs = [([states_l[0],states_l[1]],0)]
                        i = i + 1
                    else:
                        i = i + 1
                        state_pairs = [([states_l[i],new_states[j][0]],j+1) for j in range(len(new_states)) if new_states[j]]

                    for pair,j in state_pairs:
                        eq = True
                        if check_if_same_set(pair, states):
                            for e_ind in range(event_len):
                                s1_str = full_trans[pair[0]][e_ind]
                                s2_str = full_trans[pair[1]][e_ind]
                                if not check_if_same_set([s1_str,s2_str], states):
                                    eq = False
                                    break;
                        else:
                            continue

                        if eq:
                            if j == 0:
                                new_states.append(pair)
                            else:
                                new_states[j-1].append(pair[0])
                            break
                    
                    if not eq:
                        if j == 0:
                            new_states.append([pair[0]])
                            new_states.append([pair[1]])
                        else:
                            new_states.append([pair[0]])

            new_states.sort(key=len, reverse=True)
            if new_states != states:
                states = new_states.copy()
                new_states = []
            else:
                break

        to_be_merged = [x for x in new_states if len(x) > 1]

        return to_be_merged

    print("\n\nConverting NFA to DFA..........")

    states = [x[0] for x in nfa]
    [states.append(x[2]) for x in nfa]
    nfa_states = list(np.unique(states))
    len_states = len(nfa_states)

    events = [x[1] for x in nfa]
    events = list(np.unique(events))

    start_state = 1
    full_trans = {}

    states = [[start_state]]
    same_list = []
    for dfa_state in states:
        trans = []
        for e in events:
            temp = list(np.unique([x[2] for x in nfa if x[0] in dfa_state and x[1] == e]))
            temp.sort()
            temp_str = '_'.join([str(x) for x in temp])
            trans.append(temp_str)
            if temp and temp not in states:
                states.append(temp)

        state_str = '_'.join([str(s) for s in dfa_state])
        full_trans[state_str] = trans

    old_full_trans = {}
    while old_full_trans != full_trans:
        old_full_trans = copy.deepcopy(full_trans)
        to_be_merged = minimize(full_trans)
        if to_be_merged:
            full_trans = merge_states(full_trans, to_be_merged)

        full_trans_after_minimise = copy.deepcopy(full_trans)

        to_be_merged, outgoing, incoming = find_eq_states_to_merge(full_trans)
        if to_be_merged:
            full_trans = subset_states_merge(full_trans, to_be_merged, outgoing, incoming)

            dfa, num_states = dict2t(full_trans)
            (f,trace) = nfa_traverse(dfa, full_event_id)
            if not f:
                print(colored("[WARNING] Invalid merge, Reverting back....\n",'magenta'))
                full_trans = copy.deepcopy(full_trans_after_minimise)
            else:
                print(colored("Valid merge, Continue....\n",'green'))

    return full_trans

def dict2t(trans_dict):
    states = list(trans_dict.keys())
    t = []

    for s in states:
        s1 = states.index(s)
        for i in range(len(trans_dict[s])):
            next_state = trans_dict[s][i]
            if next_state != '':
                s2 = states.index(next_state)
                t.append([s1+1,i+1,s2+1])

    print("states = ",len(states))
    return t,len(states)

def make_model(full_events, model, var, hyperparams, num_states, input_dict):
    org_trace = full_events
    final_model_gen = model

    var['org_trace'] = org_trace
    var['o_event_uniq'] = input_dict['event_uniq']

    start_index = -1
    input_dict = text_preprocess(start_index,hyperparams,var)
    (f,trace) = nfa_traverse(model,input_dict['event_id'])
    if f:
        print(colored("Already present!!",'blue'))
        input_dict['event_uniq'] = var['o_event_uniq']
        return model, var, input_dict, num_states

    if round(0.75*len(full_events)) > 10:
        incr_SAT = hyperparams.incr
    else:
        incr_SAT = False
    len_seq = input_dict['len_seq']

    if incr_SAT:
        print(colored("Using Incremental SAT",'magenta'))

        ind_list = []
        num_of_ind = min([round(0.5*len(full_events)), 1000])

        max_ind = max(len(full_events)-10, round(len(full_events)*0.75))

        for i in range(num_of_ind):
            ind = random.randint(10, max_ind)
            ind_list.append(ind)

        ind_list = np.unique(ind_list)
        ind_list.sort()

        temp = []
        for ind in ind_list:
            for i in range(ind):
                temp.append(full_events[i])

        for i in range(len(full_events)):
            temp.append(full_events[i])

        full_events = temp
     
    start_id = [i for i in range(len(full_events)) if full_events[i]=='start']
    
    events_list = []
    for i in range(len(start_id)-1):
        events_list.append(full_events[start_id[i]:start_id[i+1]])

    events_tuple = list(set(tuple(x) for x in events_list))
    events_tup_to_list = [list(x) for x in events_tuple]

    events_tup_to_list.sort(key=len)
    print(len(events_tup_to_list))
    var['events_tup_to_list_internal'] = events_tup_to_list

    length = [len(x) for x in events_tup_to_list]
    print("Total size:" + str(sum(length)))

    if not final_model_gen:
        #Generate initial model
        var['incr'] = 0
    else:
        var['incr'] = 1

    start_index = -1

    input_dict['event_uniq'] = var['o_event_uniq']
    found_model = 0
    while(True):
        var['o_event_uniq'] = input_dict['event_uniq']

        init_model = final_model_gen
        start_index = start_index + 1

        if(start_index == len(events_tup_to_list)):
            break

        input_dict = text_preprocess(start_index,hyperparams,var)

        (f,trace) = nfa_traverse(init_model,input_dict['event_id'])
        if f:
            if found_model:
                input_dict_store = text_preprocess(-1,hyperparams,var)
                (f_full_trace,trace) = nfa_traverse(init_model,input_dict_store['event_id'])
                if f_full_trace:
                    break
                else:
                    found_model = 0
                    continue
            else:
                continue

        # pass only remaining trace
        if len(trace) - len_seq <= 0:
            len_done = 0
        else:
            len_done = len(trace) - len_seq
        input_dict = text_preprocess(start_index,hyperparams,var,len_done)

        ####################################
        print("Generating model with " + str(num_states) + " states")

        o_num_states = num_states

        print("Finding CE")
        (found_ce,ce_global) = get_ce(input_dict,var,init_model)
    
        if found_ce:
            print("CE found :")
            print(list(ce_global))
        else:
            print("No CE found, generating model")

        while(True):

            found_model = 0

            gen_c_model(input_dict,ce_global,num_states,start_index,init_model,var)
            print("Running CBMC...............")
            os.system("cbmc " + C_gen_model + " --trace --json-ui > " + C_gen_model_output)

            (found_model,temp_model) = get_model(input_dict,init_model)

            if found_model:
                print(colored("Final Generated model",'green'))
                print(colored(temp_model,'green'))
                final_model_gen = temp_model
                break;
            else:
                num_states = num_states + 1
                print("No model, increasing number of states to %d" %(num_states))

        var['incr'] = 1
        end_time = time.time()
        # print('\n\nTime taken: ' + str(end_time - start_time))

    print("\n\n\n------------- Verifying: ----------------------------")

    start_index = -1
    input_dict = text_preprocess(start_index,hyperparams,var)
    input_dict_store = copy.deepcopy(input_dict)
    
    while(True):
        vis = [1]
        (f, trace, vis, trans) = nfa_traverse(final_model_gen,input_dict_store['event_id'], vis ,True)

        if f:
            print(colored("\nAll behaviors present",'green'))
            final_model = final_model_gen
            print(colored("Final Generated model",'green'))
            print(colored(final_model,'green'))
            print(colored("Number of states: " + str(num_states),'green'))
            print(colored("Number of transitions: " + str(len(final_model)),'green'))
            return final_model, var, input_dict_store, num_states
        else:
            print(colored("\n[ERROR] Missing behavior",'red'))
            print(colored(trace,'red'))
            print('\n')

            if len(trace)-10 > 1:
                input_dict_store['seq_input_uniq'] = [trace[len(trace)-10:]]
                input_dict['event_id'] = trace[len(trace)-10:]
            else:
                input_dict_store['seq_input_uniq'] = [trace]
                input_dict['event_id'] = trace

            (found_ce,ce_global) = get_ce(input_dict,var,init_model)
    
            if found_ce:
                print("CE found :")
                print(list(ce_global))
            else:
                print("No CE found, generating model")

            while(True):

                found_model = 0

                init_model = final_model_gen

                gen_c_model(input_dict_store,ce_global,num_states,start_index,init_model,var)
                print("Running CBMC...............")
                os.system("cbmc " + C_gen_model + " --trace --json-ui > " + C_gen_model_output)

                (found_model,temp_model) = get_model(input_dict_store,init_model)

                if found_model:
                    print(colored("Final Generated model",'green'))
                    print(colored(temp_model,'green'))
                    final_model_gen = temp_model
                    break;
                else:
                    num_states = num_states + 1
                    print("No model, increasing number of states to %d" %(num_states))

def parse_args():
    parser = argparse.ArgumentParser()
    required_parse = parser.add_argument_group('required arguments')
    required_parse.add_argument('-i','--input_file', metavar = 'INPUT_FILENAME', required = True,
            help='Input trace file for model learning')
    parser.add_argument('-w', '--window', metavar = 'SLIDING_WINDOW', default=3, type=int,
            help='Sliding window size. Default = 3')
    parser.add_argument('-n','--num_states', metavar = 'NUM_STATES', default=2, type=int,
            help='Number of states. Default = 2')
    parser.add_argument('--dfa', metavar = 'DFA', default=False, const=True, nargs='?',
            help='Generate a DFA. Generates NFA by default.')
    parser.add_argument('-t','--target', metavar = 'TARGET_PATH', default=full_path + 'models',
            help='Target model path. Default = ' + full_path + 'models')
    parser.add_argument('-o','--order', metavar = 'TRACE_ORDER', default='same', choices=['bts','stb','random','same'],
            help='Trace order. Available options stb, bts, same, random. Default = same')
    parser.add_argument('--incr', metavar = 'INCREMENTAL_SAT', default=False, const=True, nargs='?',
            help='Incremental model learning')
    parser.add_argument('--trace-stats', metavar = 'TRACE_STATS', default=False, const=True, nargs='?',
            help='Print trace statistics')

    hyperparams = parser.parse_args()
    return hyperparams

def main():
    hyperparams = parse_args()

    trace_filename = hyperparams.input_file
    print(trace_filename)
    num_states = hyperparams.num_states
    len_seq = hyperparams.window
    target_model_path = hyperparams.target
    order = hyperparams.order
    trace_stats = hyperparams.trace_stats

    o_event_uniq = []
    o_event_id = []
    init_model = []
    start_state = 1

    f = open(trace_filename,'r')
    events_raw = f.readlines()
    full_events = [x.replace('\n','') for x in events_raw]
    if 'start' not in full_events[-1] or 'start' not in full_events[0]:
        print(colored("Delimiter 'start' missing, cannot identify beginning and end of trace\n",'magenta'))
        exit(0)

    unique_events, ind = np.unique(full_events, return_index=True)
    unique_events = list(unique_events[np.argsort(ind)])
    unique_events = [x for x in unique_events if x != 'start']
    f.close()

    start_id = [i for i in range(len(full_events)) if full_events[i]=='start']

    if trace_stats:
        print(colored("Size of Trace Set",'blue'))
        print(len(start_id)-1)
        print(colored("Total Trace Length",'blue'))
        print(len(full_events) - len(start_id))
        print(colored("Avg Trace Length",'blue'))
        trace_l = (len(full_events)-len(start_id))/(len(start_id)-1)
        print(trace_l)
        print(colored("Number of Unique Events: " + str(len(unique_events)),'blue'))
        print(unique_events)
        exit(0)

    events_list = []
    for i in range(len(start_id)-1):
        events_list.append(full_events[start_id[i]:start_id[i+1]+1])

    events_tuple = list(set(tuple(x) for x in events_list))
    events_tup_to_list = [list(x) for x in events_tuple]
    if(order == 'same'):
        events_tup_to_list.sort(key=events_list.index)
    elif(order == 'bts'):
        events_tup_to_list.sort(key=len,reverse=True)
    elif(order == 'stb'):
        events_tup_to_list.sort(key=len)
    elif(order == 'random'):
        random.shuffle(events_tup_to_list)

    var = {'incr': 0, 'events_tup_to_list': events_tup_to_list, 'o_event_uniq': o_event_uniq, 'org_trace': []}
    input_dict = {'event_id':[], 'seq_input_uniq':[], 'event_uniq':[], 'len_seq':len_seq}
    get_seq_input_uniq(var)

    model_gen = []
    i = 1

    for event_list in events_tup_to_list:
        print("\n\n\n******************************************** TRACE:" + str(i))
        model_gen, var, input_dict, num_states = make_model(event_list, model_gen, var, hyperparams, num_states, input_dict)
        i = i + 1

    plot_model(model_gen,input_dict,num_states,hyperparams)

    final_model_gen = model_gen

    start_index = -1
    var['org_trace'] = full_events
    input_dict = text_preprocess(start_index,hyperparams,var)
    input_dict_store = copy.deepcopy(input_dict)

    while(True):
        vis = [1]
        print("NFA states: ",num_states)
        end_time = time.time()
        # print('\n\nTime taken: ' + str(end_time - start_time))

        do_dfa = hyperparams.dfa
        if do_dfa:
            trans_dict = nfa_to_dfa(final_model_gen, input_dict_store['event_id'])
            final_model_gen, num_states = dict2t(trans_dict)
            print("Length of trace: " + str(len(input_dict_store['event_id'])))

        (f, trace, vis, trans) = nfa_traverse(final_model_gen,input_dict_store['event_id'], vis ,True)

        if f:
            print(colored("\nAll behaviors present",'green'))
            vis = list(np.unique(vis))
            final_model = trans
            (f, trace) = nfa_traverse(trans,input_dict_store['event_id'])
            if not f:
                print(colored("Missing Transition!",'red'))
                exit(0)
            print(colored("States not visited",'green'))
            print(colored([s+1 for s in range(num_states) if s+1 not in vis],'green'))
            print(colored("Final Generated model",'green'))
            print(colored(final_model,'green'))

            model_with_event = [[x[0],var['event_uniq'][x[1]-1],x[2]] for x in final_model]
            print(colored(model_with_event,'green'))

            print(colored("Number of states: " + str(len(vis)),'green'))
            print(colored("Number of transitions: " + str(len(final_model)),'green'))
            end_time = time.time()
            # print('\n\nTime taken: ' + str(end_time - start_time))
            plot_model(final_model,input_dict_store,num_states,hyperparams,not do_dfa)
            break
        else:
            print(colored("\n[ERROR] Missing behavior",'red'))
            print(colored(trace,'red'))
            print('\n')

            input_dict_store['seq_input_uniq'] = [trace]
            input_dict['event_id'] = trace

            (found_ce,ce_global) = get_ce(input_dict,var,init_model)
    
            if found_ce:
                print("CE found :")
                print(list(ce_global))
            else:
                print("No CE found, generating model")

            while(True):

                found_model = 0

                init_model = final_model_gen

                gen_c_model(input_dict_store,ce_global,num_states,start_index,init_model,var)
                print("Running CBMC...............")
                os.system("cbmc " + C_gen_model + " --trace --json-ui > " + C_gen_model_output)

                (found_model,temp_model) = get_model(input_dict_store,init_model)

                if found_model:
                    print(colored("Final Generated model",'green'))
                    print(colored(temp_model,'green'))
                    final_model_gen = temp_model
                    break;
                else:
                    num_states = num_states + 1
                    print("No model, increasing number of states to %d" %(num_states))

full_path = abspath(__file__).replace('learn_model.py','')
C_gen_model = full_path + 'aux_files/gen_model_new.c'
C_gen_model_output = full_path + 'aux_files/cbmc_output_gen_model_new.json'

if __name__ == '__main__':
    start_time = time.time()
    main()
    end_time = time.time()
    # print('\n\nTime taken: ' + str(end_time - start_time))
