import os,sys,getopt
import argparse
import logging
import io
import itertools as IT
import xml.etree.ElementTree as ET
PY2 = sys.version_info[0] == 2
StringIO = io.BytesIO if PY2 else io.StringIO

# Get an instance of a logger
logger = logging.getLogger(__name__)

class Variable(object):
    def __init__(self, varname, datatype, controltype, initial_value, possible_values, fsm):
        self.varname = varname
        self.datatype = datatype
        self.controltype = controltype
        self.initial_value = initial_value
        self.possible_values = possible_values
        self.fsm = fsm

    def set_varname(self, varname):
        self.varname = varname

    def set_datatype(self, datatype='boolean'):
        self.datatype = datatype

    def set_controltype(self, controltype='environment'):
        self.controltype = controltype

class SequenceNumber(object):
    def __init__(self, seqname, start, end, possible_values):
        self.seqname = seqname
        self.start = start
        self.end = end
        self.possible_values = possible_values

class Channel(object):
    def __init__(self, channel_label, start, end, noisy=False):
        self.channel_label = channel_label
        self.start = start
        self.end = end
        self.noisy = noisy

class Action(object):
    def __init__(self, action_label, channel):
        self.action_label = action_label
        self.channel = channel


class Transition(object):
    def __init__(self, transition_label, start, end, condition, actions):
        self.transition_label = transition_label
        self.start = start
        self.end = end
        self.condition = condition
        self.actions = actions
        self.contending_transitions = []
    def set_contending_transitions(self, contending_transitions):
        self.contending_transitions = contending_transitions


class FSM(object):
    def __init__(self, fsm_label, states, init_state, incoming_messages, outgoing_messages, transitions):
        self.fsm_label = fsm_label
        self.states = states
        self.init_state = init_state
        self.incoming_messages = incoming_messages
        self.outgoing_messages = outgoing_messages
        self.transitions = transitions

    def set_states(self,states):
        states = []
        for state in states:
            self.states.append(state)

    def add_state(self,state):
        self.states.append(state)

    def set_actions(self, actions):
        self.actions = []
        for action in actions:
            self.actions.append(action)

    def add_action(self, action):
        self.actions.append(action)



