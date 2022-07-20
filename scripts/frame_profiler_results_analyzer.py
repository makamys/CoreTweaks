import argparse
import sys
import code
import matplotlib.pyplot as plt
import numpy as np

parser = argparse.ArgumentParser(description='')

parser.add_argument('-i', action='store_true')
parser.add_argument('--graph-chunk-update-time', action='store_true')
parser.add_argument('FRAME_PROFILER_RESULTS_CSV', type=str)

args = parser.parse_args()

csvPath = testName = args.FRAME_PROFILER_RESULTS_CSV
interactive = args.i

def row2json(row, header):
    result = {}
    
    for i in range(len(row)):
        cell = row[i]
        if cell:
            result[header[i]] = int(cell) if cell.isnumeric() else cell
        else:
            result[header[i]] = None
    
    return result

def analyze_row(row, idx, lastRow):
    print("Frame", idx)
    
    print(" ", (row["t_frameEnd"] - row["t_frameStart"])/1000000, "ms", " - Total render time")
    
    if row["t_syncStart"]:
        print(" ", (row["t_syncEnd"] - row["t_syncStart"])/1000000, "ms", " - Sync time")
    
    if(row["t_updateRenderersDeadline"] != 0):
        print(" ", (row["t_updateRenderersEnd"] - row["t_updateRenderersDeadline"])/1000000, "ms", " - UpdateRenderers overshoot")
    print(" ", (row["t_renderWorldEnd"] - row["t_updateRenderersEnd"])/1000000, "ms", " - Time spent after updateRenderers in renderWorld")
    
    if(row["t_updateRenderersDeadline"] != 0):
        print(" ", (row["t_renderWorldEnd"] - row["t_updateRenderersDeadline"])/1000000, "ms", " - Total UpdateRenderers deadline overshoot")
    
    print(" ", (row["t_gameLoopEnd"] - row["t_gameLoopStart"])/1000000, "ms", " - Total gameloop time")
    
    if lastRow:
        print(" ", (row["t_frameEnd"] - lastRow["t_frameEnd"])/1000000, "ms", " - Frame end difference from last frame's")
    
    print("  ---")
    
    sortedItems = [x for x in sorted([i for i in row.items() if i[0].startswith('t_')], key=lambda x: x[1] if x[1] != None else 0)]
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
elif args.graph_chunk_update_time:
    plt.title("Chunk update time (ms)")
    plt.hist(np.array([r['t_updateRenderersEnd'] - r['t_updateRenderersStart'] for r in rows]) / 1000000.0, bins=200)
    plt.show()
else:
    idx = 1
    lastRow = None
    for row in rows:
        analyze_row(row, idx, lastRow)
        idx += 1
        lastRow = row
