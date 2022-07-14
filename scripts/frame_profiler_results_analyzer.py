import argparse
import sys
import code

parser = argparse.ArgumentParser(description='')

parser.add_argument('-i', action='store_true')
parser.add_argument('FRAME_PROFILER_RESULTS_CSV', type=str)

args = parser.parse_args()

csvPath = testName = args.FRAME_PROFILER_RESULTS_CSV
interactive = args.i

def row2json(row, header):
    result = {}
    
    for i in range(len(row)):
        if row[i]:
            result[header[i]] = int(row[i])
        else:
            result[header[i]] = None
    
    return result

def analyze_row(row, idx, lastRow):
    print("Frame", idx)
    
    print(" ", (row["FRAME_END"] - row["FRAME_START"])/1000000, "ms", " - Total render time")
    
    if row["SYNC_START"]:
        print(" ", (row["SYNC_END"] - row["SYNC_START"])/1000000, "ms", " - Sync time")
    
    if(row["UPDATERENDERERS_DEADLINE"] != 0):
        print(" ", (row["UPDATERENDERERS_END"] - row["UPDATERENDERERS_DEADLINE"])/1000000, "ms", " - UpdateRenderers overshoot")
    print(" ", (row["RENDERWORLD_END"] - row["UPDATERENDERERS_END"])/1000000, "ms", " - Time spent after updateRenderers in renderWorld")
    
    if(row["UPDATERENDERERS_DEADLINE"] != 0):
        print(" ", (row["RENDERWORLD_END"] - row["UPDATERENDERERS_DEADLINE"])/1000000, "ms", " - Total UpdateRenderers deadline overshoot")
    
    print(" ", (row["GAMELOOP_END"] - row["GAMELOOP_START"])/1000000, "ms", " - Total gameloop time")
    
    if lastRow:
        print(" ", (row["FRAME_END"] - lastRow["FRAME_END"])/1000000, "ms", " - Frame end difference from last frame's")
    
    print("  ---")
    
    sortedItems = [x for x in sorted(row.items(), key=lambda x: x[1] if x[1] != None else 0)]
    lastItem = None
    for item in sortedItems:
        if lastItem != None and item[1] != None and lastItem[1] != None:
            print(" ", (item[1] - lastItem[1])/1000000, "ms", "-", lastItem[0], "->", item[0])
        lastItem = item

def readRows(csvPath):
    header = None
    rowJsons = []
    for line in list(open(csvPath, "r", encoding="utf8")):
        row = line.strip().split(",")
        if header == None:
            header = row
        else:
            rowJson = row2json(row, header)
            rowJsons.append(rowJson)
    
    return rowJsons

rows = readRows(csvPath)
rows = rows[1 : len(rows) - 1]

if interactive:
    print("\n>>> Exposed rows as list 'rows'\n")
    
    code.interact(local=locals())
else:
    idx = 1
    lastRow = None
    for row in rows:
        analyze_row(row, idx, lastRow)
        idx += 1
        lastRow = row