def parseDOT(dotfile, fsm_label):

    f = open(dotfile, "r")
    lines = f.readlines()

    # store the parsing results
    smv_vars = []
    smv_seq_nums = []
    smv_transitions = []
    smv_manual_checks = []

    system_fsms = []
    system_channels = []
    injective_adversaries = []


    fsm_states = []
    in_msgs = []
    out_msgs = []
    env_vars = []
    # state_vars = []
    # seq_vars = []
    transitions = []
    transition_counter = 0

    for i in range(len(lines)):

        if 'node' in lines[i]:
            strg = lines[i].split(']')[1].split(';')[0].strip()
            fsm_states.append(strg.strip())
    

        elif 'shape' in lines[i]:
            s = lines[i].split('[')[0].strip()

            if s not in fsm_states and 'start0' not in s:
                fsm_states.append(s)


        elif 'initial_state' in lines[i]:
            init_state = lines[i].split(':')[1].strip()
            #print 'init state = ', init_state

        elif 'start0' in lines[i]:
            init_state = lines[i].split('->')[1].strip()
            init_state = init_state.split(';')[0]
            print 'init state = ', init_state

    
        elif 'incoming messages' in lines[i]:
            strg = lines[i].split(':')[1].split(';')
            for s in strg:
                s = s.strip()
                if s is None or s is '':
                        break
                in_msgs.append(s.strip())
                #print 'in_msg = ', s.strip()

        elif 'outgoing messages' in lines[i]:
            strg = lines[i].split(':')[1].split(';')
            for s in strg:
                s = s.strip()
                if s is None or s is '':
                        break
                out_msgs.append(s.strip())
                #print 'out_msg = ', s.strip()
        
        elif '//' in lines[i] and lines[i].startswith('//'):
            continue

        elif '->' in lines[i] and 'start' not in lines[i]:
            transition = ''
            strg = lines[i].split('->')
            start_state = strg[0].strip() 
            #print start_state
            strg = strg[1].split('[')
            end_state = strg[0].strip() 
            #print fsm_states

            if start_state not in fsm_states:
                print (lines[i])
                print ('ERROR: start_state is not in the list of states')
                return

            if end_state not in fsm_states:
                #print ('states = ', state_vars)
                print ('end_state ', end_state)
                print ('ERROR: end_state is not in the list of states')
                return

            strg = strg[1].split('"')
            if len(strg) == 3:  #transition is written in one line
                transition = strg[1]

            else:
                transition = strg[1].strip()
                #print transition
                j = i+1
                while '"]' not in lines[j].strip():
                    transition = transition + lines[j].strip()
                    #print transition
                    j = j + 1
                strg = lines[j].split('"]')
                transition = transition + strg[0]
                i = j
                transitions.append(transition)

            transition_counter = transition_counter + 1
            #transition_counter = 0
            transition_label = fsm_label + "_T" + str(transition_counter)

            values = transition.split('/')
            #print 'values = ', values

            cond_str = values[0]
            act_str = values[1]

            if cond_str not in in_msgs:
                in_msgs.append(cond_str)


            if act_str not in out_msgs:
                out_msgs.append(act_str)

            # Rest of the code is not really necessary for parsing dot files used in this equivalence checking
            # PARSING ACTIONS
            acts = act_str.split(',')
            actions = []
            for act in acts:
                action_label = act.strip()
                if action_label == '':
                    print ('ERROR: There are some transitions in comments (//) or missing underscore sign or extra comma due to which empty action is happening????')
                    #print (lines[i])
                    continue

                if action_label == '_':
                    action_label = 'null_action'


                else:
                    #print 'internal_action: ', action_label
                    for out_msg in out_msgs:
                        if out_msg == action_label:
                            print ('ERROR: Outgoing message has been parsed as ACTION????????')

                    if '=' in action_label:
                        int_act_tokens = action_label.split('=')
                        int_act = int_act_tokens[0].strip()
                        value = int_act_tokens[1].strip().lstrip()
                        #print 'value = ', value
                        if 'true' in value:
                            action_label = action_label.replace('true', 'TRUE')
                        elif 'false' in value:
                            action_label = action_label.replace('false', 'FALSE')
                        #print 'action_label =', action_label

                    elif '++' in action_label:
                        int_act_tokens = action_label.split('++')
                        int_act = int_act_tokens[0].strip()
                        action_label = '(' + int_act + '=' + int_act + '+ 1)'
                    chan_label = 'internal'

                if(action_label != '' and action_label != None):
                    #print "*** ACTION LABEL = ", action_label + ' ****'
                    channel_new = Channel(chan_label, chan_start, chan_end)
                    new_action = Action(action_label, channel_new)
                    actions.append(new_action)

            # PARSING CONDITIONS
            condition = cond_str
            # if 'paging_tmsi' in condition:
            #     print 'condition = ', condition
            # print 'condition = ', condition
            #
            cond_tokens = cond_str.split(' ')

            # if 'paging_tmsi' in condition:
            #     print 'cond tokens = ', cond_tokens
            #     print 'condition = ', condition

            for token in cond_tokens:

                token = token.strip()
                if '(' in token:
                    token = token.split('(')[1]
                if ')' in token:
                    token = token.split(')')[0]

                if token in in_msgs:
                    msg = token
                    condition = condition.replace(token, msg)
                condition = condition.replace('true', 'TRUE')
                condition = condition.replace('false', 'FALSE')

            # optimized version of LTS while converting from the given FSM
            if (start_state == end_state and action_label == 'null_action'):
                continue

            new_transition = Transition(transition_label, start_state, end_state, condition, actions)
            smv_transitions.append(new_transition)
    
    
    fsm = FSM(fsm_label, fsm_states, init_state, in_msgs, out_msgs, smv_transitions)

    f.close()
    return (fsm)

