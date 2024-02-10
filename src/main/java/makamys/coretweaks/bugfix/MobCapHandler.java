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
            List<EnumCreatureType> vanillaTypes = computeVanillaCreatureTypes(entity);
            EnumCreatureType spawnType;
            
            if(vanillaTypes.isEmpty()) {
                // e.g. imc.entities.EntityWildSheep replaces vanilla sheep but doesn't spawn normally
                LOGGER.debug("Creature " + entity.getClass().getName() + " has no type according to vanilla logic, leaving type as default: " + vanillaTypes);
                spawnType = null;
            } else {
                spawnType = computeSpawnType(entity);
                if(spawnType == null) {
                    // e.g. Reika.ChromatiCraft.Entity.EntityBallLightning has no type, and uses custom logic for despawning
                    LOGGER.debug("Creature " + entity.getClass().getName() + " is not in the biome spawn registry, leaving type as default: " + vanillaTypes);
                    spawnType = vanillaTypes.get(0);
                }
            }
            spawnTypeOpt = Optional.ofNullable(spawnType);
            creatureTypeLookup.put(cls, spawnTypeOpt);
            
            if(vanillaTypes.size() > 1
                    || (vanillaTypes.size() == 1 && vanillaTypes.get(0) != spawnType)) {
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
                    List<BiomeGenBase.SpawnListEntry> spawnableList = biome.getSpawnableList(type);
                    if(spawnableList != null) {
                        for(BiomeGenBase.SpawnListEntry entry : spawnableList) {
                            if(entry.entityClass == entity.getClass()) {
                                counts[type.ordinal()]++;
                                break;
                            }
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
                LOGGER.debug("Entity " + entity.getClass().getName() + " has multiple spawn types registered: " + types + ". Choosing " + type);
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
