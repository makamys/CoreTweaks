import argparse
import sys
import code
import matplotlib.pyplot as plt
import numpy as np
import os
import datetime

parser = argparse.ArgumentParser(description='')

parser.add_argument('-i', action='store_true', help="Start an interactive Python REPL session for examining data")
parser.add_argument('--graph-chunk-update-time', action='store_true', help="Shows a histogram of chunk update time")
parser.add_argument('--graph-fps', action='store_true', help="Shows a histogram of FPS (restricted to when no chunk updates are happening and no GUI is open)")
parser.add_argument('--graph-render-sections', action='store_true', help="Shows a line graph of frametime over time, similarly to OptiFine's lagometer")
parser.add_argument('--summarize-fps', action='store_true', help="Prints FPS statistics")
parser.add_argument('FRAME_PROFILER_RESULTS_CSV', type=str, help="Output of CoreTweaks's frame profiler. They are created at .minecraft/coretweaks/out/frameprofiler.csv")

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
    
    if(row["t_updateRenderersEnd"] and row["t_updateRenderersDeadline"]):
        print(" ", (row["t_updateRenderersEnd"] - row["t_updateRenderersDeadline"])/1000000, "ms", " - UpdateRenderers overshoot")
    
    if(row["t_updateRenderersEnd"]):
        print(" ", (row["t_renderWorldEnd"] - row["t_updateRenderersEnd"])/1000000, "ms", " - Time spent after updateRenderers in renderWorld")
    
    if(row["t_updateRenderersDeadline"]):
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
elif args.graph_fps or args.summarize_fps:
    
    lastNonZeroChunkUpdateTime = None
    
    for i in range(0, len(rows) - 1):
        row = rows[i]
        row['_frameTime'] = rows[i + 1]['t_gameLoopStart'] - row['t_gameLoopStart']
        if row['chunkUpdates'] != 0 or lastNonZeroChunkUpdateTime == None:
            lastNonZeroChunkUpdateTime = rows[i]['t_gameLoopStart']
            
        row['_idle'] = (row['gui'] == None or row['gui'] == 'net.minecraft.client.gui.GuiChat') and (row['t_gameLoopStart'] - lastNonZeroChunkUpdateTime) > 1000000000
    
    totalTime = rows[len(rows) - 1]['t_gameLoopStart'] - rows[0]['t_gameLoopStart']
    
    FPSs = 1000000000.0 / (np.array([row['_frameTime'] for row in rows[0:len(rows)-1] if row['_idle']]))
    
    title = "FPS ({})\nMean: {} Med: {}: Min: {} Max: {} Elapsed: {}".format(os.path.basename(csvPath), int(np.mean(FPSs)), int(np.median(FPSs)), int(np.min(FPSs)), int(np.max(FPSs)), datetime.timedelta(seconds=totalTime // 1000000000))
    print(title)
    
    if args.graph_fps:
        plt.title(title)
        plt.hist(FPSs, bins=100)
        plt.show()
elif args.graph_render_sections:
    plt.plot([row["t_gameLoopStart"] for row in rows if row["t_updateRenderersStart"]], [row["t_updateRenderersEnd"] - row["t_updateRenderersStart"] for row in rows if row["t_updateRenderersStart"]])
    plt.plot([row["t_gameLoopStart"] for row in rows], [row["t_gameLoopEnd"] - row["t_gameLoopStart"] for row in rows])
    plt.title(os.path.basename(csvPath))
    plt.show()
else:
    idx = 1
    lastRow = None
    for row in rows:
        analyze_row(row, idx, lastRow)
        idx += 1
        lastRow = row