def find_contendition_transitions(fsm):
    transition_contendingTransition_map = []
    for i in range(len(fsm.transitions)):
        transition = fsm.transitions[i]
        contendingTransitions = []
        for j in range(len(fsm.transitions)):
            if (i==j):
                continue
            if(fsm.transitions[i].start == fsm.transitions[j].start):
                contendingTransitions.append(fsm.transitions[j].transition_label)
        transition_contendingTransition_map.append((transition, contendingTransitions))
    return transition_contendingTransition_map

# dump vars
def dump_variables(file, incoming_messages, variables=0):
    file.write('\n------------------- Environment, State, and Input variables --------------------\n')
    file.write('\nVAR\n\n')
    # for var in variables:
    #     if(var.datatype == 'boolean'):
    #         file.write(var.varname +  '\t:\t' + var.datatype + ';\t\n')
    #     elif (var.datatype == 'enumerate'):
    #         file.write(var.varname + '\t:\t{')
    #         for i in range(len(var.possible_values)):
    #             if(i == len(var.possible_values) -1 ):
    #                 file.write(var.possible_values[i])
    #             else:
    #                 file.write(var.possible_values[i] + ', ')
    #         file.write('};\t\n')

    file.write('input\t:\n{')
    for i in range(len(incoming_messages)):
        if (i == len(incoming_messages) - 1):
            file.write ('\t'+incoming_messages[i] + '\n')
        else:
            file.write('\t'+incoming_messages[i] + ',\n') 
    file.write('};\t\n')
    
    return

# dump sequence numbers
def dump_sequence_numbers(file, seq_nums):
    file.write('\n----------------- Sequence numbers -------------------\n')

    for seq_num in seq_nums:
        file.write(seq_num.seqname +  '\t:\t' + str(seq_num.start) + '..' + str(seq_num.end) + '\t;\n')
    return

# dumping states of a FSM
def dump_states(file, fsms):
    for fsm in fsms:
        file.write('\n---------------- state for ' + fsm.fsm_label + ' state machine ----------------\n')
        file.write('\n' + str(fsm.fsm_label).lower() + '_state\t:\n')
        file.write('{\n')
        for i in range(len(fsm.states)):
            if (i < len(fsm.states) - 1):
                file.write(str('\t' + fsm.states[i]) + ',\n')
            else:
                file.write('\t'+str(fsm.states[i]) + '\n')
        file.write('};\n')
    return

# get the unique action_names of a fsm
def get_unique_action_names(fsm):
    action_labels = []
    #print (len(fsm.transitions))
    for transition in fsm.transitions:
        #print (len(transition.actions))
        for action in transition.actions:
            if (action.action_label not in action_labels): #and action.channel.channel_label.lower() != 'internal'):
                action_labels.append(action.action_label)
    #print action_labels
    return action_labels


# dump fsm_actions
def dump_actions(file, fsms):
    for fsm in fsms:
        file.write('------------ Possible ' + fsm.fsm_label + ' actions ----------------\n')
        action_labels = get_unique_action_names(fsm)
        #print ('action_labels = ', action_labels)
        fsm_label_entity= fsm.fsm_label.lower().split('_')[0].strip()
        file.write('\n'+ fsm.fsm_label.lower() + '_action\t:\n')
        file.write('{\n')
        
        if(len(action_labels) > 0 and 'null_action' not in action_labels):
            file.write('\t' + 'null_action,\n')
            
        for i in range(len(action_labels)):
            if (i < len(action_labels) - 1):
                #file.write('\t'+fsm.fsm_label.lower() + '_' + action_labels[i] + ',\n')
                file.write('\t' + action_labels[i] + ',\n')
            else:
                #file.write('\t'+fsm.fsm_label.lower() + '_' + action_labels[i] + '\n')
                file.write('\t' + action_labels[i] + '\n')

        if(len(action_labels) == 0):
            #file.write('\t' + fsm.fsm_label.lower() + '_null_action\n')
            file.write('\t' + 'null_action\n')

        

        file.write('};\n')
    return

