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
                String outTitle = "";
                String outBody = "";
                
                ConfigCategory cat = config.getCategory(category);
                String catName = cat.getQualifiedName();
                
                if(catName.startsWith("_")) {
                    continue;
                }
                
                if(catName.contains(".")) {
                    catName = catName.substring(catName.indexOf(".") + 1);
                    outTitle += "#";
                    
                    if(!cat.containsKey("_enabled")) {
                        outTitle += "#";
                    }
                }
                outTitle += "# " + catName + "\n\n";
                
                String comment = cat.getComment();
                
                if(comment != null) {
                    outBody += commentToMarkdown(cat.getComment());
                    outBody += "\n\n";
                }
                
                for(Property prop : cat.getValues().values()) {
                    if(!prop.getName().equals("_enabled")) {
                        outBody += "### " + catName + "." + prop.getName() + "\n";
                    }
                    outBody += commentToMarkdown(prop.comment) + "\n\n";
                }
                
                if(!cat.getQualifiedName().contains(".") || !outBody.isEmpty()) {
                    fw.write(outTitle);
                    fw.write(outBody);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
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
