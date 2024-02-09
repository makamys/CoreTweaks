package makamys.coretweaks.bugfix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;

import static makamys.coretweaks.CoreTweaks.LOGGER;

public class MobCapHandler {

    private static Map<Class<? extends Entity>, Optional<EnumCreatureType>> creatureTypeLookup = new HashMap<>();
    
    public static boolean isCreatureType(Entity entity, EnumCreatureType type) {
        Class<? extends Entity> cls = entity.getClass();
        Optional<EnumCreatureType> spawnTypeOpt = creatureTypeLookup.get(cls);
        if(spawnTypeOpt == null) {
            EnumCreatureType spawnType = computeSpawnType(entity);
            spawnTypeOpt = Optional.ofNullable(spawnType);
            creatureTypeLookup.put(cls, spawnTypeOpt);
            
            List<EnumCreatureType> vanillaTypes = computeVanillaCreatureTypes(entity);
            if(vanillaTypes.size() > 1
                    || (vanillaTypes.size() == 1 && vanillaTypes.get(0) != spawnType)
                    || (vanillaTypes.isEmpty() && spawnType != null)) {
                LOGGER.debug("Changed creature type of " + entity.getClass().getName() + " from " + vanillaTypes + " to " + spawnType);
            }
        }
        return spawnTypeOpt.orElse(null) == type;
    }

    // We could also hook EntityRegistry#addSpawn but this feels safer
    @SuppressWarnings("unchecked")
    private static EnumCreatureType computeSpawnType(Entity entity) {
        int[] counts = new int[EnumCreatureType.values().length];
        for(BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if(biome != null) {
                for(EnumCreatureType type : EnumCreatureType.values()) {
                    ListLoop:
                    for(BiomeGenBase.SpawnListEntry entry : (List<BiomeGenBase.SpawnListEntry>)biome.getSpawnableList(type)) {
                        if(entry.entityClass == entity.getClass()) {
                            counts[type.ordinal()]++;
                            break ListLoop;
                        }
                    }
                }
            }
        }
        
        int candidateCount = countNonZero(counts);
        
        if(candidateCount == 0) {
            return null;
        } else {
            EnumCreatureType type = EnumCreatureType.values()[maxIndex(counts)];
            
            if(candidateCount != 1) {
                List<EnumCreatureType> types = new ArrayList<>();
                for(int i = 0; i < counts.length; i++) {
                    if(counts[i] != 0) {
                        types.add(EnumCreatureType.values()[i]);
                    }
                }
                LOGGER.debug("Entity " + entity + " has multiple spawn types registered: " + types + ". Choosing " + type);
            }
            
            return type;
        }
    }
    
    private static List<EnumCreatureType> computeVanillaCreatureTypes(Entity entity) {
        List<EnumCreatureType> types = new ArrayList<>();
        for(EnumCreatureType type : EnumCreatureType.values()) {
            if(vanillaIsCreatureType(entity, type)) {
                types.add(type);
            }
        }
        return types;
    }
    
    @SuppressWarnings("unchecked")
    private static boolean vanillaIsCreatureType(Entity entity, EnumCreatureType type) {
        return type.getCreatureClass().isAssignableFrom(entity.getClass());
    }
    
    private static int maxIndex(int[] a) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        for(int i = 0; i < a.length; i++) {
            if(a[i] > max) {
                max = a[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    private static int countNonZero(int[] a) {
        int c = 0;
        for(int i : a) {
            if(i != 0) {
                c++;
            }
        }
        return c;
    }

}