# get the actions of a specific channel
def get_channel_actions(channel_start, channel_end, fsms):
    action_labels = []

    for fsm in fsms:

        if channel_start in fsm.fsm_label:
            for transition in fsm.transitions:
                for action in transition.actions:

                    if (action.channel.start.lower() == channel_start.lower() and action.channel.end.lower() == channel_end.lower() and
                    action.channel.channel_label != 'internal'):
                        if(action.action_label not in action_labels):
                            action_labels.append(action.action_label)
    #print action_labels
    return action_labels


# mapping (channel, actions)
def get_channel_actions_map(channels, fsms):
    channel_actions_map=[]
    for i in range(len(channels)):
        channel_actions_map.append((channels[i], get_channel_actions(channels[i].start, channels[i].end, fsms)))
    return channel_actions_map


def dump_adversary_channel(file, channels, fsms):
    #print (channels)
    for channel in channels:
        file.write('\n--------------- Adversarial channel from ' + channel.start.upper() + ' to ' + channel.end.upper() +' ---------------\n')
        actions = get_channel_actions(channel.start, channel.end, fsms)

        file.write('\n' + channel.channel_label + '\t:\n')
        file.write('{\n')
        for i in range(len(actions)):
            if (i < len(actions) - 1):
                file.write('\t'+channel.channel_label.replace('_', '') + '_'+ str(actions[i]).strip() + ',\n')
            else:
                file.write('\t' + channel.channel_label.replace('_', '') + '_'+ str(actions[i]).strip() + '\n')
        file.write('};\n')


# dump Injection Adversary Action for each of the channels
def dump_injective_adversary(file, channels, injective_adversaries, fsms):
    for injective_adversary in injective_adversaries:
        active_channel_label = injective_adversary.active_channel_label
        # find if the active channel is one of the channels
        for channel in channels:
            if (active_channel_label.lower() == channel.channel_label.lower()):
                action_labels = get_channel_actions(channel.start, channel.end, fsms)
                inj_adv_act_ch_name = injective_adversary.inj_adv_label
                file.write(
                    '\n--------------- Injective adversary action for channel '+ channel.channel_label + ' ---------------\n')
                inj_adv_act_ch_name = inj_adv_act_ch_name[0:inj_adv_act_ch_name.rfind('_')] + '_act_' + inj_adv_act_ch_name[inj_adv_act_ch_name.rfind('_') + 1:]
                file.write('\n' + inj_adv_act_ch_name + '\t:\n')
                file.write('{\n')
                for i in range(len(action_labels)):
                    prefix = injective_adversary.inj_adv_label[injective_adversary.inj_adv_label.rfind('_')+1:]
                    if (i < len(action_labels) - 1):

                        file.write('\tadv_' + prefix + '_' + action_labels[i] + ',\n')
                    else:
                        file.write('\tadv_' + prefix +'_' + action_labels[i] + '\n')
                file.write('};\n')
    return


# dump transitions of the FSMs
def dump_transitions(file, fsms):
    # dumping actions
    for fsm in fsms:
        file.write('\n-----------------' + fsm.fsm_label +' transitions --------------------\n')
        transition_contendingTransitions_map = find_contendition_transitions(fsm)
        for i in range(len(fsm.transitions)):
            condition = fsm.transitions[i].condition
            file.write(fsm.transitions[i].transition_label +'\t:=\t (' + fsm.fsm_label.lower()+ '_state = ' +
                       fsm.transitions[i].start + ' & input = '+ condition + ')\t;\n')
    return

# dump the controls for noisy channels
def dump_noisy_channel_controls(file, channels):
    file.write('\n------------------- Noisy Channels --------------------\n')
    for channel in channels:
        prefix = channel.channel_label[channel.channel_label.rfind('_') + 1:]
        if(channel.noisy.lower() == 'yes' or channel.noisy.lower() == 'true'):
            file.write('noisy_channel_' + prefix.strip() + ':=\tTRUE;\n')
        elif (channel.noisy.lower() == 'no' or channel.noisy.lower() == 'false' ):
            file.write('noisy_channel_' + prefix.strip() + ':=\tFALSE;\n')
    return

