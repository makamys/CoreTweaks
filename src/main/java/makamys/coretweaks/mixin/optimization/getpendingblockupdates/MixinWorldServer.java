package makamys.coretweaks.mixin.optimization.getpendingblockupdates;

import static makamys.coretweaks.optimization.ChunkPendingBlockUpdateMap.DEBUG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.ducks.optimization.IPendingBlockUpdatesWorldServer;
import makamys.coretweaks.optimization.ChunkPendingBlockUpdateMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

@Mixin(WorldServer.class)
abstract class MixinWorldServer implements IPendingBlockUpdatesWorldServer {
    
    @Shadow
    private Set pendingTickListEntriesHashSet;
    /** All work to do in future ticks. */
    @Shadow
    private TreeSet pendingTickListEntriesTreeSet;
    @Shadow
    private List pendingTickListEntriesThisTick;
    @Shadow
    @Final
    static private Logger logger;
    
    /** Chunk coordinate -> Block updates in that chunk */
    private Map<Long, Set<NextTickListEntry>> crtw$chunkPendingUpdatesMap;
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ChunkPendingBlockUpdateMap.onTick(this);
    }
    
    @Redirect(method = {"scheduleBlockUpdateWithPriority", "func_147446_b"}, at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;add(Ljava/lang/Object;)Z", remap = false))
    public boolean redirectAdd(TreeSet set, Object o) {
        ChunkPendingBlockUpdateMap.add(this, (NextTickListEntry)o);
        return set.add(o);
    }
    
    @Redirect(method = {"tickUpdates"}, at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;remove(Ljava/lang/Object;)Z", remap = false))
    public boolean redirectRemove(TreeSet set, Object o) {
        ChunkPendingBlockUpdateMap.remove(this, (NextTickListEntry)o);
        return set.remove(o);
    }
    
    /**
     * @author makamys
     * @reason Use map instead of iterating over the full contents of pendingTickListEntriesTreeSet for more fastness.
     * */
    @Overwrite
    public List getPendingBlockUpdates(Chunk p_72920_1_, boolean p_72920_2_)
    {
        ArrayList arraylist = null;
        ChunkCoordIntPair chunkcoordintpair = p_72920_1_.getChunkCoordIntPair();
        int i = (chunkcoordintpair.chunkXPos << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkcoordintpair.chunkZPos << 4) - 2;
        int l = k + 16 + 2;
        
        // New code start
        
        if(!ChunkPendingBlockUpdateMap.isEmpty(this)) {
            for(int cx = p_72920_1_.xPosition - 1; cx <= p_72920_1_.xPosition + 1; cx++) {
                for(int cz = p_72920_1_.zPosition - 1; cz <= p_72920_1_.zPosition + 1; cz++) {
                    Set<NextTickListEntry> chunkSet = ChunkPendingBlockUpdateMap.get(this, cx, cz);
                    if(chunkSet != null) {
                        Iterator<NextTickListEntry> it = chunkSet.iterator();
                        while(it.hasNext()) {
                            NextTickListEntry nte = it.next();
                            if (nte.xCoord >= i && nte.xCoord < j && nte.zCoord >= k && nte.zCoord < l)
                            {
                                if (p_72920_2_)
                                {
                                    this.pendingTickListEntriesHashSet.remove(nte);
                                    this.pendingTickListEntriesTreeSet.remove(nte);
                                    it.remove();
                                    if(chunkSet.isEmpty()) {
                                        ChunkPendingBlockUpdateMap.removeKey(this, cx, cz);
                                    }
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
        
        List<NextTickListEntry> debug_myList = null;
        if(DEBUG && arraylist != null) {
            debug_myList = new ArrayList<>();
            debug_myList.addAll(arraylist);
            arraylist.clear();
        }

        for (int i1 = DEBUG ? 0 : 1; i1 < 2; ++i1)
        // New code end
        {
            if(DEBUG && i1 == 1) {
                if (!((debug_myList == null && arraylist == null) || (debug_myList.equals(arraylist)))){
                    ChunkPendingBlockUpdateMap.onError();
                }
            }
            
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
    
    @Override
    public Map<Long, Set<NextTickListEntry>> crtw$getChunkPendingUpdatesMap() {
        if(crtw$chunkPendingUpdatesMap == null) {
            crtw$chunkPendingUpdatesMap = new HashMap<>();
        }
        return crtw$chunkPendingUpdatesMap;
    }
}
