package makamys.coretweaks;

import static makamys.coretweaks.Config.*;
import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private List<String> warnings = new ArrayList<>();
    
    public ConfigMigrator(Configuration config) {
        this.config = config;
    }
    
    public void writeWarnings() {
        if(warnings.isEmpty()) return;
        
        File outFile = new File(config.getConfigFile().getParentFile(), config.getConfigFile().getName() + ".migration_log.txt");
        boolean writeIntro = !outFile.exists();
        try(FileWriter fw = new FileWriter(outFile, true)) {
            if(writeIntro) {
                fw.write("Warnings encountered while migrating the config are logged here.\nFeel free to delete this file once you are done looking at it.\n\n");
            }
            for(String warning : warnings) {
                fw.write(warning + "\n");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write migration log");
            e.printStackTrace();
        }
    }
    
    public void migrate_0_2_to_0_3() {
        LOGGER.info("Migrating config from 0.2 to 0.3");
        
        migrateFeatureSetting("bugfixes", "fixDisplayListDelete", fixDisplayListDelete);
        migrateFeatureSetting("bugfixes", "fixDoubleEat", fixDoubleEat);
        migrateFeatureSetting("bugfixes", "fixForgeChatLinkCrash", fixForgeChatLinkCrash);
        migrateFeatureSetting("bugfixes", "fixHeightmapRange", fixHeightmapRange);
        migrateFeatureSetting("bugfixes", "fixSmallEntitySwim", fixSmallEntitySwim);
        migrateFeatureSetting("bugfixes", "restoreTravelSound", restoreTravelSound);
        
        migrateFeatureSetting("optimizations", "clientChunkMap", clientChunkMap);
        migrateFeatureSetting("optimizations", "fastFolderTexturePack", fastFolderResourcePack);
        migrateFeatureSetting("optimizations", "fcOptimizeTextureUpload", fcOptimizeTextureUpload);
        migrateFeatureSetting("optimizations", "forgeFastDeobfuscationRemapper", forgeFastDeobfuscationRemapper);
        migrateFeatureSetting("optimizations", "forgeFastProgressBar", forgeFastProgressBar);
        migrateFeatureSetting("optimizations", "forgeFastStepMessageStrip", forgeFastStepMessageStrip);
        migrateFeatureSetting("optimizations", "forgeModDiscovererSkipKnownLibraries", forgeModDiscovererSkipKnownLibraries);
        migrateFeatureSetting("optimizations", "getPendingBlockUpdates", optimizeGetPendingBlockUpdates);
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
        
        migrateStringList("transformer_cache_lite", "transformersToCache", (x) -> transformersToCache = new StringList(x));
        
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
        
        deleteBooleanIfDefault("diagnostics", "coreTweaksCommand", true);
        deleteBooleanIfDefault("diagnostics", "crasher", false);
        deleteBooleanIfDefault("diagnostics", "forgeBarProfiler", false);
        deleteBooleanIfDefault("diagnostics", "frameProfilerHooks", false);
        deleteBooleanIfDefault("diagnostics", "frameProfilerPrint", false);
        deleteBooleanIfDefault("diagnostics", "frameProfilerStartEnabled", false);
        deleteStringIfDefault("diagnostics", "profilerMethods", "");
        deleteBooleanIfDefault("diagnostics", "serverRunTimePrinter", false);
        deleteBooleanIfDefault("diagnostics", "wireframe", false);
        
        deleteBooleanIfDefault("tweaks", "autoLoadDingOnWorldEntry", true);
        deleteBooleanIfDefault("tweaks", "autoLoadPauseOnWorldEntry", true);
        deleteIntIfDefault("tweaks", "autoLoadPauseWaitLength", 20);
        
        removeEmptyCategories();
    }
    
    private void removeEmptyCategories() {
        for(String catName : config.getCategoryNames()) {
            ConfigCategory cat = config.getCategory(catName);
            if(cat.isEmpty()) {
                config.removeCategory(cat);
            }
        }
    }
    
    public void migrate_0_3_0_to_0_3_1(boolean newer) {
        LOGGER.info("Migrating config from 0.3 to 0.3.1");
        
        migrateRenamedFeatureSetting("optimizations.fast_folder_texture_pack", Config.fastFolderResourcePack);
        migrateRenamedFeatureSetting("optimizations.fast_default_texture_pack", Config.fastDefaultResourcePack);
        deleteIntIfDefault("tweaks.extend_sprint_time_limit", "sprintTimeLimit", 2147483647);
        
        if(newer) {
            if(((Setting)Config.extendSprintTimeLimit.getValue()) == Setting.FALSE) {
                warn("0.3.1", "The default value of extend_sprint_time_limit has changed to TRUE, but you have it set to the old default value of FALSE. You might wish to enable it.");
            }
        }
        
        removeEmptyCategories();
    }
    
    private void warn(String version, String message) {
        String fullMessage = "Warning while migrating config to " + version + ": " + message;
        LOGGER.warn(fullMessage);
        warnings.add(fullMessage);
    }
    
    private void migrateRenamedFeatureSetting(String category, FeatureSetting setting) {
        try {
            if(config.hasCategory(category)) {
                ConfigCategory cat = config.getCategory(category);
                if(cat != null) {
                    setting.setValue(FeatureSetting.Setting.valueOf(cat.get("_enabled").getString().toUpperCase()));
                    cat.remove("_enabled");
                }
            }
        } catch(Exception e) {
            LOGGER.warn("Something went wrong while trying to migrate " + category);
            e.printStackTrace();
        }
    }
    
    private void deleteIntIfDefault(String cat, String name, int def) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.INTEGER && prop.getInt() == def) {
                    category.remove(name);
                }
            }
        }
    }

    private void deleteStringIfDefault(String cat, String name, String def) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.STRING && prop.getString().equals(def)) {
                    category.remove(name);
                }
            }
        }
    }

    private void deleteBooleanIfDefault(String cat, String name, boolean def) {
        if(config.hasCategory(cat)) {
            ConfigCategory category = config.getCategory(cat);
            
            if(category.containsKey(name)) {
                Property prop = category.get(name);
                if(prop.getType() == Type.BOOLEAN && prop.getBoolean() == def) {
                    category.remove(name);
                }
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
                // Forge stores floats as strings for some reason
                if(prop.getType() == Type.STRING) {
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
