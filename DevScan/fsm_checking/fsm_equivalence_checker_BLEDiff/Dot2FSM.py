
def get_states_and_tx(filename):
    states = []
    transitions = []

    with open(filename, "r") as f:
        lines = f.readlines()
        for i in range(len(lines)):
            if 'shape' in lines[i]:
                strg = lines[i].split('[')[0].strip()
                states.append(strg.strip())

            elif '//' in lines[i] and lines[i].startswith('//'):
                continue

            elif "__start0" in lines[i]:
                continue

            elif '->' in lines[i]:
                transition = ''
                strg = lines[i].split('->')
                start_state = strg[
                    0].strip()
                strg = strg[1].split('[')
                end_state = strg[0].strip()  # str[1]: label = "nas_requested_con_establishment | paging_tmsi /

                if start_state not in states:
                    print('ERROR: start_state is not in the list of states')
                    return

                if end_state not in states:
                    print('ERROR: end_state is not in the list of states')
                    return

                strg = strg[1].split('"')
                if len(strg) == 3:  # transition is written in one line
                    transition = strg[1]

                values = transition.split('/')
                # print 'values = ', values

                input_sym = values[0].strip()
                output_sym = values[1].strip()
                transitions.append([start_state, input_sym, output_sym, end_state])

    states.remove("__start0")

    return states, transitions, states[0]


def list_to_dict(input_list):
    result_dict = {}
    key = tuple(input_list[:2])
    value = tuple(input_list[2:])
    result_dict[key] = value
    return result_dict

