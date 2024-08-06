def calculate_average_from_file(file_path):
    total = 0
    count = 0
    
    with open(file_path, 'r') as file:
        for line in file:
            _, number = line.split(',')
            total += float(number)
            count += 1
    
    if count > 0:
        average = total / count
    else:
        average = 0
    
    return average

# File path (adjust this to the path of your file)
# file_path = 'test_cal_time.txt'
# file_path = 'elapsed_time_5gbasechecker_final.txt'
file_path ='elapsed_time_realBLEDiff.txt'

# Calculate the average and print it
average = calculate_average_from_file(file_path)
print(average)