#dump the controls for adversarial channels
def dump_adversarial_channel_controls(file, injective_adversaries):
    file.write('\n------------------- Adversary enabled or not --------------------\n')
    for injective_adversary in injective_adversaries:
        prefix = injective_adversary.inj_adv_label + '_enabled'
        #if(injective_adversary.alwayson.lower() == 'yes' or injective_adversary.alwayson.lower() == 'true'):
        file.write(prefix.strip() + ':=\tTRUE;\n')
        #elif(injective_adversary.alwayson.lower() == 'no' or injective_adversary.alwayson.lower() == 'false'):
        #    file.write(prefix.strip() + ':=\tFALSE;\n')
    return



def dump_manual(input_file, file, section_name):
    # create element tree object
    tree = ET.parse(input_file)

    # get root element
    root = tree.getroot()

    manual_dumps = root.find('manual_dump')
    if (root.find('manual_dump')):
        for instance in manual_dumps:
            section = instance.find('section').text
            section = str(section).strip().upper()
            if (section in str(section_name).upper()):
                text = instance.find('text').text
                lines = str(text).split('\n')
                for line in lines:
                    file.write(line.lstrip() + '\n')
    return

def dump_manual_checks(file, manual_checks):
    for check in manual_checks:
        file.write(check + ';\n')
    return

def dump_defines(file, fsms):
    file.write('\n\nDEFINE\n')
    dump_transitions(file, fsms)
    return


# dump adversarial state machines
def dump_adversarial_state_machines(file, injective_adversaries, channel_actions_map):
    file.write('\n------------------- Adversarial state machines --------------------\n')
    for injective_adversary in injective_adversaries:
        inj_adv_act_chanLabel = injective_adversary.inj_adv_label[:injective_adversary.inj_adv_label.rfind('_')] + '_act_' + injective_adversary.inj_adv_label[injective_adversary.inj_adv_label.rfind('_') + 1 :]
        file.write('\ninit(' + inj_adv_act_chanLabel + ')\t:=\n')
        file.write('{\n')
        for i in range(len(channel_actions_map)):
            if(channel_actions_map[i][0].channel_label.lower() == injective_adversary.active_channel_label.lower()):
                action_labels = channel_actions_map[i][1]
                for i in range(len(action_labels)):
                    prefix = injective_adversary.inj_adv_label[injective_adversary.inj_adv_label.rfind('_')+1:]
                    if (i < len(action_labels) - 1):
                        file.write('\tadv_' + prefix + '_' + action_labels[i] + ',\n')
                    else:
                        file.write('\tadv_' + prefix +'_' + action_labels[i] + '\n')
                file.write('};\n')
        file.write('\nnext(' + inj_adv_act_chanLabel + ')\t:=\tcase\n')
        file.write('TRUE\t:\t{\n')
        for i in range(len(channel_actions_map)):
            if (channel_actions_map[i][0].channel_label.lower() == injective_adversary.active_channel_label.lower()):
                action_labels = channel_actions_map[i][1]
                for i in range(len(action_labels)):
                    prefix = injective_adversary.inj_adv_label[injective_adversary.inj_adv_label.rfind('_') + 1:]
                    if (i < len(action_labels) - 1):
                        file.write('\tadv_' + prefix + '_' + action_labels[i] + ',\n')
                    else:
                        file.write('\tadv_' + prefix + '_' + action_labels[i] + '\n')
                file.write('};\n')
                file.write('esac\t;\n')

    return

# get the mapping (fsm, (deststate, transitions))
# for each deststate of a FSM, find the transitions
# transitions are list of transition_labels
def get_fsm_deststate_transition_map(fsms):
    fsm_deststate_transition_map = []
    for fsm in fsms:
        deststate_transition_map = []
        for state in fsm.states:
            transitions = []
            for transition in fsm.transitions:
                if (str(state).lower().strip() == str(transition.end).lower().strip()):
                    transitions.append(transition.transition_label)
            deststate_transition_map.append((state, transitions))
        fsm_deststate_transition_map.append((fsm, deststate_transition_map))

    return fsm_deststate_transition_map


