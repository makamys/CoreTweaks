package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

public class ClientChunkMap extends LongHashMap {
    
    int radius = 32; // TODO
    int offX, offZ;
    
    int chunksContained = 0;
    
    Chunk[][] map;
    
    Map<Long, Chunk> farMap = new HashMap<>();
    
    LongHashMap testMap;
    boolean test = Boolean.parseBoolean(System.getProperty("owg.debugClientChunkMap", "false"));
    
    public ClientChunkMap() {
        map = new Chunk[radius * 2 + 1][radius * 2 + 1];
        
        if(test) {
            testMap = new LongHashMap();
        }
    }
    
    public void onBug() {
        LOGGER.info("Bug detected in ClientChunkMap");
    }
    
    public int getNumHashElements()
    {
        int result = chunksContained;
        
        if(testMap != null) {
            if(result != testMap.getNumHashElements()) {
                onBug();
            }
        }
        
        return result;
    }
    /**
     * get the value from the map given the key
     */
    public Object getValueByKey(long xz)
    {   
        int chunkX = (int)(xz & 0xFFFFFFFF);
        int chunkZ = (int)((xz >> 32) & 0xFFFFFFFF);

        Chunk result = null;
        
        if(isInRange(chunkX, chunkZ)) {
            result = getMapElement(chunkX, chunkZ);
        } else {
            result = farMap.get(xz);
        }
        
        if(testMap != null) {
            if(result != testMap.getValueByKey(xz)) {
                onBug();
            }
        }
        
        return result;
    }
    
    private Chunk getMapElement(int x, int z) {
        int mapX = Math.floorMod(x, radius * 2 + 1);
        int mapZ = Math.floorMod(z, radius * 2 + 1);
        return map[mapX][mapZ];
    }
    
    private void putMapElement(Chunk chunk, int x, int z) {
        int mapX = Math.floorMod(x, radius * 2 + 1);
        int mapZ = Math.floorMod(z, radius * 2 + 1);
        
        if(chunkXinternal2world(mapX) != x || chunkZinternal2world(mapZ) != z) {
            onBug();
        }
        
        map[mapX][mapZ] = chunk;
    }
    
    private boolean isInRange(int x, int z) {
        return x >= offX && x < (offX + 2 * radius + 1) && z >= offZ && z < (offZ + 2 * radius + 1); 
    }

    public boolean containsItem(long xz)
    {
        int chunkX = (int)(xz & 0xFFFFFFFF);
        int chunkZ = (int)((xz >> 32) & 0xFFFFFFFF);
        
        boolean result = (isInRange(chunkX, chunkZ) && getMapElement(chunkX, chunkZ) != null) || farMap.containsKey(xz);
        
        if(testMap != null) {
            if(result != testMap.containsItem(xz)) {
                onBug();
            }
        }
        
        return result;
    }
    
    /**
     * Add a key-value pair.
     */
    public void add(long key, Object obj)
    {
        if(obj instanceof Chunk) {
            Chunk chunk = (Chunk)obj;
            if(isInRange(chunk.xPosition, chunk.zPosition)) {
                putMapElement(chunk, chunk.xPosition, chunk.zPosition);
            } else {
                farMap.put(key, chunk);
            }
            chunksContained++;
        } else {
            throw new IllegalArgumentException("tried to add non-chunk object " + obj + " to ClientChunkMap!");
        }
        
        if(testMap != null) {
            testMap.add(key, obj);
        }
    }
    
    /**
     * calls the removeKey method and returns removed object
     */
    public Object remove(long xz)
    {
        int chunkX = (int)(xz & 0xFFFFFFFF);
        int chunkZ = (int)((xz >> 32) & 0xFFFFFFFF);
        
        chunksContained--;
        
        Object result;
        if(isInRange(chunkX, chunkZ)) {
            Chunk chunk = getMapElement(chunkX, chunkZ);
            putMapElement(null, chunkX, chunkZ);
            result = chunk;
        } else {
            result = farMap.remove(xz);
        }
        
        if(testMap != null) {
            if(result != testMap.remove(xz)) {
                onBug();
            }
        }
        return result;
    }
    
    private int getDiameter() {
        return radius * 2 + 1;
    }
    
    private int chunkXinternal2world(int x) {
        return (Math.floorDiv(offX, getDiameter()) + (x < (Math.floorMod(offX, getDiameter())) ? 1 : 0)) * getDiameter() + x;
    }
    
    private int chunkZinternal2world(int z) {
        return (Math.floorDiv(offZ, getDiameter()) + (z < (Math.floorMod(offZ, getDiameter())) ? 1 : 0)) * getDiameter() + z;
    }
    
    private void updateDirty(int x, int z) {
        x = Math.floorMod(x, getDiameter());
        z = Math.floorMod(z, getDiameter());
        
        int worldX = chunkXinternal2world(x);
        int worldZ = chunkZinternal2world(z);
        
        Chunk chunk = getMapElement(x, z);
        if(chunk != null && !isInRange(chunk.xPosition, chunk.zPosition)) {
            farMap.put(ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition), chunk);
            map[x][z] = null;
        }
        
        Chunk farChunk = farMap.remove(ChunkCoordIntPair.chunkXZ2Int(worldX, worldZ));
        
        if(farChunk != null) {
            putMapElement(farChunk, farChunk.xPosition, farChunk.zPosition);
        }
    }
    
    public void setCenter(int newCenterX, int newCenterZ) {
        int newX = newCenterX - radius;
        int newZ = newCenterZ - radius;
        
        int oldOffX = offX;
        int oldOffZ = offZ;
        
        if(newX == offX && newZ == offZ) return;
        
        offX = newX;
        offZ = newZ;
        
        int signDx = (int)Math.signum(newX - oldOffX);
        int signDz = (int)Math.signum(newZ - oldOffZ);
        for(int px = oldOffX + (signDx > 0 ? 0 : -1); px != newX + (signDx > 0 ? 0 : -1); px += signDx) {
            for(int z = 0; z < getDiameter(); z++) {
                updateDirty(px, z);
            }
        }
        for(int pz = oldOffZ + (signDz > 0 ? 0 : -1); pz != newZ + (signDz > 0 ? 0 : -1); pz += signDz) {
            for(int x = 0; x < getDiameter(); x++) {
                updateDirty(x, pz);
            }
        }
        
        if(test) {
            for(Chunk chunk : farMap.values()) {
                if(isInRange(chunk.xPosition, chunk.zPosition)) {
                    onBug();
                }
            }
        }
    }
}
