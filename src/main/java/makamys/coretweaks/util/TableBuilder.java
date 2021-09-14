package makamys.coretweaks.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;

/** A table you can build row by row, and export to a CSV. */
public class TableBuilder<C extends Enum, V> {
	private List<C> entryKeys = new ArrayList<>();
	private List<V> entryValues = new ArrayList<>();
	
    public void set(C column, V value) {
    	entryKeys.add(column);
    	entryValues.add(value);
    }
    
    public void endRow() {
    	if(!entryKeys.isEmpty()) {
	    	entryKeys.add(null);
	    	entryValues.add(null);
    	}
    }
    
    public void writeToCSV(File file) throws IOException {
    	List<C> vals = EnumUtils.getEnumList((Class<C>)entryKeys.get(0).getClass());
    	try(FileWriter writer = new FileWriter(file)) {
    		writer.write(String.join(",", vals.stream().map(String::valueOf).toArray(String[]::new)) + "\n");
    		Map<C, V> row = new HashMap<>();
    		String[] rowValues = new String[vals.size()];
    		
    		for(int entryI = 0; entryI < entryKeys.size(); entryI++) {
    			C key = entryKeys.get(entryI);
    			V value = entryValues.get(entryI);
    			if(key == null) { // end of row
    				for(int valI = 0; valI < vals.size(); valI++) {
    					C rowKey = vals.get(valI);
    					V rowValue = row.get(rowKey);
    					rowValues[valI] = rowValue != null ? String.valueOf(rowValue) : "";
    				}
    				writer.write(String.join(",", rowValues) + "\n");
    				row.clear();
    				Arrays.fill(rowValues, null);
    			} else {
    				row.put(key, value);
    			}
    		}
    	} catch(IOException e) {
    		throw e;
    	}
    }
}
