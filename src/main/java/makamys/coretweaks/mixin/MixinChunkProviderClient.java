package makamys.coretweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import makamys.coretweaks.ducks.IChunkProviderClient;
import makamys.coretweaks.optimization.ClientChunkMap;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.World;

@Mixin(ChunkProviderClient.class)
abstract class MixinChunkProviderClient implements IChunkProviderClient {
    
    @Shadow
    LongHashMap chunkMapping;
    
    @Inject(method = "<init>*", at = @At("RETURN"))
    public void preConstructed(World world, CallbackInfo ci) {
        chunkMapping = new ClientChunkMap();
    }
    
    public LongHashMap getChunkMapping() {
        return chunkMapping;
    }
}