# class StateClass:
#     def __init__(self, name):  # outgoing_tx = (in_sym,out_sym,next_state)
#         self.state_name = name
#         self.is_null = False
#         self.outgoing_transitions_dict = dict()
#         self.incoming_transitions_list = list()
#         self.suffix_result = []
#         self.outgoing_state_names = []
#         self.incoming_state_names = []
#         self.mutation_list = dict()
#         self.input_messages_conditions = dict()
#
#     def add_outgoing_transition(self, outgoing_transition):
#         if outgoing_transition[0] in list(self.outgoing_transitions_dict.keys()):
#             del self.outgoing_transitions_dict[outgoing_transition[0]]
#
#         if "{" in outgoing_transition[0]:
#             input_message_name = str(outgoing_transition[0]).split("{")[0].strip()
#             input_message_condition = str(outgoing_transition[0]).split("{")[1].split("}")[0].strip()
#         else:
#             input_message_name = outgoing_transition[0].strip()
#             input_message_condition = ""
#
#         if input_message_name in self.input_messages_conditions.keys():
#             new_list = []
#             added = False
#             for i in range(len(self.input_messages_conditions[input_message_name])):
#                 if (not added and
#                         is_subset_condition(input_message_condition,
#                                             self.input_messages_conditions[input_message_name][i])):
#                     new_list.append(input_message_condition)
#                     new_list.append(self.input_messages_conditions[input_message_name][i])
#                 else:
#                     new_list.append(self.input_messages_conditions[input_message_name][i])
#             self.input_messages_conditions[input_message_name] = new_list
#         else:
#             self.input_messages_conditions[input_message_name] = [input_message_condition]
#
#         if input_message_condition != "":
#             input_symbol_in_rfsm = input_message_name + "{" + input_message_condition + "}"
#         else:
#             input_symbol_in_rfsm = input_message_name
#
#         self.outgoing_transitions_dict[input_symbol_in_rfsm] = [outgoing_transition[1], outgoing_transition[2]]
#
#     def get_outgoing_transition_output(self, input_symbol):
#         if "{" in input_symbol:
#             input_message_name = str(input_symbol).split("{")[0].strip()
#             input_message_condition = str(input_symbol).split("{")[1].split("}")[0].strip()
#         else:
#             input_message_name = input_symbol.strip()
#             input_message_condition = ""
#
#         if self.is_null:
#             return ["null_action", self.state_name]
#
#         if input_message_name not in list(self.input_messages_conditions.keys()):
#             return ["null_action", self.state_name]
#         else:
#             condition_counter = 0
#             while condition_counter < len(
#                     self.input_messages_conditions[input_message_name]):
#                 current_condition = self.input_messages_conditions[input_message_name][condition_counter]
#                 if is_subset_condition(input_message_condition, current_condition):
#                     if current_condition != "":
#                         input_symbol_in_rfsm = input_message_name + "{" + current_condition + "}"
#                     else:
#                         input_symbol_in_rfsm = input_message_name
#
#                     return self.outgoing_transitions_dict[input_symbol_in_rfsm]
#                 condition_counter += 1
#
#         return ["null_action", self.state_name]
#
#     def get_outgoing_input_symbols(self):
#         all_inp_symbols = []
#         for k, v in self.input_messages_conditions.items():
#             for cond in v:
#                 if cond != "":
#                     inp_sym = k + "{" + cond + "}"
#                 else:
#                     inp_sym = k
#                 all_inp_symbols.append(inp_sym)
#         return all_inp_symbols
#
#     def get_name(self):
#         return self.state_name
#
#     def add_outgoing_state(self, state_name):
#         self.outgoing_state_names.append(state_name)
#
#     def get_outgoing_states(self):
#         return self.outgoing_state_names
#
#     def check_in_mutation_list(self, mutations_rem, tokens_rem, length_rem):
#         key = str(mutations_rem) + "-" + str(tokens_rem) + "-" + str(length_rem)
#         if key in self.mutation_list.keys():
#             return True, self.mutation_list[key]
#         else:
#             return False, [False, [[], []]]
#
#     def add_to_mutation_list(self, exists_sequence, sequences, mutations_rem, tokens_rem, length_rem):
#         key = str(mutations_rem) + "-" + str(tokens_rem) + "-" + str(length_rem)
#         if key in self.mutation_list.keys():
#             if exists_sequence and not self.mutation_list[key][0] or (exists_sequence and self.mutation_list[key][0]
#                                                                       and len(self.mutation_list[key][1]) == 0):
#                 self.mutation_list[key] = [exists_sequence, self.clear_duplicates(sequences)]
#             elif exists_sequence and self.mutation_list[key][0]:
#                 self.mutation_list[key] = [exists_sequence,
#                                            self.clear_duplicates(sequences + self.mutation_list[key][1])]
#         else:
#             if len(sequences) == 0:
#                 self.mutation_list[key] = [exists_sequence, sequences]
#             else:
#                 self.mutation_list[key] = [exists_sequence, self.clear_duplicates(sequences)]
#
#     def clear_mutation_list(self):
#         self.mutation_list.clear()
#
#     def clear_duplicates(self, sequence):
#         new_seq = []
#         for i in range(len(sequence)):
#             add = False
#             if len(new_seq) == 0:
#                 add = True
#             for j in range(len(new_seq)):
#                 if len(new_seq[j][0]) == len(sequence[i][0]):
#                     for k in range(len(new_seq[j][0])):
#                         if new_seq[j][0][k] != sequence[i][0][k]:
#                             add = True
#                             break
#                 else:
#                     add = True
#                 if len(new_seq[j][1]) == len(sequence[i][1]):
#                     for k in range(len(new_seq[j][1])):
#                         if new_seq[j][1][k] != sequence[i][1][k]:
#                             add = True
#                             break
#                 else:
#                     add = True
#             if add:
#                 new_seq.append(sequence[i])
#
#         return new_seq
# class FSM(object):
#     def __init__(self, states, start_state, transitions=None):  # transitions = (curr_state,in_sym,out_sym,next_state)
#         self.testcases = list()
#         self.states_dict = dict()
#         self.start_state_name = start_state
#         self.global_longest_predecessor_list = []
#         self.template_qre = ""
#         self.current_state = self.start_state_name
#
#         for state in states:
#             if state not in list(self.states_dict.keys()):
#                 self.states_dict[str(state).strip()] = StateClass(state)
#
#         for transition in transitions:
#             curr_state_name = str(transition[0]).strip()
#             in_sym = str(transition[1]).strip()
#             out_sym = str(transition[2]).strip()
#             next_state_name = str(transition[3]).strip()
#
#             if curr_state_name not in list(self.states_dict.keys()):
#                 print("Error : curr_state Not Found")
#
#             if next_state_name not in list(self.states_dict.keys()):
#                 print("Error : next_state Not Found")
#
#             self.states_dict[curr_state_name].add_outgoing_transition([in_sym, out_sym, next_state_name])
#             self.states_dict[curr_state_name].add_outgoing_state(next_state_name)
#
#         self.states_dict["hypothetical_state"] = StateClass("hypothetical_state")
#
#     def update_fsm_for_considered_inputs(self, considered_interesting_inputs):
#         for state_name in list(self.states_dict.keys()):
#             if state_name != "hypothetical_state":
#                 state = self.states_dict[state_name]
#                 for input_symbol in considered_interesting_inputs:
#                     if input_symbol not in state.outgoing_transitions_dict.keys():
#                         output_symbol, next_state_name = state.get_outgoing_transition_output(input_symbol)
#                         state.add_outgoing_transition([input_symbol, output_symbol, next_state_name])
#
#     def set_state(self, state_name):
#         self.current_state = state_name
#
#     def make_transition(self, input_symbol):
#         if input_symbol == 'RESET':
#             self.set_state(self.start_state_name)
#             return 'null_action'
#         inp_sym = str(input_symbol).replace("FUZZ", "0")
#         current_state = self.states_dict[self.current_state]
#         output_symbol, next_state = current_state.get_outgoing_transition_output(inp_sym)
#         self.set_state(next_state)
#         return output_symbol
#
#     def generate_fsm_output_sequence(self, input_sequence, attacker_expectation_sequence):
#         current_state_name = self.start_state_name
#         output_sequence = []
#
#         for i in range(len(input_sequence)):
#             curr_state = self.states_dict[current_state_name]
#             inp_sym = str(input_sequence[i]).replace("FUZZ", "0")
#             out_sym, next_state_name = curr_state.get_outgoing_transition_output(inp_sym)
#             output_sequence.append(out_sym)
#             current_state_name = next_state_name
#
#             if not str(attacker_expectation_sequence[i]).startswith("^"):
#                 if not matches_symbol_condition(out_sym, attacker_expectation_sequence[i]) == (True, True):
#                     break
#             else:
#                 if matches_symbol_condition(out_sym, attacker_expectation_sequence[i]) == (True, True):
#                     break
#
#         return output_sequence, current_state_name
#
#     def get_all_input_symbols(self):
#         all_input_symbols = []
#         for k, v in self.states_dict.items():
#             all_input_symbols = all_input_symbols + self.states_dict[k].get_outgoing_input_symbols()
#         all_input_symbols = list(set(all_input_symbols))
#         return all_input_symbols
#
#     def get_all_output_symbols(self):
#         all_output_symbols = []
#         for k, v in self.states_dict.items():
#             for inp, tran in v.outgoing_transitions_dict.items():
#                 all_output_symbols.append(tran[0])
#         all_output_symbols = list(set(all_output_symbols))
#         return all_output_symbols
#
#     def get_responsive_io(self, state_name):
#         state = self.states_dict[state_name]
#         responsive_ios = dict()
#         for input_sym, [output_sym, _] in state.outgoing_transitions_dict.items():
#             if output_sym != "null_action":
#                 responsive_ios[input_sym] = output_sym
#         return responsive_ios
#
#     def count_token_remaining(self, qre_expression: str):
#         tokens = qre_expression.split(";")
#         total_tokens = 0
#
#         for i in range(len(tokens)):
#             curr_tok = tokens[i].strip()
#             if curr_tok == "":
#                 continue
#             if "*" not in curr_tok:
#                 total_tokens += 1
#
#         return total_tokens
#
#     def get_allowed_transitions(self, node_name: str, qre_expression: str, token_rem: int):
#         tokens = qre_expression.split(";")
#         total_tokens = 0
#
#         forward_token_indices = []
#         forward_tokens = []
#
#         allowed_transitions = []
#         forward_transitions = []
#
#         for i in range(len(tokens)):
#             curr_tok = tokens[i].strip()
#             if curr_tok == "":
#                 continue
#             if "*" not in curr_tok:
#                 forward_token_indices.append(i)
#                 forward_tokens.append(curr_tok)
#                 total_tokens += 1
#
#         next_token = total_tokens - token_rem
#
#         next_forward_token = tokens[forward_token_indices[next_token]]
#         neg_token = False
#         if "^" in next_forward_token.strip():
#             neg_token = True
#             next_forward_token = next_forward_token.split("^")[1]
#
#         next_inp_sym = next_forward_token.split("<")[1].split(",")[0].strip()
#         next_out_sym = next_forward_token.split(",")[1].split(">")[0].strip()
#
#         if forward_token_indices[next_token] == 0:
#             # print("fti 0 : Next inp sym : {}, Next out sym: {} Neg: {}\n".format(next_inp_sym, next_out_sym,
#             #                                                                      neg_token))
#             # pass
#             if node_name != "hypothetical_state":
#                 node = self.states_dict[node_name]
#                 for input_sym, out_tran in node.outgoing_transitions_dict.items():
#                     if not neg_token and \
#                             (matches_symbol_condition(input_sym, next_inp_sym) == (True, True)) \
#                             and (matches_symbol_condition(out_tran[0], next_out_sym, input_sym) == (True, True)) \
#                             :
#                         forward_transitions.append([input_sym, out_tran[0], out_tran[1]])
#
#                     elif neg_token and \
#                             (matches_symbol_condition(input_sym, next_inp_sym) == (True, True)
#                              and matches_symbol_condition(out_tran[0], next_out_sym, input_sym) != (True, True)):
#                         forward_transitions.append([input_sym, out_tran[0], out_tran[1]])
#             else:
#                 if not neg_token:
#                     forward_transitions.append([next_inp_sym, next_out_sym, "hypothetical_state"])
#                 else:
#                     forward_transitions.append([next_inp_sym, "^" + next_out_sym, "hypothetical_state"])
#
#         else:
#             prev_token_idx = forward_token_indices[next_token] - 1
#             prev_token = tokens[prev_token_idx].strip()
#
#             default_allowed = False
#             constraint_tokens = []
#
#             if "^" in prev_token and "]*" in prev_token:
#                 default_allowed = True
#                 tok_str = prev_token.split("^")[1].split("]*")[0].strip()
#                 if "|" in tok_str:
#                     not_allowed_toks_str = tok_str.split("|")
#                     for n_tok in not_allowed_toks_str:
#                         n_input_sym = n_tok.split("<")[1].split(",")[0].strip()
#                         n_output_sym = n_tok.split(",")[1].split(">")[0].strip()
#                         constraint_tokens.append([n_input_sym, n_output_sym])
#                 elif "<" in tok_str:
#                     n_input_sym = tok_str.split("<")[1].split(",")[0].strip()
#                     n_output_sym = tok_str.split(",")[1].split(">")[0].strip()
#                     constraint_tokens.append([n_input_sym, n_output_sym])
#
#             elif "^" not in prev_token and "]*" in prev_token:
#                 default_allowed = False
#                 tok_str = prev_token.split("[")[1].split("]*")[0].strip()
#                 if "<" in tok_str:
#                     constraint_tok = tok_str.split("<", 1)[1].split(">", 1)[0].strip()
#                     n_input_sym = constraint_tok.split(",")[0].strip()
#                     n_output_sym = constraint_tok.split(",")[1].strip()
#                     constraint_tokens.append([n_input_sym, n_output_sym])
#
#             # print("fti not 0 : Next inp sym : {}, Next out sym: {}, neg : {}".format(next_inp_sym, next_out_sym,
#             #                                                                          neg_token))
#             # print("Constraint Allowed : {}, constraint tokens {}".format(default_allowed, constraint_tokens))
#
#             if node_name != "hypothetical_state":
#                 node = self.states_dict[node_name]
#                 for input_sym, out_tran in node.outgoing_transitions_dict.items():
#                     if not neg_token and \
#                             (matches_symbol_condition(input_sym, next_inp_sym) == (True, True)
#                              and matches_symbol_condition(out_tran[0], next_out_sym, input_sym) == (True, True)):
#                         forward_transitions.append([input_sym, out_tran[0], out_tran[1]])
#                         continue
#
#                     elif neg_token and \
#                             (matches_symbol_condition(input_sym, next_inp_sym) == (True, True)
#                              and matches_symbol_condition(out_tran[0], next_out_sym, input_sym) != (True, True)):
#                         forward_transitions.append([input_sym, out_tran[0], out_tran[1]])
#                         continue
#
#                     for n_inp, n_out in constraint_tokens:
#                         if default_allowed and (matches_symbol_condition(input_sym, n_inp) == (True, True)
#                                                 and matches_symbol_condition(out_tran[0], n_out, input_sym) == (
#                                                         True, True)):
#                             default_allowed = False
#                         elif not default_allowed and (matches_symbol_condition(input_sym, n_inp) == (True, True)
#                                                       and matches_symbol_condition(out_tran[0], n_out, input_sym) == (
#                                                               True, True)):
#                             default_allowed = True
#
#                     if default_allowed:
#                         allowed_transitions.append([input_sym, out_tran[0], out_tran[1]])
#             else:
#                 if not neg_token:
#                     forward_transitions.append([next_inp_sym, next_out_sym, "hypothetical_state"])
#                     for input_sym in self.get_all_input_symbols():
#                         # if next_inp_sym == "$":
#                         #     break
#                         # if matches_symbol_condition(input_sym, next_inp_sym) == (True, True):
#                         #     continue
#
#                         final_tr_allowed = [input_sym, "$"]
#
#                         for n_inp, n_out in constraint_tokens:
#                             if default_allowed:
#                                 if matches_symbol_condition(input_sym, n_inp) == (True, True) \
#                                         and n_out == "$":
#                                     final_tr_allowed = []
#                                 elif matches_symbol_condition(input_sym, n_inp) == (True, True) \
#                                         and n_out != "$":
#                                     final_tr_allowed = [input_sym, "^" + n_out]
#                                 elif n_inp == "$" and n_out != "$":
#                                     final_tr_allowed = [input_sym, "^" + n_out]
#                                 elif n_inp == "$" and n_out == "$":
#                                     final_tr_allowed = []
#                             if not default_allowed:
#                                 final_tr_allowed = []
#                                 if matches_symbol_condition(input_sym, n_inp) == (True, True):
#                                     final_tr_allowed = [input_sym, n_out]
#
#                         if len(final_tr_allowed) == 2:
#                             allowed_transitions.append([final_tr_allowed[0], final_tr_allowed[1], "hypothetical_state"])
#
#                 else:
#                     forward_transitions.append([next_inp_sym, "^" + next_out_sym, "hypothetical_state"])
#                     for input_sym in self.get_all_input_symbols():
#                         # if next_inp_sym == "$":
#                         #     break
#                         # if matches_symbol_condition(input_sym, next_inp_sym) == (True, True):
#                         #     continue
#
#                         final_tr_allowed = [input_sym, "$"]
#
#                         for n_inp, n_out in constraint_tokens:
#                             if default_allowed:
#                                 if matches_symbol_condition(input_sym, n_inp) == (True, True) \
#                                         and n_out == "$":
#                                     final_tr_allowed = []
#                                 elif matches_symbol_condition(input_sym, n_inp) == (True, True) \
#                                         and n_out != "$":
#                                     final_tr_allowed = [input_sym, "^" + n_out]
#                                 elif n_inp == "$" and n_out != "$":
#                                     final_tr_allowed = [input_sym, "^" + n_out]
#                                 elif n_inp == "$" and n_out == "$":
#                                     final_tr_allowed = []
#                             if not default_allowed:
#                                 final_tr_allowed = []
#                                 if matches_symbol_condition(input_sym, n_inp) == (True, True):
#                                     final_tr_allowed = [input_sym, n_out]
#
#                         if len(final_tr_allowed) == 2:
#                             allowed_transitions.append([final_tr_allowed[0], final_tr_allowed[1], "hypothetical_state"])
#
#                 # TODO : For same input symbol if there are multiple output symbol not allowed
#
#         return neg_token, next_inp_sym, next_out_sym, forward_transitions, allowed_transitions
#
#
#
#     def generate_mutated_sequences(self, dump_filename, considered_interesting_inputs, template_qre,
#                                    max_mutation_budget=2, max_test_case_length=7):
#         self.testcases = []
#         for state_name in self.states_dict.keys():
#             self.states_dict[state_name].clear_mutation_list()
#         self.template_qre = template_qre
#         total_tokens = self.count_token_remaining(self.template_qre)
#         self.generate_fixed_mutation_2(considered_interesting_inputs,
#                                        self.start_state_name, [], max_mutation_budget, total_tokens,
#                                        max_test_case_length)
#
#         self.testcases = self.expand_testcase(self.start_state_name, max_mutation_budget, total_tokens,
#                                               max_test_case_length)
#
#         with open(os.path.join('testcases_dump', "{}.txt".format(dump_filename.split(".")[0])), 'w') as fw:
#             for i in range(len(self.testcases)):
#                 fw.write("TestCase {} : {}\n".format(i + 1, self.testcases[i]))
#
#         return
#
#     def expand_testcase(self, node_name, mutation_rem, token_rem, length_rem):
#         _, results = self.states_dict[node_name].check_in_mutation_list(mutation_rem,
#                                                                         token_rem,
#                                                                         length_rem)
#
#         testcases = []
#         if results[0]:
#             sequences = results[1]
#             for i in range(len(sequences)):
#                 curr_tok = sequences[i][0]
#                 expand_node = sequences[i][1]
#                 if len(expand_node) == 4:
#                     suffix_seqs = self.expand_testcase(expand_node[0],
#                                                        expand_node[1],
#                                                        expand_node[2],
#                                                        expand_node[3])
#
#                     final_seq = [[curr_tok] + seq for seq in suffix_seqs]
#                     for seq in final_seq:
#                         testcases.append(seq)
#
#                 else:
#                     testcases.append([curr_tok])
#         return testcases
#
#     def generate_fixed_mutation_2(self, considered_interesting_inputs,
#                                   node_name, visited_states, mutation_rem, token_rem, length_rem):
#
#         if token_rem > length_rem:
#             self.states_dict[node_name].add_to_mutation_list(False, [], mutation_rem, token_rem,
#                                                              length_rem)
#             return False, []
#         if mutation_rem < 0:
#             self.states_dict[node_name].add_to_mutation_list(False, [], mutation_rem, token_rem,
#                                                              length_rem)
#             return False, []
#         if length_rem == 0:
#             if token_rem > 0:
#                 self.states_dict[node_name].add_to_mutation_list(False, [], mutation_rem, token_rem,
#                                                                  length_rem)
#                 return False, []
#             else:
#                 self.states_dict[node_name].add_to_mutation_list(False, [], mutation_rem, token_rem,
#                                                                  length_rem)
#                 return True, []
#         if token_rem == 0:
#             self.states_dict[node_name].add_to_mutation_list(True, [], mutation_rem, token_rem,
#                                                              length_rem)
#             return True, []
#
#         visited_states.append(node_name)
#         fn_exists_seq = False
#         fn_seq_list = []
#
#         neg_token, next_template_inp_sym, next_template_out_sym, \
#         forward_transitions, allowed_transitions = self.get_allowed_transitions(node_name,
#                                                                                 self.template_qre,
#                                                                                 token_rem)
#
#         if node_name != "hypothetical_state":
#             for input_sym, output_sym, next_state_name in forward_transitions:
#                 next_state = self.states_dict[next_state_name]
#
#                 calculated, result = next_state.check_in_mutation_list(mutation_rem, token_rem - 1,
#                                                                        length_rem - 1)
#
#                 if calculated:
#                     exists, sequences = result
#                 else:
#                     new_visited_states = visited_states.copy()
#                     new_visited_states.append(node_name)
#                     exists, sequences = self.generate_fixed_mutation_2(considered_interesting_inputs,
#                                                                        next_state_name,
#                                                                        new_visited_states,
#                                                                        mutation_rem,
#                                                                        token_rem - 1, length_rem - 1)
#                 if exists:
#                     fn_exists_seq = True
#                     if token_rem == 1:
#                         fn_seq_list.append(
#                             [[node_name, input_sym,
#                               output_sym,
#                               next_state_name], []])
#                     else:
#                         fn_seq_list.append([[node_name, input_sym,
#                                              output_sym,
#                                              next_state_name],
#                                             [next_state_name, mutation_rem, token_rem - 1, length_rem - 1]])
#
#             for input_sym, output_sym, next_state_name in allowed_transitions:
#                 next_state = self.states_dict[next_state_name]
#                 if next_state_name in visited_states:
#
#                     in_considered_interesting_inputs = False
#                     for considered_input in considered_interesting_inputs:
#                         if matches_symbol_condition(input_sym, considered_input) == (True, True):
#                             in_considered_interesting_inputs = True
#                             break
#
#                     if mutation_rem >= 1 and in_considered_interesting_inputs:
#                         calculated, result = next_state.check_in_mutation_list(mutation_rem - 1, token_rem,
#                                                                                length_rem - 1)
#
#                         if calculated:
#                             exists, sequences = result
#                         else:
#                             new_visited_states = visited_states.copy()
#                             new_visited_states.append(node_name)
#                             exists, sequences = self.generate_fixed_mutation_2(considered_interesting_inputs,
#                                                                                next_state_name,
#                                                                                new_visited_states,
#                                                                                mutation_rem - 1,
#                                                                                token_rem, length_rem - 1)
#                         if exists:
#                             fn_exists_seq = True
#                             fn_seq_list.append([[node_name, input_sym,
#                                                  output_sym,
#                                                  next_state_name],
#                                                 [next_state_name, mutation_rem - 1, token_rem, length_rem - 1]])
#
#
#                 else:
#                     calculated, result = next_state.check_in_mutation_list(mutation_rem, token_rem,
#                                                                            length_rem - 1)
#
#                     if calculated:
#                         exists, sequences = result
#                     else:
#                         new_visited_states = visited_states.copy()
#                         new_visited_states.append(node_name)
#                         exists, sequences = self.generate_fixed_mutation_2(considered_interesting_inputs,
#                                                                            next_state_name,
#                                                                            new_visited_states,
#                                                                            mutation_rem,
#                                                                            token_rem, length_rem - 1)
#
#                     if exists:
#                         fn_exists_seq = True
#                         fn_seq_list.append([[node_name, input_sym,
#                                              output_sym,
#                                              next_state_name],
#                                             [next_state_name, mutation_rem, token_rem, length_rem - 1]])
#
#             if mutation_rem >= 1:
#                 for input_sym, output_sym, next_state_name in forward_transitions:
#                     next_state = self.states_dict["hypothetical_state"]
#
#                     calculated, result = next_state.check_in_mutation_list(mutation_rem, token_rem - 1,
#                                                                            length_rem - 1)
#
#                     if calculated:
#                         exists, sequences = result
#                     else:
#                         exists, sequences = self.generate_fixed_mutation_2(considered_interesting_inputs,
#                                                                            "hypothetical_state",
#                                                                            visited_states,
#                                                                            mutation_rem,
#                                                                            token_rem - 1, length_rem - 1)
#                     if exists:
#                         fn_exists_seq = True
#                         if token_rem == 1:
#                             fn_seq_list.append(
#                                 [[node_name, input_sym,
#                                   output_sym,
#                                   "hypothetical_state"], []])
#                         else:
#                             fn_seq_list.append([[node_name, input_sym,
#                                                  output_sym,
#                                                  "hypothetical_state"],
#                                                 ["hypothetical_state", mutation_rem, token_rem - 1, length_rem - 1]])
#
#                 for input_sym, output_sym, next_state_name in allowed_transitions:
#
#                     in_considered_interesting_inputs = False
#                     for considered_input in considered_interesting_inputs:
#                         if matches_symbol_condition(input_sym, considered_input) == (True, True):
#                             in_considered_interesting_inputs = True
#                             break
#
#                     if in_considered_interesting_inputs:
#                         next_state = self.states_dict["hypothetical_state"]
#
#                         calculated, result = next_state.check_in_mutation_list(mutation_rem, token_rem,
#                                                                                length_rem - 1)
#
#                         if calculated:
#                             exists, sequences = result
#                         else:
#                             exists, sequences = self.generate_fixed_mutation_2(considered_interesting_inputs,
#                                                                                "hypothetical_state",
#                                                                                visited_states,
#                                                                                mutation_rem,
#                                                                                token_rem, length_rem - 1)
#
#                         if exists:
#                             fn_exists_seq = True
#                             fn_seq_list.append([[node_name, input_sym,
#                                                  output_sym,
#                                                  "hypothetical_state"],
#                                                 ["hypothetical_state", mutation_rem, token_rem, length_rem - 1]])
#
#         else:
#             if mutation_rem >= 1:
#                 for input_sym, output_sym, next_state_name in forward_transitions:
#
#                     in_considered_interesting_inputs = False
#                     for considered_input in considered_interesting_inputs:
#                         if matches_symbol_condition(input_sym, considered_input) == (True, True):
#                             in_considered_interesting_inputs = True
#                             break
#
#                     if not neg_token or (neg_token and in_considered_interesting_inputs):
#                         next_state = self.states_dict["hypothetical_state"]
#
#                         calculated, result = next_state.check_in_mutation_list(mutation_rem - 1, token_rem - 1,
#                                                                                length_rem - 1)
#
#                         if calculated:
#                             exists, sequences = result
#                         else:
#                             exists, sequences = self.generate_fixed_mutation_2(considered_interesting_inputs,
#                                                                                "hypothetical_state",
#                                                                                visited_states,
#                                                                                mutation_rem - 1,
#                                                                                token_rem - 1, length_rem - 1)
#                         if exists:
#                             fn_exists_seq = True
#                             if token_rem == 1:
#                                 fn_seq_list.append(
#                                     [["hypothetical_state", input_sym,
#                                       output_sym,
#                                       "hypothetical_state"], []])
#
#                             else:
#                                 fn_seq_list.append([["hypothetical_state", input_sym,
#                                                      output_sym,
#                                                      "hypothetical_state"],
#                                                     ["hypothetical_state", mutation_rem - 1, token_rem - 1,
#                                                      length_rem - 1]])
#
#                 for input_sym, output_sym, next_state_name in allowed_transitions:
#
#                     in_considered_interesting_inputs = False
#                     for considered_input in considered_interesting_inputs:
#                         if matches_symbol_condition(input_sym, considered_input) == (True, True):
#                             in_considered_interesting_inputs = True
#                             break
#
#                     if in_considered_interesting_inputs:
#                         next_state = self.states_dict["hypothetical_state"]
#                         calculated, result = next_state.check_in_mutation_list(mutation_rem - 1, token_rem,
#                                                                                length_rem - 1)
#
#                         if calculated:
#                             exists, sequences = result
#                         else:
#                             exists, sequences = self.generate_fixed_mutation_2(considered_interesting_inputs,
#                                                                                "hypothetical_state",
#                                                                                visited_states,
#                                                                                mutation_rem - 1,
#                                                                                token_rem, length_rem - 1)
#
#                         if exists:
#                             fn_exists_seq = True
#                             fn_seq_list.append([[node_name, input_sym, output_sym, "hypothetical_state"],
#                                                 ["hypothetical_state", mutation_rem - 1, token_rem,
#                                                  length_rem - 1]])
#
#                 if length_rem >= 2:
#                     for next_state_name in self.states_dict.keys():
#                         if next_state_name != "hypothetical_state":
#                             next_state = self.states_dict[next_state_name]
#                             if token_rem > 1:
#                                 for input_sym, output_sym, _ in forward_transitions:
#                                     calculated, result = next_state.check_in_mutation_list(mutation_rem - 1,
#                                                                                            token_rem - 1,
#                                                                                            length_rem - 1)
#
#                                     if calculated:
#                                         exists, sequences = result
#                                     else:
#                                         exists, sequences = self.generate_fixed_mutation_2(
#                                             considered_interesting_inputs,
#                                             next_state_name,
#                                             visited_states,
#                                             mutation_rem - 1,
#                                             token_rem - 1, length_rem - 1)
#                                     if exists:
#                                         fn_exists_seq = True
#                                         if token_rem == 1:
#                                             fn_seq_list.append(
#                                                 [["hypothetical_state",
#                                                   input_sym,
#                                                   output_sym,
#                                                   next_state_name], []])
#                                         else:
#                                             fn_seq_list.append(
#                                                 [["hypothetical_state",
#                                                   input_sym,
#                                                   output_sym,
#                                                   next_state_name],
#                                                  [next_state_name, mutation_rem - 1, token_rem - 1,
#                                                   length_rem - 1]])
#                             else:
#                                 for input_sym, output_sym, _ in forward_transitions:
#                                     fn_exists_seq = True
#                                     fn_seq_list = [
#                                         [["hypothetical_state", input_sym, output_sym, "hypothetical_state"], []]]
#
#                             for input_sym, output_sym, _ in allowed_transitions:
#
#                                 in_considered_interesting_inputs = False
#                                 for considered_input in considered_interesting_inputs:
#                                     if matches_symbol_condition(input_sym, considered_input) == (True, True):
#                                         in_considered_interesting_inputs = True
#                                         break
#
#                                 if in_considered_interesting_inputs:
#                                     calculated, result = next_state.check_in_mutation_list(mutation_rem - 1, token_rem,
#                                                                                            length_rem - 1)
#
#                                     if calculated:
#                                         exists, sequences = result
#                                     else:
#                                         exists, sequences = self.generate_fixed_mutation_2(
#                                             considered_interesting_inputs,
#                                             next_state_name,
#                                             visited_states,
#                                             mutation_rem - 1,
#                                             token_rem, length_rem - 1)
#
#                                     if exists:
#                                         fn_exists_seq = True
#                                         fn_seq_list.append(
#                                             [["hypothetical_state",
#                                               input_sym,
#                                               output_sym,
#                                               next_state_name],
#                                              [next_state_name, mutation_rem - 1, token_rem,
#                                               length_rem - 1]])
#
#                 else:
#                     if token_rem == 1:
#                         for input_sym, output_sym, _ in forward_transitions:
#                             fn_exists_seq = True
#                             fn_seq_list = [[["hypothetical_state", input_sym, output_sym, "hypothetical_state"], []]]
#
#         self.states_dict[node_name].add_to_mutation_list(fn_exists_seq, fn_seq_list, mutation_rem, token_rem,
#                                                          length_rem)
#         print(node_name, mutation_rem, token_rem, length_rem)
#         return fn_exists_seq, fn_seq_list