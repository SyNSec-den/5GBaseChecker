import os
from Dot2FSM import get_states_and_tx, list_to_dict


folder_path = './FSM'

def TransNum(FSMpath): #Output transition number of a FSM
    Transitions_num = 0
    states, transitions, start_state = get_states_and_tx(FSMpath)

    for trans in transitions:
        if trans[0] != trans[3]:
            Transitions_num = Transitions_num + 1


    return str(len(states)), Transitions_num

if __name__ == "__main__":
    for filename in os.listdir(folder_path):
        file_path = os.path.join(folder_path, filename)
        states_num, Transitions_num = TransNum(file_path)
        print(filename + ': ' + states_num + ' ' + str(Transitions_num))

