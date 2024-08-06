#!/usr/bin/env python
"""
simple script to visualize the trace output of smv / NuSMV
via Graphviz's dot-format. first, the trace is parsed and
then coded as dot-graph with states as nodes and input 
(transitions) as arcs between them. even if the counterexample's
loop start- and end-state are the same, they are represented by
two different nodes as there can be differences in the completeness
of the state variables' representation.

this is only a simple hack to get quick and dirty trace graphs ;-)
"""
import os,sys,getopt
from collections import OrderedDict
import subprocess
from iterative_checker import parseDOT, dump_variables, dump_states, dump_actions, dump_defines, dump_assigns
digraph = ""
import re
import time
try:
   import pydot
except:
   print ("this module depends on pydot\nplease visit http://dkbza.org/ to obtain these bindings")
   sys.exit(2)

# CHANGE HERE:
VIEW_CMD="gv -antialias" #used as: VIEW_CMD [file]
DOT_CMD="dot -Tps -o" #used as:    DOT_CMD [outfile] [infile]
TEMPDIR="/tmp"        #store dot-file and rendering if viewmode without output-file


# for internal purposes, change only, if you know, what you do


class State(object):
    def __init__(self, label, input, action1, action2, transition1, transition2):
        self.label = label
        self.input = input
        self.action1 = action1
        self.action2 = action2
        self.transition1 = transition1
        self.transition2 = transition2

    def set_transition1(self,transition1):
        transition1 = []
        for transition in transition1:
            self.transition1.append(transition)
    def set_transition2(self,transition1):
        transition2 = []
        for transition in transition2:
            self.transition2.append(transition)


class Transition(object):
    def __init__(self, action_label, value):
        self.action_label = action_label
        self.value = value


DEBUG=False
PROFILE=False
PSYCO=True
if PSYCO:
   try:
      import psyco
   except (ImportError, ):
      pass
   else:
      psyco.full()
      if DEBUG: print ("psyco enabled")



def trace2dotlist(traces):
    """this function takes the trace output of (nu)smv as a string;
    then, after cleaning the string of warnings and compiler messages,
    decomposes the output into separate single traces which are translated
    to dot-graphs by _singletrace2dot. as a traceoutput can combine several
    traces, this method returns a list of the dot-graphs"""

    # beautify ;-)

    lines = [line for line in traces if not (line.startswith("***") or
      line.startswith("WARNING") or line == "\n" or line == '')]

    map(lambda x: x.lstrip("  "), lines)

    #print ("Printing lines .....")
    #for l in lines:
        #print l
    #print("EOL.......")
    # cut list at each "-- specification"
    index=0
    trace_list=[]
    trace=[]
    # trace_list = traces for multiple properties.
    # each trace consists of sequence of states.
    # each state consists of a list of variables and their values
    '''
    for line in lines:
        if (line.startswith("-- no counterexample found with bound") or "" in line):
            index = lines.index(line)
            continue
        elif line.startswith("-- specification"):
            # TODO: need to commemnt out the following line
            #formulae = line.rstrip("is false\n").lstrip("-- specification")
            #print ('formulae = ', formulae)
            last = index
            index = lines.index(line)
            trace_list.append(lines[last: index])
    trace_list.append(lines[index: len(lines)])
    '''
    for line in lines:
        if "specification" in line or "as demonstrated by the following execution sequence" in line or "Trace Description: LTL Counterexample " in line or "Trace Type: Counterexample " in line:
            continue
        trace.append(line)
    trace_list.append(trace)
    #print len(trace_list)
    #print trace_list
    #sort out postive results. And filter out the empty trace.
    trace_list = [trace for trace in trace_list if len(trace)>1 and not str(trace[0]).endswith("true")]

    #print ('### trace_list = #### ', trace_list)

        # Draw graph for each trace
    graph=[]
    for trace in trace_list:
        #print "IK"
        #print trace
        graph.append(_singletrace2dot(trace,True))
   
    return graph


