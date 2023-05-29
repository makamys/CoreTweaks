package makamys.coretweaks.mixin.diagnostics.enhancemapstorageerrors;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;

@Mixin(MapStorage.class)
abstract class MixinMapStorage {
    @Shadow
    private ISaveHandler saveHandler;
    
    @Inject(method = "saveData", at = @At(value = "INVOKE", target = "Ljava/lang/Exception;printStackTrace()V"))
    private void printSaveFile(WorldSavedData data, CallbackInfo ci) {
        LOGGER.error("Failed to save data to " + saveHandler.getMapFileFromName(data.mapName));
    }
}
