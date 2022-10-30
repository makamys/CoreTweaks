package makamys.coretweaks.asm.itaros.backport5160;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import makamys.coretweaks.asm.itaros.asmutils.NameResolutionException;

public class DebugProbe {

    private static Logger logger;

    static{
        logger = LogManager.getLogger("forge5160");
    }

    public static void notifyCall(String message){
        logger.error(message);
    }

    public static void error(Exception e) {
        logger.error("============ERROR============");
        logger.error("Critical instrumentation(class change canceled) exception:");
        logger.error(e);
        logger.error("=============================");
    }
}