def _singletrace2dot(trace,is_beautified=False):
    """translate a single trace into a corresponding dot-graph;
    wheras the parsing assumes a correct trace given as
    trace ::=  state ( input state )*
    """

    # if not is_beautified:
    #     lines = [line for line in trace if not (line.startswith("***") or
    #         line.startswith("WARNING") or line == "\n"
    #              or line.startswith("-- specification") or line.startswith("-- as demonstrated")
    #              or line.startswith("Trace Description: ") or line.startswith("Trace Type: "))]
    #     map(lambda x: x.lstrip("  "), lines)
    # else:
    #     lines = trace

    # strip the headers of each trace.
    global digraph
    lines = []
    #print ('trace = ', trace)
    for line in trace:
        #print(line)
        if( not (line.startswith("***") or
            line.startswith("WARNING") or line == "\n"
                 or line.startswith("-- specification") or line.startswith("-- as demonstrated")
                 or line.startswith("Trace Description: ") or line.startswith("Trace Type: "))):
            lines.append(line.lstrip("  "))

    #print (lines)
    #slice list at "->"
    index=0
    states=[]
    for item in lines:
        #print ('item = ', item)
        if item.startswith("->"):
            last=index
            index=lines.index(item)
            states.append(lines[last:index]) # the first state is empty
    states.append(lines[index:len(lines)])

    #print ('states', states)

    lines=False #free space!
   
    graph = pydot.Graph()

    loop=False #flag to finally add an additional dotted edge for loop
    
    #print states[1][0]
    assert states[1][0].startswith("-> State:") #starting with state!

    digraph = 'Digraph G{\n'
    digraph += 'rankdir=LR\n'
    stateVariablesDict = OrderedDict()
    counter = 0
    for item in states[1:]: #first item is header
        name= item[0].lstrip("-> ").rstrip(" <-\n")
        if (name.startswith("State")):
            state=name.lstrip("State: ")
            node=pydot.Node(state)
            props=name+'\\n' #to reach pydotfile: need double '\'
            digraph =  digraph + 'S' + str(counter) + '[shape=box,label=\"' + name + '\\n'
            counter = counter + 1
            #print (name)
            for i in (item[1:]):
                #props+=i.rstrip('\n')
                #props+="\\n"
                isNewValue = False
                s = str(i).rstrip('\n')
                variable = s[:s.rfind('=')].strip()
                value = s[s.rfind('=')+1:].strip()

                if(variable not in stateVariablesDict):
                    isNewValue = False
                else:
                    (val, newValInd) = stateVariablesDict[variable]
                    if(str(val) != str(value)):
                        isNewValue = True
                stateVariablesDict[variable] = (value, isNewValue)

            #stateVariablesList = [[k, v] for k, v in stateVariablesDict.items()]

            for var, (val, newValInd) in stateVariablesDict.items():
                if(newValInd == True):
                    props += '*' + str(var) + ' = ' + str(val) + '\\n'
                    digraph = digraph + '*' + str(var) + ' = ' + str(val) + '\\n'
                else:
                    props += str(var) + ' = ' + str(val) + '\\n'
                    digraph = digraph + str(var) + ' = ' + str(val) + '\\n'

            node.set_label('"'+props+'"')

            digraph = digraph + '\"]\n'

            graph.add_node(node)

            for var, (val, newValInd) in stateVariablesDict.items():
                stateVariablesDict[var] = (val, False)


        elif name.startswith("Input"):
            assert state #already visited state
            trans=name.lstrip("Input: ")
            edge=pydot.Edge(state,trans)

            hasLoop = [it for it in item[1:] if it.startswith("-- Loop starts here")]
            #TODO: check trace-syntax, if this can happen only in the last line of a transition
            #      then list-compreh. can be avoided
            if hasLoop:
                loop=state #remember state at which loop starts
                item.remove(hasLoop[0])

            props=""
            for i in (item[1:]):
                props+=i.rstrip('\n')
                props+="\\n"
                edge.set_label(props)
                graph.add_edge(edge)

        else:
            assert False #only states and transitions!

    if loop:
        edge=pydot.Edge(state,loop)
        edge.set_style("dotted,bold")
        edge.set_label(" LOOP")
        graph.add_edge(edge)

    for i in range(1, counter):
        digraph = digraph + 'S' + str(i-1) + ' -> ' + 'S' + str(i) + '\n'
    digraph = digraph + '\n}\n'

    return graph



