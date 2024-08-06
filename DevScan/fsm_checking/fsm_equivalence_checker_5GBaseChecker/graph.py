# Python program to print all paths from a source to destination.
import os
import sys
from collections import defaultdict
from Dot2FSM import get_states_and_tx, list_to_dict

global_graph_dict = {}

# This class represents a directed graph
# using adjacency list representation
class Graph:

    def __init__(self, vertices, vertices_names):
        # No. of vertices
        self.V = vertices

        self.vertices_names = vertices_names

        # default dictionary to store graph
        self.graph = defaultdict(list)

        self.path = None
        self.visited = None

        self.all_paths_to_state = {}

    # def addpath(self,path):
    #     self.all_path.append(path)
    #
    # def getallpath(self):
    #     print(self.all_path)

    # function to add an edge to graph
    def addEdge(self, u, v):
        self.graph[u].append(v)

    def printGraph(self):
        print("self.v :", self.V)
        print("self.vertices_names :", self.vertices_names)
        print("self.graph :", self.graph)

    def getgraph(self,u):
        return self.graph[u]

    '''A recursive function to print all paths from 'u' to 'd'.
    visited[] keeps track of vertices in current path.
    path[] stores actual vertices and path_index is current
    index in path[]'''

    def printAllPathsUtil(self, u, d):

        # Mark the current node as visited and store in path
        self.visited[u] = True
        self.path.append(u)

        # If current vertex is same as destination, then print
        # current path[]
        if u == d:
            print(self.path)
            with open('graphout.txt', 'a') as file:
                file.write(str(self.path) + '\n')

        else:
            # If current vertex is not destination
            # Recur for all the vertices adjacent to this vertex
            # self.printGraph()
            for i in self.graph[u]:
                if self.visited[i] == False:
                    self.printAllPathsUtil(i, d)

        # Remove current vertex from path[] and mark it as unvisited
        self.path.pop()
        self.visited[u] = False

    # Prints all paths from 's' to 'd'
    def printAllPaths(self, s, d):
        if os.path.exists('./graphout.txt'):
            os.system("rm ./graphout.txt")
        # Mark all the vertices as not visited
        self.visited = {}
        for v in self.vertices_names:
            self.visited[v] = False
        # visited = [False] * (self.V)

        # Create an array to store paths
        self.path = []

        # Call the recursive helper function to print all paths
        self.printAllPathsUtil(s, d)

        self.path = None
        self.visited = None


def LoadFSM(FSMpath): #load a FSM and query it
    # print(FSMpath)

    states, transitions, start_state = get_states_and_tx(FSMpath)
    INIT_STATE = start_state
    Transitions = {}  # empty dict to store transitions

    state = INIT_STATE
    response = ""

    for i in transitions:
        Transitions.update(list_to_dict(i))


    return states, transitions, Transitions, INIT_STATE

def loadallpath():
    file_path = './graphout.txt'  # Replace with the actual path to your file
    if os.path.isfile(file_path):
        file = open(file_path, 'r')
        all_path = {}
        for line in file.readlines():
            line = line.strip()
            # Read the file line by line and store each line as a list
            all_path[line] = eval(line)

        # for lst in all_path:
        #     print(lst)

        # no duplicate paths
        all_path = all_path.values()
        if os.path.exists('./graphout.txt'):
            os.system("rm ./graphout.txt")
        return all_path
    else:
        all_path = []
        print("The file does not exist.")
        return all_path

def getTracefrompath(fsm_list, all_path):
    # this function will return 2 lists contains all deviant queries and corresponding output

    all_input_traces = [] # this is the return value
    all_output_traces = []

    for item in all_path: #item is a single list
        single_input_trace = []
        single_output_trace = []
        for i in range(len(item)-1): # each round get one deviant trace and append
            state1_num = item[i]
            state2_num = item[i+1] # each time we get 2 states out and search them in the list, see what's the input/output
            state1 = str(state1_num)
            state2 = str(state2_num)
            for transitions in fsm_list:
                if transitions[0] == state1 and transitions[3] == state2:
                    deviant_input = transitions[1]
                    deviant_output = transitions[2]
                    single_input_trace.append(deviant_input)
                    single_output_trace.append(deviant_output)
                    # break # ?
        all_input_traces.append(single_input_trace) # final return value here
        all_output_traces.append(single_output_trace)

    return all_input_traces, all_output_traces



