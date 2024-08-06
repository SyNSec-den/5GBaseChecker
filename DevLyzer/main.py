import ast
import json
import subprocess
import time

start = time.time()


def string_to_list(input_string):
    try:
        result_list = ast.literal_eval(input_string)
        if isinstance(result_list, list):
            return result_list
        else:
            return None
    except (SyntaxError, ValueError):
        return None


def read_ltl_command():
    ltl_properties = []
    with open('attack_trace_checker/ltl_properties.txt', 'r') as fr:
        lines = fr.readlines()
        for line in lines:
            ltl_properties.append(line.split("\n")[0])

    ltl_properties_count = dict()

    for i in range(len(ltl_properties)):
        ltl_properties_count[i + 1] = 0

    return ltl_properties, ltl_properties_count


def create_inputs():
    all_inp = []
    with open('attack_trace_checker/all_inputs.txt', 'r') as fr:
        lines = fr.readlines()
        for line in lines:
            curr_inp = line.split("\n")[0].strip()
            all_inp.append(curr_inp)
    return list(set(all_inp))


def create_outputs():
    all_outp = []
    with open('attack_trace_checker/all_outputs.txt', 'r') as fr:
        lines = fr.readlines()
        for line in lines:
            curr_outp = line.split("\n")[0].strip()
            all_outp.append(curr_outp)

    return list(set(all_outp))


def create_vars(input_sequence):
    var_str = "VAR\n\n"
    all_inputs = create_inputs()
    all_outputs = create_outputs()
    num_states = len(input_sequence) + 1

    var_input = "input: { "
    for i in range(len(all_inputs)):
        var_input += all_inputs[i]
        if i != len(all_inputs) - 1:
            var_input += ", "

    var_input += " };\n"

    var_output = "output: { "
    for i in range(len(all_outputs)):
        var_output += all_outputs[i]
        if i != len(all_outputs) - 1:
            var_output += ", "

    var_output += ", output_null_action };\n"

    var_state = "state: { "
    for i in range(num_states):
        var_state += "S{}".format(i)
        if i != num_states - 1:
            var_state += ", "

    var_state += " };\n\n"

    return var_str + var_input + var_output + var_state


def case_for_state(input_seq):
    state_counter = 0
    state_str = "init(state) := S{};\n".format(state_counter)
    state_str += "next(state) := case\n"
    for state_num in range(len(input_seq)):
        state_str += "state = S{} & input = {} : S{} ;\n".format(state_num, input_seq[state_num], state_num + 1)
    state_str += "TRUE : state ;\n"
    state_str += "esac ; \n\n"

    return state_str


def case_for_output(input_seq_, output_seq_):
    output_str = "init(output) := output_null_action;\n"
    output_str += "next(output) := case\n"
    for state_num in range(len(input_seq_)):
        output_str += "state = S{} & input = {} : {} ;\n".format(state_num, input_seq_[state_num],
                                                                 output_seq_[state_num])
    output_str += "TRUE : output_null_action ;\n"
    output_str += "esac ; \n\n"

    return output_str


ltl_properties, ltl_properties_count = read_ltl_command()
ltl_violation_count = dict()
ltl_violation_devices = dict()

for k, v in ltl_properties_count.items():
    ltl_violation_count[k] = 0
    ltl_violation_devices[k] = set()

multiple_count = 0


def obtain_violating_properties(input_seq, output_seq):
    with open('attack_trace_checker/demo.smv'.format(i), 'w') as fw:
        fw.write('MODULE main\n\n')
        var_str = create_vars(input_seq)
        fw.write(var_str)
        fw.write("ASSIGN\n\n")
        fw.write(case_for_state(input_seq))
        fw.write(case_for_output(input_seq, output_seq))

        for j in range(len(ltl_properties)):
            fw.write("LTLSPEC {}; \n".format(ltl_properties[j]))

    command2 = ["make"]
    process2 = subprocess.Popen(command2, stdout=subprocess.PIPE, cwd="./attack_trace_checker")
    process2.wait()

    violating_properties_idx = []
    violating_properties = []
    with open('attack_trace_checker/result.txt', 'r') as fr:
        ltl_count = 0
        lines = fr.readlines()
        for line in lines:
            if line.startswith("-- specification"):
                ltl_count += 1
                if "is false" in line:
                    violating_properties_idx.append(ltl_count)
                    ppty = line.split("\n")[0].split("specification ")[1].split(" is ")[0].strip()
                    violating_properties.append(ppty)

    if len(violating_properties_idx) == 0:
        return False, violating_properties_idx, violating_properties
    else:
        return True, violating_properties_idx, violating_properties