# dump FSM transition state machines
def dump_state_machines(file, fsms):
    fsm_deststate_transition_map = get_fsm_deststate_transition_map(fsms)
    for i in range(len(fsm_deststate_transition_map)):
        fsm = fsm_deststate_transition_map[i][0]
        file.write('\n\n---------------' + fsm.fsm_label + ' state machine ------------------\n')
        file.write("\ninit(" + fsm.fsm_label.lower() +'_state)\t:=' +
                   fsm.init_state.lower() + ';\n')
        file.write("\nnext(" + fsm.fsm_label.lower() + '_state)\t:=\t case\n\n')
        deststate_transition_map = fsm_deststate_transition_map[i][1]
        for j in range(len(deststate_transition_map)):
            deststate = deststate_transition_map[j][0]
            transition_labels = deststate_transition_map[j][1]
            if (len(transition_labels) != 0):
                file.write('(')
            for k in range(len(transition_labels)):
                if(k < len(transition_labels)-1):
                    file.write(transition_labels[k] + ' | ')
                else:
                    file.write(transition_labels[k])
            if(len(transition_labels) != 0):
                file.write(' )\t:\t' + deststate.lower() +'\t;\n')
        file.write('TRUE\t:\t' + fsm_deststate_transition_map[i][0].fsm_label.lower() +'_state\t;\n')
        file.write('esac\t;')

    return


# get the mapping (fsm, (action, transitions))
# for each action of a FSM, find the corresponding transitions
def get_fsm_action_transition_map(fsms):
    fsm_action_transition_map = []
    for fsm in fsms:
        action_transition_map = []
        action_labels = get_unique_action_names(fsm)
        for action_label in action_labels:
            transitions = []
            for transition in fsm.transitions:
                for action in transition.actions:
                    if (action_label.lower() == action.action_label.lower()):
                        transitions.append(transition.transition_label)
            action_transition_map.append((action_label, transitions))
        fsm_action_transition_map.append((fsm, action_transition_map))
    return fsm_action_transition_map


def dump_action_state_machines(file, fsms):
    fsm_action_transition_map = get_fsm_action_transition_map(fsms)
    for i in range(len(fsm_action_transition_map)):
        # file.write("\n\n\ninit(" + fsm_action_transition_map[i][0].fsm_label.lower() +'_action)\t:= ' +
        #            fsm_action_transition_map[i][0].fsm_label.lower() + '_null_action\t;\n')
        file.write("\n\n\ninit(" + fsm_action_transition_map[i][0].fsm_label.lower() + '_action)\t:= null_action\t;\n')
        file.write("\nnext(" + fsm_action_transition_map[i][0].fsm_label.lower() + '_action)\t:=\t case\n\n')
        action_transition_map = fsm_action_transition_map[i][1]
        for j in range(len(action_transition_map)):
            file.write('(')
            action_label = action_transition_map[j][0]
            #print action_label
            transition_labels = action_transition_map[j][1]
            for k in range(len(transition_labels)):
                if(k < len(transition_labels)-1):
                    file.write(transition_labels[k] + ' | ')
                else:
                    file.write(transition_labels[k])
            fsm_label_entity = fsm_action_transition_map[i][0].fsm_label.lower().split('_')[0].strip()
            #file.write(' )\t:\t' + fsm_action_transition_map[i][0].fsm_label.lower() + '_'+ action_label +'\t;\n')
            file.write(' )\t:\t' + action_label + '\t;\n')

        #file.write('TRUE\t:\t' + fsm_action_transition_map[i][0].fsm_label.lower() +'_null_action\t;\n')
        file.write('TRUE\t:\t null_action\t;\n')
        file.write('esac\t;')

    return


