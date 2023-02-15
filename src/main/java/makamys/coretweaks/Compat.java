package makamys.coretweaks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import cpw.mods.fml.common.versioning.ComparableVersion;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

public class Compat {

    public static boolean isBetterCrashesPresent() {
        return Config.class.getResource("/vfyjxf/bettercrashes/BetterCrashes.class") != null;
    }
    
    public static boolean isCrashGuardPresent() {
        return Config.class.getResource("/com/falsepattern/crashguard/CrashGuard.class") != null;
    }
    
    public static boolean isHodgepodgeChatLinkCrashFixEnabled() {
        String version = getModVersion("com.mitchej123.hodgepodge.Hodgepodge");

        if(version != null) {
            if(new ComparableVersion(version).compareTo(new ComparableVersion("1.6.14")) >= 0) {
                Configuration config = new Configuration(new File(Launch.minecraftHome, "config/hodgepodge.cfg"));
                config.load();
                ConfigCategory cat = config.getCategory("fixes");
                if(cat.containsKey("fixUrlDetection")) {
                    return cat.get("fixUrlDetection").getBoolean();
                } else {
                    // Unknown format, let's be on the safe side and yield
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBackport5160Present() {
        return Config.class.getResource("/ru/itaros/backport5160/Forge5160Plugin.class") != null;
    }

    public static boolean isOptifinePresent() {
        return Config.class.getResource("/optifine/OptiFineTweaker.class") != null;
    }
    
    private static String getModVersion(String className) {
        String path = "/" + className.replace('.', '/') + ".class";
        try(InputStream is = Compat.class.getResourceAsStream(path)) {
            if(is != null) {
                byte[] data = IOUtils.toByteArray(is);
                VersionRetrievingModClassVisitor visitor = new VersionRetrievingModClassVisitor();
                ClassReader classReader = new ClassReader(data);
                classReader.accept(visitor, 0);
                
                return visitor.modVersion;
            }
        } catch (IOException e) {
            
        }
        return null;
    }
    
    private static class VersionRetrievingModClassVisitor extends ClassVisitor {
        
        public String modVersion;
        
        public VersionRetrievingModClassVisitor() {
            super(Opcodes.ASM5);
        }
        
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if(desc.equals("Lcpw/mods/fml/common/Mod;")) {
                return new ModAnnotationVisitor();
            } else {
                return super.visitAnnotation(desc, visible);
            }
        }
                
        private class ModAnnotationVisitor extends AnnotationVisitor {

            public ModAnnotationVisitor() {
                super(Opcodes.ASM5);
            }
            
            @Override
            public void visit(String name, Object value) {
                if(name.equals("version")) {
                    modVersion = (String)value;
                }
            }
            
        }
    }
    
}
