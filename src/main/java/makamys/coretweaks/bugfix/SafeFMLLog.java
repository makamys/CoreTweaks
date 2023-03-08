package makamys.coretweaks.bugfix;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

@SuppressWarnings("static-access")
public class SafeFMLLog {

    public static void log(FMLRelaunchLog coreLog, String targetLog, Level level, Throwable ex, String format, Object[] data) {
        coreLog.log(targetLog, level, format + "\n" + ExceptionUtils.getStackTrace(ex), data);
    }
    
    public static void log(FMLRelaunchLog coreLog, Level level, Throwable ex, String format, Object[] data) {
        coreLog.log(level, format + "\n" + ExceptionUtils.getStackTrace(ex), data);
    }

}