def get_states(nodes):
    states = []
    #nodes.sort(key=lambda x: x.label, reverse=False)
    for i in range(0,len(nodes)):
        node = nodes[i].to_string()
        node_list = node.split("\\n")
        #print (node_list)
        transition1 = []
        transition2 = []
        for field in node_list:
            if "State: " in field:
                label_list = re.findall("\d+\.\d+", field)
                label = label_list[0]
                #print 'State label = ', label

            if "input" in field:
                input = field.split("= ",1)[1] 
                #print 'input = ', input


            if "BLE1_" in field:
                remaining = field.split("T",1)[1]
                #print 'remaining = ', remaining
                #label_t1 = [int(i) for i in remaining.split() if i.isdigit()]
                label_t1 = remaining.split('=')[0].strip()
                value =  remaining.split("= ",1)[1].strip()
                tran1 = Transition(label_t1,value)
                if('true' in value.lower()):
                    #print (label_t1, value)
                    transition1.append(tran1)
                    tlabel = "BLE1_T" + str(label_t1)
                    #print tlabel, value

            if "BLE2_" in field:
                remaining = field.split("T",1)[1] 
                #label_t1 = re.findall("\d", remaining)
                #label_t2 = [int(i) for i in remaining.split() if i.isdigit()]
                # value =  remaining.split("= ",1)[1]

                label_t2 = remaining.split('=')[0].strip()
                value = remaining.split("= ", 1)[1].strip()
                tran2 = Transition(label_t2,value)
                if ('true' in value.lower()):
                    #print (label_t2, value)
                    transition2.append(tran2)
                    tlabel = "BLE2_T" + str(label_t2)
                    #print tlabel, value


            if "ble1_action" in field:
                action1 = field.split("= ",1)[1].strip()
                #print 'action 1 = ', action1

            if "ble2_action" in field:
                action2 = field.split("= ",1)[1].strip()
                #print 'action 2 = ', action2

        state = State(label,input,action1,action2,transition1,transition2)
        #print state.label
        states.append(state)
        
    states.sort(key=lambda x: x.label, reverse=False)
    return states