def check_match_and_obtain_diff(list_1, list_2):
    list_1 = sorted(list(set(list_1)))
    list_2 = sorted(list(set(list_2)))

    extra = list((set(list_1) - set(list_2)) | (set(list_2) - set(list_1)))
    if len(list_1) != len(list_2):
        return False, extra
    for i in range(len(list_1)):
        if list_1[i] != list_2[i]:
            return False, extra
    return True, extra


def check_input_in_properties(violating_property, input_symbol):
    interesting_part = violating_property
    if "->" in violating_property:
        interesting_part = str(violating_property).split("->", 1)[1].strip()
    if input_symbol in interesting_part:
        return True
    return False


def check_property_present(input_sym):
    for pp in ltl_properties:
        if check_input_in_properties(pp, input_sym):
            return True
    return False


def get_multiple_deviating_input_symbols(input_seq_, output_seq_1, output_seq_2):
    input_seq_ = string_to_list(input_seq_)
    output_seq_1 = string_to_list(output_seq_1)
    output_seq_2 = string_to_list(output_seq_2)

    deviating_inputs = dict()
    for i in range(len(input_seq_)):
        if output_seq_1[i] != output_seq_2[i]:
            if input_seq_[i] not in deviating_inputs.keys():
                deviating_inputs[input_seq_[i]] = 1
            else:
                deviating_inputs[input_seq_[i]] += 1

    multiple_inputs = dict()
    for input_sym in input_seq_:
        multiple_inputs.setdefault(input_sym, []).append(1)

    multiple_inputs_list = list()
    for k, v in multiple_inputs.items():
        if len(v) > 1:
            multiple_inputs_list.append(k)

    return deviating_inputs, multiple_inputs_list


def check_trace_resolve(trace):
    for out_i in range(len(trace["outputs"])):
        for out_j in range(out_i + 1, len(trace["outputs"])):
            if out_i == out_j:
                continue
            trace_input_seq = trace["input_symbols"]
            violating_i = trace["outputs"][out_i]["ViolatingPropertiesIdx"]
            violating_j = trace["outputs"][out_j]["ViolatingPropertiesIdx"]

            violating_i_properties = trace["outputs"][out_i]["ViolatingProperties"]
            is_same, extra_property_idx = check_match_and_obtain_diff(violating_i, violating_j)

            for idx in extra_property_idx:
                ltl_properties_count[idx] += 1

            if is_same:
                if len(violating_i) > 0:
                    check1 = False
                    check2 = True

                    deviating_inputs, multiple_inputs = get_multiple_deviating_input_symbols(trace_input_seq,
                                                                                             trace["outputs"][out_i][
                                                                                                 "output_symbols"],
                                                                                             trace["outputs"][out_j][
                                                                                                 "output_symbols"]
                                                                                             )
                    for multiple_input in multiple_inputs:
                        for violating_property in violating_i_properties:
                            if check_input_in_properties(violating_property, multiple_input):
                                check1 = True

                    for input_sym in deviating_inputs.keys():
                        if not check_property_present(input_sym):
                            check2 = False
                            break

                    if check1 & check2:
                        return True

                return False

    return True