def dump_adv_channel_state_machines(file, channels, injective_adversaries, fsms):
    for injective_adversary in injective_adversaries:
        file.write('\n\ninit(' + injective_adversary.active_channel_label + ')\t:=\t' +
                   injective_adversary.active_channel_label.replace('_','') + '_null_action;\n')
        file.write('\nnext(' + injective_adversary.active_channel_label + ')\t:=\t case\n')
        attacher_inject_msg = 'attacker_inject_message_' + injective_adversary.active_channel_label.replace('_','')
        inj_adv_chan_enabled = 'inj_adv_' + injective_adversary.inj_adv_label[injective_adversary.inj_adv_label.rfind('_') + 1 : ] + '_enabled'
        inj_adv_act_chan = 'inj_adv_act_' + injective_adversary.inj_adv_label[injective_adversary.inj_adv_label.rfind('_') + 1 : ]
        for channel in channels:
            if (channel.channel_label.lower() == injective_adversary.active_channel_label.lower()):
                action_labels = get_channel_actions(channel.start, channel.end, fsms)
                for action_label in action_labels:
                    adv_chan_act = 'adv_' + injective_adversary.inj_adv_label[injective_adversary.inj_adv_label.rfind('_')+1:] + '_' + action_label
                    file.write(attacher_inject_msg + '\t&\t' + inj_adv_chan_enabled + '\t&\t'+ inj_adv_act_chan + '\t=\t')
                    file.write(adv_chan_act + '\t:\t' + injective_adversary.active_channel_label.replace('_', '') +'_' + action_label + '\t;\n')


                #noisy_channel_chan = 'noisy_channel_' + channel.channel_label[channel.channel_label.rfind('_') + 1 : ]
                entity_action = channel.start.lower() + '_action'
                for action_label in action_labels:
                    entity_action_value = channel.start.lower() + '_' + action_label
                    entity_action_value = action_label
                    chan_value = channel.channel_label.replace('_', '') + '_' + action_label
                    #file.write('! ' + noisy_channel_chan + '\t&\t'+ entity_action + '\t=\t '+ entity_action_value + '\t:\t' + chan_value +'\t;\n')
                    file.write(entity_action + '\t=\t ' + entity_action_value + '\t:\t' + chan_value + '\t;\n')
                #
                # file.write('\nTRUE\t:\n')
                # file.write('{\n')
                # for i in range(len(action_labels)):
                #     chan_value = channel.channel_label.replace('_', '') + '_' + action_labels[i]
                #     if(i < len(action_labels)-1):
                #         file.write('\t'+chan_value + ',\n')
                #     else:
                #         file.write('\t' + chan_value + '\n')

                file.write('TRUE\t: {')
                chan_value = channel.channel_label.replace('_', '') + '_null_action'
                file.write(chan_value + '}\t;\n')
                file.write('esac\t;\n')
    return


def dump_state_variable_state_machines(file, vars, fsms):
    var_value_transition_map = []
    file.write('\n\n--------------- State Variables state machine ------------------\n')
    for var in vars:
        if (var.controltype.strip() in 'state'):
            state_variable = var.varname
            #print var.varname
            value_transition_map = []
            for possible_value in var.possible_values:
                transitions = []
                for fsm in fsms:
                    for transition in fsm.transitions:
                        for action in transition.actions:
                            if (action.channel.channel_label.lower() == 'internal'):
                                state_variable = action.action_label.split('=')[0]
                                #print state_variable
                                if(state_variable.strip() in var.varname):
                                    value = action.action_label.split('=')[1]
                                    # print 'dump_state_variable_state_machines: action_label = ', action.action_label
                                    # print 'value = ', value
                                    # print 'possible_values = ', var.possible_values
                                    if(possible_value == value.strip()):
                                        #print (state_variable, var.initial_value, value, transition.transition_label)
                                        transitions.append(transition)
                if (len(transitions)>0):
                    value_transition_map.append((possible_value, transitions))

            if(len(value_transition_map) > 0):
                var_value_transition_map.append((var, value_transition_map))

    print ("--------- dump --------")
    for i in range(len(var_value_transition_map)):
        var = var_value_transition_map[i][0]
        state_variable = var.varname
        value_transition_map = var_value_transition_map[i][1]

        if(var.datatype == 'boolean'):
            file.write("\n\n\ninit(" + state_variable + ')\t:= ' + var.initial_value.upper() + '\t;\n') # TRUE and FALSE in uppercase
        elif(var.datatype == 'enumerate'):
            file.write("\n\n\ninit(" + state_variable + ')\t:= ' + var.initial_value + '\t;\n')

        file.write("\nnext(" + state_variable + ')\t:=\t case\n')
        for j in range(len(value_transition_map)):
            val = value_transition_map[j][0]
            transitions = value_transition_map[j][1]
            file.write('(')
            for k in range(len(transitions)):
                if(k == len(transitions)-1):
                    file.write(transitions[k].transition_label)
                else:
                    file.write(transitions[k].transition_label + ' | ')
            file.write(' )\t:\t' + val + '\t;\n')
        file.write('TRUE\t:\t' +  var.varname+ '\t;\n')
        file.write('esac\t;\n')

    return

