package makamys.coretweaks.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigDumper {

    public static void dumpConfigIfEnabled(Configuration config, String modid) {
        boolean enabled = Boolean.parseBoolean(System.getProperty(modid + ".configDumper.enabled", "false"));
        if(enabled) {
            dumpConfig(config, modid);
        }
    }
    
    public static void dumpConfig(Configuration config, String modid) {
        File outFile = new File(Launch.minecraftHome, "config-export-" + config.getConfigFile().getName() + ".md");
        try(FileWriter fw = new FileWriter(outFile)){
            for(String category : config.getCategoryNames()) {
                String out = "";
                
                ConfigCategory cat = config.getCategory(category);
                String catName = cat.getQualifiedName();
                String catBaseName = cat.getName();
                int level = StringUtils.countMatches(catName, ".");
                
                if(catName.startsWith("_")) {
                    continue;
                }
                
                String catComment = StringUtils.defaultString(cat.getComment());
                if(cat.containsKey("_enabled")) {
                    catComment = cat.get("_enabled").comment;
                }
                
                out += createEntry(level, catBaseName, commentToMarkdown(catComment));
                
                for(Property prop : cat.getValues().values()) {
                    if(!prop.getName().startsWith("_")) {
                        out += createEntry(level + 1, prop.getName(), commentToMarkdown(prop.comment));
                    }
                }
                
                fw.write(out);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    private static String createEntry(int level, String title, String body) {
        if(level == 0) {
            return "\n# " + title + "\n" + body + "\n";
        } else {
            return createIndent(level - 1) + "* **" + title + "**<br>" + body.replaceAll("\n\n", "<br>") + "\n";
        }
    }
    
    private static String createIndent(int level) {
        return level == 0 ? "" : String.format("%" + level * 4 + "c", ' ');
    }

    private static String commentToMarkdown(String comment) {
        String outText = "";
        
        boolean backticksBetwixt = false;
        for(int i = 0; i < comment.length(); i++) {
            char c = comment.charAt(i);
            String outChar = String.valueOf(c);
            if(c == '`') {
                backticksBetwixt = !backticksBetwixt;
            }
            
            if(c == '<' && !backticksBetwixt) {
                outChar = "&lt;";
            }
            
            outText += outChar;
        }
        return outText.replaceAll("\n", "\n\n");
    }

}
