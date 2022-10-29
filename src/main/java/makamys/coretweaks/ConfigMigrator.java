package makamys.coretweaks;

import static makamys.coretweaks.Config.*;
import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.function.Consumer;

import org.apache.commons.lang3.EnumUtils;

import makamys.coretweaks.Config.CloudHeightCheck;
import makamys.coretweaks.Config.TransformerCache;
import makamys.coretweaks.Config.FeatureSetting.Setting;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

/** Migrates config from 0.2 to 0.3 */
public class ConfigMigrator {
    
    private Configuration config;
    
    public ConfigMigrator(Configuration config) {
        this.config = config;
    }
    
    public void migrate() {
        LOGGER.info("Migrating config from 0.2 to 0.3");
        
        migrateFeatureSetting("bugfixes", "fixDisplayListDelete", fixDisplayListDelete);
        migrateFeatureSetting("bugfixes", "fixDoubleEat", fixDoubleEat);
        migrateFeatureSetting("bugfixes", "fixForgeChatLinkCrash", fixForgeChatLinkCrash);
        migrateFeatureSetting("bugfixes", "fixHeightmapRange", fixHeightmapRange);
        migrateFeatureSetting("bugfixes", "fixSmallEntitySwim", fixSmallEntitySwim);
        migrateFeatureSetting("bugfixes", "restoreTravelSound", restoreTravelSound);
        
        migrateFeatureSetting("optimizations", "clientChunkMap", clientChunkMap);
        migrateFeatureSetting("optimizations", "fastFolderTexturePack", fastFolderTexturePack);
        migrateFeatureSetting("optimizations", "fcOptimizeTextureUpload", fcOptimizeTextureUpload);
        migrateFeatureSetting("optimizations", "forgeFastDeobfuscationRemapper", forgeFastDeobfuscationRemapper);
        migrateFeatureSetting("optimizations", "forgeFastProgressBar", forgeFastProgressBar);
        migrateFeatureSetting("optimizations", "forgeFastStepMessageStrip", forgeFastStepMessageStrip);
        migrateFeatureSetting("optimizations", "forgeModDiscovererSkipKnownLibraries", forgeModDiscovererSkipKnownLibraries);
        migrateFeatureSetting("optimizations", "getPendingBlockUpdates", getPendingBlockUpdates);
        migrateFeatureSetting("optimizations", "jarDiscovererCache", jarDiscovererCache);
        migrateFeatureSetting("optimizations", "ofOptimizeWorldRenderer", ofOptimizeWorldRenderer);
        migrateFeatureSetting("optimizations", "tcpNoDelay", tcpNoDelay);
        migrateInt("optimizations", "threadedTextureLoaderThreadCount", (x) -> threadedTextureLoaderThreadCount = x);
        migrateEnum("optimizations", "transformerCache", (x) -> {
            if(x != null) {
                transformerCacheMode = (TransformerCache)x;
                transformerCache.setValue(Setting.TRUE);
            } else {
                transformerCache.setValue(Setting.FALSE);
            }
        },  transformerCacheMode.getClass());
        
        migrateString("transformer_cache_full", "badClasses", (x) -> badClasses = x);
        migrateString("transformer_cache_full", "badTransformers", (x) -> badTransformers = x);
        migrateString("transformer_cache_full", "modFilesToIgnore", (x) -> modFilesToIgnore = x);
        migrateInt("transformer_cache_full", "recentCacheSize", (x) -> recentCacheSize = x);
        migrateInt("transformer_cache_full", "verbosity", (x) -> verbosity = x);
        
        migrateStringList("transformer_cache_lite", "transformersToCache", (x) -> transformersToCache = x);
        
        migrateEnum("tweaks", "cloudHeightCheck", (x) -> {
            if(x != null) {
                cloudHeightCheckMode = (CloudHeightCheck)x;
                tweakCloudHeightCheck.setValue(Setting.TRUE);
            } else {
                tweakCloudHeightCheck.setValue(Setting.FALSE);
            }
            
        }, cloudHeightCheckMode.getClass());
        migrateFeatureSetting("tweaks", "crashHandler", crashHandler);
        migrateFeatureSetting("tweaks", "disableFog", disableFog);
        migrateFeatureSetting("tweaks", "forceUncapFramerate", forceUncapFramerate);
        migrateFeatureSetting("tweaks", "lightFixStare", lightFixStare);
        migrateFeatureSetting("tweaks", "mainMenuContinueButton", mainMenuContinueButton);
        migrateFloat("tweaks", "minFarPlaneDistance", (x) -> {
            minFarPlaneDistance = Math.max(0, x);
            clampFarPlaneDistance.setValue(x >= 0 ? FeatureSetting.Setting.TRUE : FeatureSetting.Setting.FALSE);
        });
        migrateFeatureSetting("tweaks", "ofFixUpdateRenderersReturnValue", ofFixUpdateRenderersReturnValue);
        migrateFeatureSetting("tweaks", "ofUnlockCustomSkyMinRenderDistance", ofUnlockCustomSkyMinRenderDistance);
        migrateFeatureSetting("tweaks", "uncapCreateWorldGuiTextFieldLength", uncapCreateWorldGuiTextFieldLength);
        
        for(String catName : config.getCategoryNames()) {
            ConfigCategory cat = config.getCategory(catName);
            if(cat.isEmpty()) {
                config.removeCategory(cat);
            }
        }
    }
    
    private void migrateFeatureSetting(String cat, String name, FeatureSetting setting) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.BOOLEAN) {
                    setting.setValue(prop.getBoolean() ? FeatureSetting.Setting.TRUE : FeatureSetting.Setting.FALSE);
                    category.remove(name);
                }
            }
        }
        
    }
    
    private void migrateInt(String cat, String name, Consumer<Integer> consumer) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.INTEGER) {
                    consumer.accept(prop.getInt());
                    category.remove(name);
                }
            }
        }
    }
    
    private void migrateString(String cat, String name, Consumer<String> consumer) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.STRING) {
                    consumer.accept(prop.getString());
                    category.remove(name);
                }
            }
        }
    }
    
    private void migrateFloat(String cat, String name, Consumer<Float> consumer) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.DOUBLE) {
                    consumer.accept((float)prop.getDouble());
                    category.remove(name);
                }
            }
        }
    }
    
    private void migrateStringList(String cat, String name, Consumer<String[]> consumer) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.STRING) {
                    consumer.accept(prop.getStringList());
                    category.remove(name);
                }
            }
        }
    }
    
    private void migrateEnum(String cat, String name, Consumer<Enum<?>> consumer, Class<? extends Enum> enumClass) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.STRING) {
                    consumer.accept((Enum<?>) EnumUtils.getEnumMap(enumClass).get(prop.getString().toUpperCase()));
                    category.remove(name);
                }
            }
        }
    }
    
}
