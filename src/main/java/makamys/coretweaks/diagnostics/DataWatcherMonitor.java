package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.minecraft.entity.DataWatcher;

public class DataWatcherMonitor {
    private static WeakHashMap<DataWatcher, Map<Integer, List<AdditionRecord>>> data = new WeakHashMap<>();
    
    public static void onAddition(DataWatcher dw, String entityClassName, int id) {
        AdditionRecord record = new AdditionRecord();
        List<AdditionRecord> recordsForId = data.computeIfAbsent(dw, k -> new HashMap<>()).computeIfAbsent(id, k -> new ArrayList<>());
        recordsForId.add(record);
        
        if(recordsForId.size() > 1) {
            LOGGER.warn("Detected DataWatcher ID conflict at ID " + id + " for entity " + entityClassName);
            for(int i = 0; i < recordsForId.size(); i++) {
                LOGGER.warn("Stack trace for registration #" + (i + 1) + " (out of " + recordsForId.size() + "): ");
                LOGGER.warn(recordsForId.get(i).stackTrace);
            }
        }
    }
    
    public static class AdditionRecord {
        public final String stackTrace;
        public AdditionRecord() {
            stackTrace = ExceptionUtils.getStackTrace(new Throwable());
        }
    }
}
