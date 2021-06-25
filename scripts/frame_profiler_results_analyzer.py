import sys

if len(sys.argv) != 2:
    sys.exit("Usage: {} FRAME_PROFILER_RESULTS_CSV".format(sys.argv[0]))

csvPath = sys.argv[1]

def row2json(row, header):
    result = {}
    
    for i in range(len(row)):
        if row[i]:
            result[header[i]] = int(row[i])
        else:
            result[header[i]] = None
    
    return result

def analyze_row(row):
    start = row["FRAME_START"]
    end = row["FRAME_END"]
    
    print(end - start, "ns")

header = None
for line in list(open(csvPath, "r", encoding="utf8")):
    row = line.strip().split(",")
    if header == None:
        header = row
    else:
        rowJson = row2json(row, header)
        if None not in rowJson.values():
            analyze_row(rowJson)