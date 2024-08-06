import os
import time
from itertools import combinations
import subprocess
import glob

# os.system()

file_names = []
folder_path = './FSM'


def get_file_names_in_folder(folder_path):
    file_names = []

    for filename in os.listdir(folder_path):
        if os.path.isfile(os.path.join(folder_path, filename)):
            file_names.append(filename)

    return file_names

def get_pairwise_combinations(file_names):
    pairwise_combinations = []

    for file1, file2 in combinations(file_names, 2):
        pairwise_combinations.append((file1, file2))

    print(len(pairwise_combinations))

    return pairwise_combinations

def checkfile(first_fsm, second_fsm):
    Result_file_names = []
    Result_folder_path = './Result'
    for filename in os.listdir(Result_folder_path):
        # if os.path.isfile(os.path.join(folder_path, filename)):
        Result_file_names.append(filename)

    target_file = first_fsm + '_vs_' + second_fsm + '_final'
    if target_file in Result_file_names:
        # print ("target file exist")
        return 0
    else:
        # print ("Start comparing")
        return 1


def generate_cmd(pairwise_combinations):
    for i in range(len(pairwise_combinations)):
        #todo: add file existence check
        first_fsm = pairwise_combinations[i][0]
        second_fsm = pairwise_combinations[i][1]
        file_exist = checkfile(first_fsm,second_fsm) # 0 means exist, 1 means not exist
        if file_exist == 1:
            cmd = 'python2.7 ./iterative_checker.py ' + '-lts1 FSM/' + first_fsm + ' -lts2 FSM/' + second_fsm + ' -o Result/' + first_fsm + '_vs_' + second_fsm
            print(cmd)
            file_path = "pair_time.txt"
            # Open the file in write mode and write the elapsed time to it
            with open(file_path, 'a') as file:
                file.write(first_fsm + " " + second_fsm +"\n" )
            file.close()
            # os.system(cmd)
            # time.sleep(5 * 60)  # 4 mins for each pair of comparison


            subprocess.call(cmd, shell=True, stdout=subprocess.DEVNULL)

        else:
            print("file already exist")

        # WD = os.getcwd()
        # print(WD)


def delete_temp_files(directory):
    # Create a pattern to match all files that contain 'temp' in their filename
    pattern = os.path.join(directory, '*temp*')

    # Use glob to find all files matching the pattern
    temp_files = glob.glob(pattern)

    # Iterate over the list of file paths and remove each file
    for file_path in temp_files:
        try:
            os.remove(file_path)
        except Exception as e:
            print(f"Failed to delete {file_path}: {str(e)}")

if __name__ == "__main__":

    # if Result/ folder does not exist create it
    dir_name = "Result"
    if not os.path.exists(dir_name):
        # Create the directory
        os.makedirs(dir_name)
    delete_temp_files(folder_path)
    file_names = get_file_names_in_folder(folder_path)
    file_names.sort()
    pairwise_combinations = get_pairwise_combinations(file_names)
    generate_cmd(pairwise_combinations)
    delete_temp_files(folder_path)






