package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import makamys.coretweaks.ducks.optimization.IPendingBlockUpdatesWorldServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;

public class ChunkPendingBlockUpdateMap {
    
    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugGetPendingBlockUpdates", "false"));
    
    public static void add(IPendingBlockUpdatesWorldServer ws, NextTickListEntry e) {
        Map<Long, Set<NextTickListEntry>> map = ws.crtw$getChunkPendingUpdatesMap();
        long key = ChunkCoordIntPair.chunkXZ2Int(e.xCoord >> 4, e.zCoord >> 4);
        
        Set<NextTickListEntry> chunkSet = map.get(key);
        if(chunkSet == null) {
            chunkSet = new TreeSet<>();
            map.put(key, chunkSet);
        }
        chunkSet.add(e);
    }

    public static void remove(IPendingBlockUpdatesWorldServer ws, NextTickListEntry o) {
        Map<Long, Set<NextTickListEntry>> map = ws.crtw$getChunkPendingUpdatesMap();
        NextTickListEntry e = (NextTickListEntry)o;
        long key = ChunkCoordIntPair.chunkXZ2Int(e.xCoord >> 4, e.zCoord >> 4);
        
        Set<NextTickListEntry> chunkSet = map.get(key);
        if(chunkSet != null) {
            chunkSet.remove(e);
        }
        if(chunkSet.isEmpty()) {
            map.remove(key);
        }
    }

    public static Set<NextTickListEntry> get(IPendingBlockUpdatesWorldServer ws, int cx, int cz) {
        Map<Long, Set<NextTickListEntry>> map = ws.crtw$getChunkPendingUpdatesMap();
        long key = ChunkCoordIntPair.chunkXZ2Int(cx, cz);
        
        return map.get(key);
    }
    
    public static void removeKey(IPendingBlockUpdatesWorldServer ws, int cx, int cz) {
        Map<Long, Set<NextTickListEntry>> map = ws.crtw$getChunkPendingUpdatesMap();
        long key = ChunkCoordIntPair.chunkXZ2Int(cx, cz);
        
        map.remove(key);
    }

    public static boolean isEmpty(IPendingBlockUpdatesWorldServer ws) {
        Map<Long, Set<NextTickListEntry>> map = ws.crtw$getChunkPendingUpdatesMap();
        
        return map == null || map.isEmpty();
    }

    public static void onTick(IPendingBlockUpdatesWorldServer ws) {
        if(DEBUG) {
            Map<Long, Set<NextTickListEntry>> map = ws.crtw$getChunkPendingUpdatesMap();
            Iterator<Entry<Long, Set<NextTickListEntry>>> it = map.entrySet().iterator();
            String debug = "";
            while(it.hasNext()) {
                Entry<Long, Set<NextTickListEntry>> e = it.next();
                long k = e.getKey();
                int cx = chunkCoordPairToX(k);
                int cz = chunkCoordPairToZ(k);
                debug += "(" + cx + ", " + cz + "), ";
            }
            if(map.size() > 0) {
                LOGGER.info(map.size() + ": " + debug);
            }
        }
    }
    
    private static int chunkCoordPairToX(long pair) {
        return (int)(pair & 4294967295L);
    }
    
    private static int chunkCoordPairToZ(long pair) {
        return (int)((pair >> 32) & 4294967295L);
    }

    public static void onError() {
        System.out.println("ERROR");
    }
    
}
