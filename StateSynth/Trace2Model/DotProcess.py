import re

# All possible inputs
inputs = ["enable_s1", "id_request_plain_text", "auth_request_plain_text", "nas_sm_cmd", "rrc_sm_cmd", "ue_cap_enquiry_protected", "rrc_reconf_protected", "registration_accept_protected","config_update_cmd_protected"]


# lines = [
# "S0->S1 [label=\"Enable_s1/Registration_request\"]",
# "S3->S4 [label=\"Id_request/Id_response\"]",
# "S3->S5 [label=\"Auth_request/Auth_response\"]",
# "S4->S5 [label=\"Auth_request/Auth_response\"]",
# "S5->S6 [label=\"Nas_sm_cmd/Nas_sm_complete\"]",
# "S6->S7 [label=\"RRC_sm_cmd/RRC_sm_cmd_complete\"]",
# "S6->S8 [label=\"Registration_accept/Registration_complete\"]",
# "S7->S9 [label=\"RRC_Reconf/RRC_Reconf_complete\"]",
# "S8->S10 [label=\"Config_update_cmd/Null_action\"]",
# "S8->S7 [label=\"RRC_sm_cmd/RRC_sm_cmd_complete\"]",
# "S9->S10 [label=\"Config_update_cmd/Null_action\"
# "S8->S7 [label=\"RRC_sm_cmd/RRC_sm_cmd_complete\"]",
# "S9->S10 [label=\"Config_update_cmd/Null_action\"  ]]",
# "S9->S8 [label=\"Registration_accept/Registration_complete\"]"
# ]
import re

# Function to subtract 2 from a number
def subtract_two(match):
    # The group() method returns the actual content matched by the regex
    number = int(match.group(1))  # Convert the matched number to an integer
    return f"S{number - 2}"  # Subtract 2 and return the modified string

# Open the file
with open('./models/5g_incr_same_dfa.dot', 'r') as f:
    # Read the lines
    lines = f.readlines()

# Keep only the lines that contain '->'
lines = [line for line in lines if '->' in line and 'start' not in line]

# Substitute ',' with ']' at the end of each line, add 'S' at the beginning
# and after '->' in each line, then remove all whitespace
lines = [''.join(('S' + line.replace('->', '-> S').rstrip(',\n') + ']').split()) for line in lines]

# Add a blank space before '[' in each line
lines = [line.replace('[', ' [') for line in lines]

# Subtract 2 from each number in each line
pattern = r"S(\d+)"  # Regular expression to find 'S' followed by numbers
lines = [re.sub(pattern, subtract_two, line) for line in lines]

# Find all unique states
states = set()
for line in lines:
    state, next_state = re.findall(r'S\d+', line)
    states.add(state)
    states.add(next_state)

# Sort states in ascending order
states = sorted(list(states), key=lambda x: int(x[1:]))

# Create a map of each state's transitions
transitions = {state: {} for state in states}
for line in lines:
    state, next_state = re.findall(r'S\d+', line)
    input = re.search(r'label="([^/]+)/', line).group(1)
    output = re.search(r'/([^"]+)"', line).group(1)
    transitions[state][input] = (next_state, output)

# Add missing transitions
for state in states:
    for input in inputs:
        if input not in transitions[state]:
            transitions[state][input] = (state, "unknown")  # default to a self-loop with "unknown" output

# Create the final lines
final_lines = []
for state, trans in transitions.items():
    for input, (next_state, output) in trans.items():
        final_lines.append(f"{state}->{next_state} [label=\"{input}/{output}\"]")


def get_states(input_string):
    # Ensure the input is a string
    if not isinstance(input_string, str):
        raise ValueError("format_states function expects a string as input")

    # Regular expression to find 'S' followed by digits
    pattern = r'S(\d+)'
    # Find all matches in the input string
    matches = re.findall(pattern, input_string)

    # Convert matches to a set of integers to remove duplicates and sort them
    unique_states = sorted(set(int(match) for match in matches))

    # Format each unique state according to the specified format
    states = [f's{state} [shape="circle" label="s{state}"];' for state in unique_states]

    # Join all formatted states with a newline character
    # result = "\n".join(formatted_states)

    return states

def format_lines(strings):
    formatted_strings = []
    for s in strings:
        # Make the string lowercase
        lower_s = s.lower()
        # Add space around '->' and '/'
        formatted_s = re.sub(r'->', ' -> ', lower_s)
        formatted_s = re.sub(r'/', ' / ', formatted_s)
        formatted_s = formatted_s.replace("null_action", "null_action_dummy")
        formatted_s = formatted_s+";"
        formatted_strings.append(formatted_s)
    return formatted_strings

states=get_states("\n".join(final_lines))
final_lines=format_lines(final_lines)

with open("FSM2.dot", "w") as file:
    file.write("digraph g {\n__start0 [label=\"\" shape=\"none\"];\n\n")

    for state in states:
        file.write("\t"+state + "\n")
    for line in final_lines:
        file.write("    "+line + "\n")
    file.write("\n")
    file.write("__start0 -> s0;\n}")