def dump_seq_num_state_machines(file, seq_nums, fsms):
    seqnum_value_transition_map = []
    for seq_num in seq_nums:
        seqname = seq_num.seqname
        value_transition_map = []
        for possible_value in seq_num.possible_values:
            possible_value = possible_value.lstrip()
            #print 'possible_value = ', possible_value
            transitions = []
            for fsm in fsms:
                for transition in fsm.transitions:
                    for action in transition.actions:
                        if (action.channel.channel_label.lower() == 'internal'):
                            sname = str(action.action_label.split('=')[0]).strip()

                            if (seqname.strip() == sname):
                                #print 'sname = ', sname
                                next_value = str(action.action_label.split('=')[1]).strip()

                                if (possible_value == next_value.strip()):
                                    #print 'possible_value matched'
                                    transitions.append(transition)
            if (len(transitions) > 0):
                value_transition_map.append((possible_value, transitions))

        if (len(value_transition_map) > 0):
            seqnum_value_transition_map.append((seq_num, value_transition_map))


    print("--------- dump --------")
    file.write('\n\n')
    for i in range(len(seq_nums)):
        file.write('init(' + seq_nums[i].seqname + ')\t:= ' + seq_nums[i].start + '\t;\n')


    for i in range(len(seqnum_value_transition_map)):
        seqname = seqnum_value_transition_map[i][0].seqname
        value_transition_map = seqnum_value_transition_map[i][1]
        file.write('\nTRANS\n')
        file.write('case\n')
        for j in range(len(value_transition_map)):
            val = value_transition_map[j][0]
            transitions = value_transition_map[j][1]
            file.write('(')
            for k in range(len(transitions)):
                if (k == len(transitions) - 1):
                    file.write(transitions[k].transition_label)
                else:
                    file.write(transitions[k].transition_label + ' | ')
            file.write(' )\t:\tnext(' + seqname + ')\t=\t' +  val + '\t;\n')

        file.write('TRUE\t:\tnext(' + seqname + ')\t=\t' + seqname +'\t;\n')
        file.write('esac\t;\n')

    return

def dump_assigns(file, fsms):
    file.write('\n\nASSIGN\n\n')
    dump_state_machines(file, fsms)
    dump_action_state_machines(file, fsms)
    return


def draw_fsms(fsms):
    for fsm in fsms:
        fsm_digraph = 'digraph ' + fsm.fsm_label + '{\n'
        fsm_digraph += 'rankdir = LR;\n'
        fsm_digraph += 'size = \"8,5\"\n'
        for state in fsm.states:
            fsm_digraph += 'node [shape = circle, label=\"' + state + '\"]' + state + ';\n'

        for transition in fsm.transitions:
            fsm_digraph += transition.start + ' -> ' + transition.end + ' [label = \"' + transition.transition_label + ': '+ transition.condition + '/\n'
            for i in range(len(transition.actions)):
                if(i == len(transition.actions)-1):
                    fsm_digraph += transition.actions[i].action_label.lstrip()
                else:
                    fsm_digraph += transition.actions[i].action_label.lstrip() + ', '
            fsm_digraph += '\"]\n'
        fsm_digraph += '}\n'
        fsmOutPutFileName = fsm.fsm_label + '.dot'
        f = open(fsmOutPutFileName, "w")
        f.write(fsm_digraph)
        print (fsm_digraph)
        f.close()


    return






def usage():
    print ("usage:")
    print (str(os.path.basename(sys.argv[0]))+" fsm1Filename fsm2Filename")
 