def compute_diff(nuxmfilename, commandfilename, outputfilename, input_condition = None, fsm1_action=None, fsm2_action=None, fsm1_inFile=None, fsm2_inFile=None):
  
    fsm1_label = 'BLE1'
    fsm2_label = 'BLE2'
    fsm1 = parseDOT(fsm1_inFile, fsm1_label) # return (fsm, env_vars)
    fsm2 = parseDOT(fsm2_inFile, fsm2_label) # return (fsm, env_vars)

    

    incoming_messages = []
    for in_msg in fsm1.incoming_messages:
        in_msg = in_msg.strip()
        if in_msg not in incoming_messages and in_msg not in 'null_action':
            incoming_messages.append(in_msg)

            
    for in_msg in fsm2.incoming_messages:
        in_msg = in_msg.strip()
        if in_msg not in incoming_messages and in_msg not in 'null_action':
            incoming_messages.append(in_msg)



    f = open(nuxmfilename, "w")
    f.write("MODULE main\n")
 
            
    dump_variables(f, incoming_messages)

    dump_states(f, (fsm1, fsm2))
    dump_actions(f, (fsm1, fsm2))
    
    dump_defines(f, (fsm1, fsm2))
    dump_assigns(f, (fsm1, fsm2))
    f.close()
    output_list = []
    outputfilename_time = outputfilename+"_time"
    if outputfilename_time:
        outputfile_time = open(outputfilename_time, 'a')
    else:
        import tempfile
        tempdir = tempfile.mkdtemp(dir=TEMPDIR)
        outputfile_time = os.path.join(tempdir, "trace")
        outputfile_time = open(outputfilename_time, 'a')
    outputfile_time.write("\n New Round Starting \n")
    print (" ########## Equivalence Checking for <output1: " +""+fsm1_action + ", output2: "+  fsm2_action + "> diversity class ############")

    # -----------------------------------
    nuxmv  = open(nuxmfilename, 'r').readlines()
    nuxmfile = open(nuxmfilename, 'w')
    is_ltlspec_present = False
    ltlspec_index = -1
    for i in range(len(nuxmv)):
        #print (nuxmv[i])
        if "LTLSPEC " in nuxmv[i]:
            is_ltlspec_present = True
            ltlspec_index = i
            break
        else:
            nuxmfile.write(nuxmv[i])

    if not fsm1_action or not fsm2_action:
        exit(0)

    if not is_ltlspec_present:
        ltlspec =  "\n\nLTLSPEC ( \n G( ! (input = "+ input_condition + " & ble1_action = " + fsm1_action + " & ble2_action = " + fsm2_action + "))\n);"
        print("LTL Property:")
        print (ltlspec)
        nuxmfile.write(ltlspec)
    else:
        nuxmv[ltlspec_index] = "LTLSPEC ( \n G( ! (input = "+ input_condition + " & ble1_action = " + fsm1_action + " & ble2_action = " + fsm2_action + "))\n);"
        print("LTL Property:\n")
        print (nuxmv[ltlspec_index])
        nuxmfile.write(nuxmv[ltlspec_index])
    nuxmfile.close()
    # ----------------------------------------------
    bashCommand = "./nuXmv EQCHECK.smv > ctx"
    cmd = ['./nuXmv','-source', commandfilename, nuxmfilename]

    iter = 0

    while True:
        states = []
        offen_state = 0
        start_time = time.time()
        process = subprocess.Popen(cmd, stdout=subprocess.PIPE)
        output, error = process.communicate()
        #print(output)
        total_time = (time.time() - start_time)
        outputfile_time.write('Duration: {}'.format(time.time() - start_time))
        outputfile_time.write("\n")
        print (output)
        if "is true" in output:
            break

        #print ("reached loop")
        trace = output.split("\n")
       
        if "State" in trace[-2]:
            del trace[-2]
        graph = trace2dotlist(trace)
        nodes = graph[0].get_nodes()
        states = get_states(nodes)
        #if outputfilename:
        #    outputfile = open(outputfilename, 'a')
        #else:
        #    import tempfile
        #    tempdir = tempfile.mkdtemp(dir=TEMPDIR)
        #    outputfilename = os.path.join(tempdir, "trace")
        #    outputfile = open(outputfilename, 'a')

        ctx_inputs = ""
        ue1_ctx_outputs = ""
        ue2_ctx_outputs = ""
        input_list = []
        out_list1 = []
        out_list2 = []
        for state in states:
            #print (state.action1, state.action2)
            trans1 = state.transition1
            trans2 = state.transition1
            #print "Len: "+str(len(trans1))+str(len(trans1))+"\n"
            #for i in range(0,len(trans1)):
                #print  "T " +trans1[i].action_label+" "+str(offen_state)+" "+str(i) +" index: "+str(states.index(state))+"\n"

            if state.action1 == fsm1_action and state.action2 == fsm2_action and state.input == input_condition:               
                offen_state = states.index(state) - 1
                print ("#### offen_state =" + str(offen_state) + " #######")
                print (states[offen_state + 1].action1, states[offen_state + 1].action2)
                print (states[offen_state].input)
                break
            ctx_inputs = ctx_inputs.lstrip() + " " + state.input
            input_list.append(state.input)
            out_list1.append(states[states.index(state) + 1].action1)
            out_list2.append(states[states.index(state) + 1].action2)
            ue1_ctx_outputs = ue1_ctx_outputs.lstrip() + " " + states[states.index(state) + 1].action1
            ue2_ctx_outputs = ue2_ctx_outputs.lstrip() + " " + states[states.index(state) + 1].action2
        print("input_condition")
        print(input_condition)
        print("input_list[len(input_list)-1]")
        print(input_list)
        print(input_list[len(input_list)-1])
        if input_condition == input_list[len(input_list)-1]: # and "attach_request" == out_list1[0] and "attach_request" == out_list2[0]:
            print("input_condition")
            print(input_condition)
            print("input_list[len(input_list)-1]")
            print(input_list[len(input_list)-1])
            s =  "[" + ctx_inputs + " / " + ue1_ctx_outputs + "] [" + ctx_inputs + " / " + ue2_ctx_outputs + "]"
            output_list.append(s)
            print("output_list line 448:")
            print(s)
            # outputfile.write(" ")
            #outputfile.write("\n")
            #outputfile.close()
        else:
            break
        #print str(offen_state) + " meh\n"
        trans1 = states[offen_state+1].transition1
        trans2 = states[offen_state+1].transition2
        if (len(trans1) == 0 and len(trans2) == 0):
            trans1 = states[offen_state].transition1
            trans2 = states[offen_state].transition2
        if (len(trans1) == 0 and len(trans2) == 0):
            trans1 = states[offen_state-1].transition1
            trans2 = states[offen_state-1].transition2
        #print str(len(trans1)) + str(len(trans2)) + "len \n"
        # offending_labels_t1 = []
        offending_label_t1 = None
        # offending_labels_t2 = []
        offending_label_t2 = None

        #print (len(trans1), len(trans2))

        for t1 in trans1:
            #print "T1 "+str(t1.action_label)+" "+t1.value +"\n"
            if (t1.value.upper() in "TRUE"):
                offending_label_t1 = str(t1.action_label)
                #print ('offending_label_t1 = ', str(offending_label_t1))

        for t2 in trans2:
            #print "T2 "+str(t2.action_label)+" "+t2.value+"\n"
            if (t2.value.upper() in "TRUE"):
                offending_label_t2 = str(t2.action_label)
                #print ('offending_label_t2 = ', str(offending_label_t2))

        #print (str(offending_label_t1), str(offending_label_t2))

        #outputfile.close()
        
        nuxmv = open(nuxmfilename, 'r').readlines()
        nuxmfile = open(nuxmfilename, 'w')

        for i in range(0, len(nuxmv)):
            
            #print nuxmv[i]
            if "LTLSPEC (" in nuxmv[i] and iter == 0:
                #print "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"
                invariant = ""
                if (offending_label_t1):
                    invariant = "G(!BLE1_T" + offending_label_t1
                if (offending_label_t1 and offending_label_t2):
                    invariant = invariant + " & "+"!BLE2_T" + offending_label_t2 
                if (not offending_label_t1 and offending_label_t2):
                    invariant = invariant + "G(!BLE2_T" + offending_label_t2
                invariant = invariant + ")" + "\n" + "->" + "\n"
                #print invariant
                nuxmv[i] = nuxmv[i] + invariant
                #print offending_label_t1
                #print offending_label_t2
                #print invariant
            elif "G(!" in nuxmv[i] and iter != 0:
                #print nuxmv[i]
                first = nuxmv[i].split(")", 1)[0]

                if (offending_label_t1):
                    nuxmv[i] = first + " & !BLE1_T" + offending_label_t1
                if (offending_label_t2):
                    nuxmv[i] = first + " & !BLE2_T" + offending_label_t2
                nuxmv[i] = nuxmv[i] + ")" + "\n"
                
            nuxmfile.write(nuxmv[i])
        iter = iter + 1
        nuxmfile.close()

    else:
        outputfile = sys.stdout
    
    outputfile_time.close()
    return output_list

