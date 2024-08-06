# Read the content of the file
file_path = './FSM1_vs_FSM2_final'
with open(file_path, 'r') as file:
    lines = file.readlines()

# Process lines to extract deviations
deviations = []
current_deviation = []
inside_deviation = False

for line in lines:
    line = line.strip()
    if line.startswith('[') and line.endswith(']'):
        if current_deviation:
            deviations.append(current_deviation)
        current_deviation = [line]
        inside_deviation = True
    elif inside_deviation:
        if line:
            current_deviation.append(line)
        else:
            inside_deviation = False

if current_deviation:
    deviations.append(current_deviation)


output_lines = []
for dev in deviations:
    content = dev[0].strip('[]')
    if ']' in content:
        first_segment = content.split(']')[0]
        output_lines.append(f"INFO: [{first_segment}]")
    else:
        output_lines.append(f"INFO: [{content.strip()}]")

output_lines.reverse()
output_file_path = './Initial_PCEs.txt'
with open(output_file_path, 'w') as output_file:
    for line in output_lines:
        output_file.write(line + '\n')