with open('deviant-queries.json', 'r') as fr:
    data = json.load(fr)
    attack_data = dict()
    benign_data = dict()
    not_yet_resolved_traces = dict()

    attack_data["traces"] = list()
    benign_data["traces"] = list()
    not_yet_resolved_traces["traces"] = list()

    for i in range(len(data["traces"])):
        print("{} / {}".format(i, len(data["traces"])))
        data["traces"][i]["Id"] = i + 1

        trace_resolved = False
        input_added_attack = False
        input_added_benign = False

        input_seq = string_to_list(data["traces"][i]["input_symbols"])
        new_input_attack = dict()
        new_input_benign = dict()

        num_attacks = 0
        num_benign = 0

        for j in range(len(data["traces"][i]["outputs"])):
            output_seq = string_to_list(data["traces"][i]["outputs"][j]["output_symbols"])
            is_violating, violating_properties_idx, violating_properties \
                = obtain_violating_properties(input_seq, output_seq)
            data["traces"][i]["outputs"][j]["ViolatingPropertiesIdx"] = violating_properties_idx
            data["traces"][i]["outputs"][j]["ViolatingProperties"] = violating_properties
            if is_violating:
                num_attacks += 1
                if not input_added_attack:
                    input_added_attack = True
                    new_input_attack["Id"] = data["traces"][i]["Id"]
                    new_input_attack["input_symbols"] = input_seq

                    new_outputs = dict()
                    new_outputs["output_symbols"] = output_seq
                    new_outputs["devices"] = string_to_list(data["traces"][i]["outputs"][j]["devices"])
                    new_outputs["ViolatingPropertiesIdx"] = violating_properties_idx

                    new_input_attack["outputs"] = list()
                    new_input_attack["outputs"].append(new_outputs)

                else:
                    new_outputs = dict()
                    new_outputs["output_symbols"] = output_seq
                    new_outputs["devices"] = string_to_list(data["traces"][i]["outputs"][j]["devices"])
                    new_outputs["ViolatingPropertiesIdx"] = violating_properties_idx
                    new_input_attack["outputs"].append(new_outputs)

            else:
                num_benign += 1
                if not input_added_benign:
                    input_added_benign = True
                    new_input_benign["Id"] = data["traces"][i]["Id"]
                    new_input_benign["input_symbols"] = input_seq

                    new_outputs = dict()
                    new_outputs["output_symbols"] = output_seq

                    new_input_benign["outputs"] = list()
                    new_input_benign["outputs"].append(new_outputs)

                else:
                    new_outputs = dict()
                    new_outputs["output_symbols"] = output_seq
                    new_input_benign["outputs"].append(new_outputs)

        trace_resolved = check_trace_resolve(data["traces"][i])
        # pprint(data["traces"][i])
        if input_added_attack & trace_resolved:
            attack_data["traces"].append(new_input_attack)

        if input_added_benign & trace_resolved:
            benign_data["traces"].append(new_input_benign)

        elif not trace_resolved:
            print(input_seq)
            not_yet_resolved_traces["traces"].append(data["traces"][i])

    trace_ids = []
    attack_ids = []
    for i in range(len(data["traces"])):
        trace_ids.append(data["traces"][i]["Id"])

    for i in range(len(attack_data["traces"])):
        attack_ids.append(attack_data["traces"][i]["Id"])

    # print(set(trace_ids) - set(attack_ids))

    for i in range(len(attack_data["traces"])):
        for j in range(len(attack_data["traces"][i]["outputs"])):
            for k in range(len(attack_data["traces"][i]["outputs"][j]["ViolatingPropertiesIdx"])):
                violating_property_idx = attack_data["traces"][i]["outputs"][j]["ViolatingPropertiesIdx"][k]
                ltl_violation_count[violating_property_idx] += 1
                ltl_violation_devices[violating_property_idx] = set(ltl_violation_devices[violating_property_idx]) | \
                                                                set(
                                                                    attack_data["traces"][i]["outputs"][j]["devices"])

    # with open('deviant-queries.json', 'w') as fw:
    #     json.dump(data, fw, indent=2)

    with open('deviant-queries_attack.json', 'w') as fw:
        json.dump(attack_data, fw, indent=2)

    with open('deviant-queries_benign.json', 'w') as fw:
        json.dump(benign_data, fw, indent=2)

    with open('deviant-queries_unresolved.json', 'w') as fw:
        json.dump(not_yet_resolved_traces, fw, indent=2)

    print("Total Deviations {}".format(len(data["traces"])))
    print("Unresolved {}".format(len(not_yet_resolved_traces["traces"])))

end = time.time()
runtime = end - start
print(f"Program runtime: {runtime:.6f} seconds")