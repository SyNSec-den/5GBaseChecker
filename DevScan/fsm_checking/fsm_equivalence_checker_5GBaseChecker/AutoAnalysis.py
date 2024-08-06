import os
import sys
import json
import time

from Dot2FSM import get_states_and_tx, list_to_dict
import copy

folder_path = "./Result"  # Replace with the actual folder path

lines_with_bracket = []
deviant_trace = []
device_list = {
    "Qualcomm" : ["Redmagic", "Motorola", "Nothing", "OneplusNord", "SamsungS20", "Redmi", "Iphone", "Quectel", "Oneplus10pro"],
    "Mediatek" : ["Oppo","Rog"],
    "Unisoc": ["Hisense"],
    "Exynos": ["Pixel6", "Pixel7", "SamsungS21"],
    "Balong": ["Huawei"],
    "srsue": ["srsue"],
    "oaiue": ["oaiue"],
}

New = 0

def ExtractTrace():
    # Iterate through all files in the folder
    for filename in os.listdir(folder_path):
        file_path = os.path.join(folder_path, filename)

        # Check if the item in the folder is a file (not a subfolder)
        if os.path.isfile(file_path):
            with open(file_path, 'r') as file:
                # Read each line in the file
                for line in file:
                    if '[' in line:
                        content_before_slash = line.split('/')[0].replace('[', '')
                        lines_with_bracket.append([content_before_slash])

    unique_lines_with_bracket = []
    for item in lines_with_bracket:
        if item not in unique_lines_with_bracket:
            unique_lines_with_bracket.append(item)

    for i in range(len(unique_lines_with_bracket)):
        word_list = unique_lines_with_bracket[i][0].split()
        deviant_trace.append(word_list)

    # print (deviant_trace)
    return deviant_trace

def LQFSM(Query, FSMpath): #load a FSM and query it
    output = []

    states, transitions, start_state = get_states_and_tx(FSMpath)
    INIT_STATE = start_state
    Transitions = {}  # empty dict to store transitions

    state = INIT_STATE
    response = ""

    for i in transitions:
        Transitions.update(list_to_dict(i))

    for j in range(len(Query)):
        (response, dst_state) = Transitions[(state, Query[j])]
        state = dst_state
        output.append(response)

    return output

def ProcessResult(deviant_trace, folder_path): #The folder path should still be ./FSM
    Result = {} # Both key and value should be a list {str([Query anwser]) : [UEs] }
    device = []
    for filename in os.listdir(folder_path):
        file_path = os.path.join(folder_path, filename)
        response = LQFSM(deviant_trace, file_path)

        # now we are going to store the response into a dict, for each Query we will have a result dictionary
        key_to_search = str(response) #double check this line
        device_name = filename.split('.')[0]

        if key_to_search in Result:
            # Key already exists, update its value here if needed
            # print("Key already exist in the dict, append this device")
            current_list = Result[key_to_search]
            current_list.append(device_name)



        else:
            # Key doesn't exist, create a new entry
            # print("Added a new entry in dict!")
            empty_list = []
            empty_list.append(device_name)
            Result[key_to_search] = empty_list #device is a list here.

    printresult(Result)

    return Result

def printresult(Result):
    for key_str, value_list in Result.items():
        # Convert the key string back to a list for printing
        key_list = eval(key_str)

        # Print the key and value on separate lines
        print("Answer and device list")
        print(str(key_list) + '   ' + str(value_list))
        # print("Device:")
        # print(value_list)

def get_device_credit(device_name):
    for manufacturer, devices in device_list.items():
        if device_name in devices:
            credit = 0
            credit = 1.0 / len(devices)
            return credit
    return 0

def Majorityvote(Result,device_list):
    credits = {}
    # credit = 0 # credit for one response, multiple devices could have the same response
    # calculating credits for each answer(key in dict)

    for key, value in Result.items():
        credit = 0 # reset credit for each entry
        for j in range(len(value)):
            credit += get_device_credit(value[j])
        credits[key] = credit

    # print("credits for one query get!")
    # print(credits)
    return credits






def Write2File():
    pass



