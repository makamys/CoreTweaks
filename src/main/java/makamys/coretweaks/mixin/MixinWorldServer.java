package makamys.coretweaks.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;

import makamys.coretweaks.MixinLogger;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

@Mixin(WorldServer.class)
abstract class MixinWorldServer {
    
    @Shadow
    private Set pendingTickListEntriesHashSet;
    /** All work to do in future ticks. */
    @Shadow
    private TreeSet pendingTickListEntriesTreeSet;
    @Shadow
    private List pendingTickListEntriesThisTick;
    @Shadow
    static private Logger logger;
    
    Map<Long, List<NextTickListEntry>> map;
    
    @Redirect(method = {"scheduleBlockUpdateWithPriority", "func_147446_b"}, at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;add(Ljava/lang/Object;)Z"))
    public boolean redirectAdd(TreeSet set, Object o) {
        NextTickListEntry e = (NextTickListEntry)o;
        long key = ChunkCoordIntPair.chunkXZ2Int(e.xCoord / 16, e.yCoord / 16);
        
        if(map == null) map = new HashMap<>();
        List<NextTickListEntry> list = map.get(key);
        if(list == null) {
            list = new LinkedList<>();
            map.put(key, list);
        }
        list.add(e);
        return set.add(o);
    }
    
    @Redirect(method = {"tickUpdates"}, at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;remove(Ljava/lang/Object;)Z"))
    public boolean redirectRemove(TreeSet set, Object o) {
        NextTickListEntry e = (NextTickListEntry)o;
        List<NextTickListEntry> list = map.get(ChunkCoordIntPair.chunkXZ2Int(e.xCoord / 16, e.zCoord / 16));
        if(list != null) {
            list.remove(e);
        }
        return set.remove(o);
    }
    
    @Overwrite
    public List getPendingBlockUpdates(Chunk p_72920_1_, boolean p_72920_2_)
    {
        MixinLogger.printActive(this);
        
        ArrayList arraylist = null;
        ChunkCoordIntPair chunkcoordintpair = p_72920_1_.getChunkCoordIntPair();
        int i = (chunkcoordintpair.chunkXPos << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkcoordintpair.chunkZPos << 4) - 2;
        int l = k + 16 + 2;
        
        if(map != null) {
            for(int cx = p_72920_1_.xPosition - 1; cx <= p_72920_1_.xPosition + 1; cx++) {
                for(int cz = p_72920_1_.zPosition - 1; cz <= p_72920_1_.zPosition + 1; cz++) {
                    List<NextTickListEntry> list = map.get(ChunkCoordIntPair.chunkXZ2Int(cx, cz));
                    if(list != null) {
                        Iterator<NextTickListEntry> it = list.iterator();
                        while(it.hasNext()) {
                            NextTickListEntry nte = it.next();
                            if (nte.xCoord >= i && nte.xCoord < j && nte.zCoord >= k && nte.zCoord < l)
                            {
                                if (p_72920_2_)
                                {
                                    this.pendingTickListEntriesHashSet.remove(nte);
                                    this.pendingTickListEntriesTreeSet.remove(nte);
                                    it.remove();
                                }
    
                                if (arraylist == null)
                                {
                                    arraylist = new ArrayList();
                                }
    
                                arraylist.add(nte);
                            }
                        }
                    }
                }
            }
        }

        for (int i1 = 1; i1 < 2; ++i1)
        {
            Iterator iterator;

            if (i1 == 0)
            {
                iterator = this.pendingTickListEntriesTreeSet.iterator();
            }
            else
            {
                iterator = this.pendingTickListEntriesThisTick.iterator();

                if (!this.pendingTickListEntriesThisTick.isEmpty())
                {
                    logger.debug("toBeTicked = " + this.pendingTickListEntriesThisTick.size());
                }
            }

            while (iterator.hasNext())
            {
                NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();

                if (nextticklistentry.xCoord >= i && nextticklistentry.xCoord < j && nextticklistentry.zCoord >= k && nextticklistentry.zCoord < l)
                {
                    if (p_72920_2_)
                    {
                        this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                        iterator.remove();
                    }

                    if (arraylist == null)
                    {
                        arraylist = new ArrayList();
                    }

                    arraylist.add(nextticklistentry);
                }
            }
        }

        return arraylist;
    }
}