def get_all_paths(dot_filename, dst_state):
    global global_graph_dict

    States, FSM_List, FSM_dict, init_state = LoadFSM(dot_filename)  # each list contains a transition e.g. ['s0', 'enable_s1', 'registration_request', 's1']
    print ("FSM_dict got!")
    if init_state == dst_state:
        return [], [], []

    # start creating the graph
    if not dot_filename in global_graph_dict:
        global_graph_dict[dot_filename] = Graph(len(States), vertices_names=States)
        # Convert FSM to graph
        for item in FSM_List:
            Source_state = item[0]
            Dest_state = item[3]
            # Source_state_num = int(Source_state)
            # Dest_state_num = int(Dest_state)
            if Source_state != Dest_state:
                graph_u = global_graph_dict[dot_filename].getgraph(Source_state)
                if Dest_state in graph_u:
                    # print ("Dest_state_num already exist!")
                    pass
                else:
                    global_graph_dict[dot_filename].addEdge(Source_state, Dest_state)

    # print("Following are all different paths from {} to {} :".format(init_state, dst_state))

    if dst_state in global_graph_dict[dot_filename].all_paths_to_state:
        all_path = global_graph_dict[dot_filename].all_paths_to_state[dst_state]
    else:
        global_graph_dict[dot_filename].printAllPaths(init_state, dst_state)
        all_path = loadallpath()

        # check problematic paths
        temp_paths = []
        for path in all_path:
            if len(path) <= global_graph_dict[dot_filename].V:
                temp_paths.append(path)
            else:
                print("ERROR: PATH LARGER THAN NUMBER OF STATES!!!")
                print(path)
                sys.exit()

        all_path = temp_paths
        global_graph_dict[dot_filename].all_paths_to_state[dst_state] = all_path
        # sys.exit()

    all_input_seqs, all_output_seqs = getTracefrompath(FSM_List, all_path)
    # print(all_input_seqs)
    # print (all_output_seqs)

    return all_path, all_input_seqs, all_output_seqs




# if __name__ == '__main__':
#
#     all_paths = get_all_paths("./FSM/FSM2-5G.dot", "s8")
#
#     # all_path = []
#     # deviant_input = []
#     # deviant_output = []
#     #
#     # States, FSM_List, FSM_dict, init_state = LoadFSM("./FSM/Hisense.dot") # each list contains a transition e.g. ['s0', 'enable_s1', 'registration_request', 's1']
#     # print ("FSM_dict got!")
#     #
#     # os.system("rm ./graphout.txt")
#     #
#     # # start creating the graph
#     # g = Graph(len(States))
#     #
#     # # Convert FSM to graph
#     # for item in FSM_List:
#     #     Source_state = item[0][1:]
#     #     Dest_state = item[3][1:]
#     #     Source_state_num = int(Source_state)
#     #     Dest_state_num = int(Dest_state)
#     #     if Source_state != Dest_state:
#     #         graph_u = g.getgraph(Source_state_num)
#     #         if Dest_state_num in graph_u:
#     #             print ("Dest_state_num already exist!")
#     #         else:
#     #             g.addEdge(Source_state_num, Dest_state_num)
#     #
#     #
#     # # s means source, d means destination
#     # s = 0
#     # d = 11
#     # print("Following are all different paths from % d to % d :" % (s, d))
#     # g.printAllPaths(s, d)
#     #
#     # all_path = loadallpath()
#     # print(all_path)
#     #
#     # deviant_input, deviant_output = getTracefrompath(FSM_List,all_path)
#     # print(deviant_input)
#     # print (deviant_output)
#     #
#     #
#     #
#     #
#     #
#     #
#     #
#     #
#     #
#     #
#     #
#     #
#     #
#     #
#     # # # Create a graph given in the above diagram
#     # # g = Graph(4)
#     # # g.addEdge(0, 1)
#     # # g.addEdge(0, 2)
#     # # g.addEdge(0, 3)
#     # # g.addEdge(2, 0)
#     # # g.addEdge(2, 1)
#     # # g.addEdge(1, 3)
#     # #
#     # # s = 2
#     # # d = 3
#     # # print("Following are all different paths from % d to % d :" % (s, d))
#     # # g.printAllPaths(s, d)
#

