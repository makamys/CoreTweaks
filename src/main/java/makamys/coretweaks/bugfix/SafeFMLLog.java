package makamys.coretweaks.bugfix;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

@SuppressWarnings("static-access")
public class SafeFMLLog {

    public static void log(FMLRelaunchLog coreLog, String targetLog, Level level, Throwable ex, String format, Object[] data) {
        coreLog.log(targetLog, level, ex, format, data);
    }
    
    public static void log(FMLRelaunchLog coreLog, Level level, Throwable ex, String format, Object[] data) {
        coreLog.log(level, ex, format, data);
    }

}
