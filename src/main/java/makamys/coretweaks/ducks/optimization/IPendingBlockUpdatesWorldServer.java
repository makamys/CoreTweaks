package makamys.coretweaks.ducks.optimization;

import java.util.Map;
import java.util.Set;

import net.minecraft.world.NextTickListEntry;

public interface IPendingBlockUpdatesWorldServer {
    
    public Map<Long, Set<NextTickListEntry>> crtw$getChunkPendingUpdatesMap();
    
}