def usage():
    print ("usage:")
    print (str(os.path.basename(sys.argv[0]))+" nuXmvFilename CommandFilename OutputFilename")
  
def main():
    global digraph
    '''
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hvo:", ["view","help","output="])
    except getopt.GetoptError:
        # print help information and exit:
        usage()
        sys.exit(2)
    '''
    if len(sys.argv)<4:
        usage()
        sys.exit(2)

    nuxmfilename = None
    commandfilename = None
    outputfilename = None
    verbose = False
    view=False
    tempdir=None
    '''
    for o, a in opts:
        if o in ("-h", "--help"):
            usage()
            sys.exit()
        if o in ("-o", "--output"):
            outputfilename = a
        if o == "--view":
            view=True
    '''
  
    filename="ctx"

    nuxmfilename = sys.argv[1]
    commandfilename = sys.argv[2]
    outputfilename = sys.argv[3]
    #print nuxmfilename

    compute_diff(nuxmfilename, commandfilename, outputfilename)
    return

#
if __name__=="__main__":
    if DEBUG:
        # for post-mortem debugging
        import pydb,sys
        sys.excepthook = pydb.exception_hook
    elif PROFILE:
        if PSYCO:
            raise (Exception, "cannot profile whilst using psyco!!!")
        import hotshot
        prof = hotshot.Profile("_hotshot",lineevents=1)
        prof.runcall(main)
        prof.close()
    else:
        main()