def rev_sort(trace_list):
    result_list = []

    str_list = []
    str_dict = {}

    for item in trace_list:
        temp_item = copy.deepcopy(item)
        list.reverse(temp_item)
        list_str = " ".join(temp_item)
        str_list.append(list_str)
        str_dict[list_str] = item

    list.sort(str_list)
    for item in str_list:
        result_list.append(str_dict[item])
    return result_list

def get_max_idx(val_list):
    max_val = -1
    max_idx = -1

    for idx in range(len(val_list)):
        if val_list[idx] >= max_val:
            max_idx = idx
            max_val = val_list[idx]

    return max_idx


if __name__ == "__main__":
    result_json = {}
    result_json["traces"] = []

    # if os.path.exists("elapsed_time.txt"):
    #     os.system("rm -f elapsed_time.txt")
    #
    # with open("elapsed_time.txt", 'w') as outfile:
    #     outfile.write("Comparison, time\n")
    #     outfile.close()


    deviant_trace = ExtractTrace()
    sorted_deviant_traces = rev_sort(deviant_trace)
    time.sleep(2)

    # print(sorted_deviant_traces)
    # sys.exit()



    for i in range(len(deviant_trace)):  # for each deviant trace, load each FSM and query, then log the result
        global NEW
        New = 0
        trace_dict = {}

        print("Deviant Trace:")
        if 'NEW' in deviant_trace[i]:
           deviant_trace[i].remove('NEW')
           New = 1
        if 'PATH:' in deviant_trace[i]:
           deviant_trace[i].remove('PATH:')
        print(deviant_trace[i])

        trace_dict["0_New"] = str(New)
        trace_dict["1_input_symbols"] = str(deviant_trace[i])

        Result = ProcessResult(deviant_trace[i], './FSM')

        trace_dict["2_outputs"] = []

        key_list = []
        for key in Result:
            key_list.append(key)

            output_dict = {}
            output_dict["1_output_symbols"] = key
            output_dict["2_devices"] = str(Result[key])

            if "Iphone" in output_dict["2_devices"] or "iphone" in output_dict["2_devices"]:
                correct_behavior = key

            trace_dict["2_outputs"].append(output_dict)

        trace_dict["9_duplicated"] = 0


        result_json["traces"].append(trace_dict)

    # checking all duplicates
    all_old_paths = set()
    for item in result_json["traces"]:
        if item["0_New"] != "1":
            all_old_paths.add(item["1_input_symbols"])

    all_new_paths = set()
    for item in result_json["traces"]:
        if item["0_New"] == "1":
            if item["1_input_symbols"] in all_old_paths:
                item["9_duplicated"] = 1
                all_new_paths.add(item["1_input_symbols"])
            elif item["1_input_symbols"] in all_new_paths:
                item["9_duplicated"] = 2

    # removing all duplicates
    final_traces = []
    for item in result_json["traces"]:
        if item["9_duplicated"] == 0:
            final_traces.append(item)

    result_json["traces"] = final_traces

    for item in result_json["traces"]:
        del item["0_New"]
        del item["9_duplicated"]


    with open("deviant-queries.json", 'w') as outfile:
        json.dump(result_json, outfile, indent=2, sort_keys=True)
        outfile.close()

    input_file = open("deviant-queries.json", 'r')
    input_lines = input_file.readlines()
    input_file.close()

    for idx in range(len(input_lines)):
        input_lines[idx] = (input_lines[idx].replace("1_input_symbols", "input_symbols")
                            .replace("2_outputs", "outputs")
                            .replace("1_output_symbols", "output_symbols")
                            .replace("2_devices", "devices")
                            .replace("3_vote_result", "vote_result")
                            .replace("4_voted_behavior", "voted_behavior")
                            .replace("5_correct_behavior", "correct_behavior")
                            .replace("6_underspecified", "underspecified")
                            .replace("7_unsure", "unsure")
                            .replace("8_checked", "checked")
                            .replace("9_duplicated", "duplicated"))

    with open("deviant-queries.json", 'w') as outfile:
        outfile.writelines(input_lines)
        outfile.close()





















