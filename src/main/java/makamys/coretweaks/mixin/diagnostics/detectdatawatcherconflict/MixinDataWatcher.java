package makamys.coretweaks.mixin.diagnostics.detectdatawatcherconflict;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.Config;
import makamys.coretweaks.diagnostics.DataWatcherMonitor;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;

@Mixin(DataWatcher.class)
public class MixinDataWatcher {
    @Shadow
    private Entity field_151511_a;
    @Shadow
    private Map watchedObjects;
    
    @Inject(method = "addObject", at = @At("HEAD"))
    public void monitorObjectAddition(int id, Object object, CallbackInfo ci) {
        if(Config.detectDataWatcherIdConflictCulprit && field_151511_a != null) {
            DataWatcherMonitor.onAddition((DataWatcher)(Object)this, field_151511_a.getClass().getName(), id);
        }
    }
    
    @Inject(method = "addObjectByDataType", at = @At("HEAD"))
    public void monitorTypedObjectAddition(int id, int type, CallbackInfo ci) {
        boolean printedCulprit = false;
        if(Config.detectDataWatcherIdConflictCulprit && field_151511_a != null) {
            printedCulprit = true;
            DataWatcherMonitor.onAddition((DataWatcher)(Object)this, field_151511_a.getClass().getName(), id);
        }
        if(watchedObjects.containsKey(id)) {
            LOGGER.warn("Detected duplicate DataWatcher object registration for entity " + (field_151511_a == null ? "null" : field_151511_a.getClass().getName()) + " at already occupied ID " + id + ". Things are likely going to break!" + (!Config.detectDataWatcherIdConflictCulprit ? " Enable `detectDataWatcherIdConflictCulprit` to gather more information." : ""));
            if(!printedCulprit) LOGGER.warn("Last registration stack trace:\n" + ExceptionUtils.getStackTrace(new Throwable()));
        }
    }
}
