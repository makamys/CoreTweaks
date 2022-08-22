package makamys.coretweaks.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigDumper {

    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("coretweaks.configDumper.enabled", "false"));

    public static void dumpConfig(Configuration config) {
        File outFile = new File(Launch.minecraftHome, "config-export-" + config.getConfigFile().getName() + ".md");
        try(FileWriter fw = new FileWriter(outFile)){
            for(String category : config.getCategoryNames()) {
                ConfigCategory cat = config.getCategory(category);
                fw.write("# " + cat.getName() + "\n\n");
                for(Property prop : cat.getValues().values()) {
                    fw.write("### " + prop.getName() + "\n" + prop.comment.replaceAll("\n", "\n\n") + "\n\n");
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
