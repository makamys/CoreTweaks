package makamys.coretweaks.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

public class AnnotationBasedConfigHelper {
    
    private Logger logger;
    private Class<?> theConfigClass;
    
    public AnnotationBasedConfigHelper(Class<?> theConfigClass, Logger logger) {
        this.logger = logger;
        this.theConfigClass = theConfigClass;
    }
    
    public boolean loadFields(Configuration config) {
        return iterateOverConfigAnd(config, this::setConfigClassField);
    }
    
    private void setConfigClassField(Field field, Object newValue, Configuration config) {
        try {
            if(ILoadable.class.isAssignableFrom(field.getType())) {
                ILoadable instance = (ILoadable)field.getType().newInstance();
                instance.setValue(newValue, field, config);
                field.set(null, instance);
            } else {
                field.set(null, newValue);
            }
        } catch (Exception e) {
            logger.error("Failed to set value of field " + field.getName());
            e.printStackTrace();
        }
    }
    
    private boolean iterateOverConfigAnd(Configuration config, TriConsumer<Field, Object, Configuration> callback) {
        boolean needReload = false;
        
        for(Field field : theConfigClass.getFields()) {
            if(!Modifier.isStatic(field.getModifiers())) continue;
            
            NeedsReload needsReload = null;
            ConfigBoolean configBoolean = null;
            ConfigInt configInt = null;
            ConfigFloat configFloat = null;
            ConfigEnum configEnum = null;
            ConfigStringList configStringList = null;
            ConfigString configString = null;
            ConfigLoadable configLoadable = null;
            
            for(Annotation an : field.getAnnotations()) {
                if(an instanceof NeedsReload) {
                    needsReload = (NeedsReload) an;
                } else if(an instanceof ConfigInt) {
                    configInt = (ConfigInt) an;
                } else if(an instanceof ConfigFloat) {
                    configFloat = (ConfigFloat) an;
                } else if(an instanceof ConfigBoolean) {
                    configBoolean = (ConfigBoolean) an;
                } else if(an instanceof ConfigEnum) {
                    configEnum = (ConfigEnum) an;
                } else if(an instanceof ConfigStringList) {
                    configStringList = (ConfigStringList) an;
                }  else if(an instanceof ConfigString) {
                    configString = (ConfigString) an;
                }  else if(an instanceof ConfigLoadable) {
                    configLoadable = (ConfigLoadable) an;
                }
            }
            
            if(configBoolean == null && configInt == null && configFloat == null && configEnum == null && configLoadable == null && configStringList == null && configString == null) continue;
            
            Object currentValue = null;
            Object newValue = null;
            try {
                currentValue = field.get(null);
            } catch (Exception e) {
                logger.error("Failed to get value of field " + field.getName());
                e.printStackTrace();
                continue;
            }
            
            if(configBoolean != null) {
                newValue = config.getBoolean(field.getName(), configBoolean.cat(), configBoolean.def(), configBoolean.com());
            } else if(configInt != null) {
                newValue = config.getInt(field.getName(), configInt.cat(), configInt.def(), configInt.min(), configInt.max(), configInt.com()); 
            } else if(configFloat != null) {
                newValue = config.getFloat(field.getName(), configFloat.cat(), configFloat.def(), configFloat.min(), configFloat.max(), configFloat.com());
            } else if(configEnum != null || configLoadable != null) {
                String annDef = configEnum != null ? configEnum.def() : configLoadable.def();
                String annCat = configEnum != null ? configEnum.cat() : configLoadable.cat();
                String annCom = configEnum != null ? configEnum.com() : configLoadable.com();
                String fieldName = field.getName();
                
                boolean lowerCase = annDef.codePoints().anyMatch(cp -> Character.isLowerCase(cp));
                
                ILoadableClass loadableAnn = ((ILoadableClass)field.getType().getAnnotation(ILoadableClass.class));
                String suffix = loadableAnn == null ? "" : loadableAnn.suffix();
                
                Class<? extends Enum> configClass = (Class<? extends Enum>) (configEnum != null ? field.getType() : loadableAnn.enumClass());
                Map<String, ? extends Enum> enumMap = EnumUtils.getEnumMap(configClass);
                String[] valuesStrUpper = (String[])enumMap.keySet().stream().toArray(String[]::new);
                String[] valuesStr = Arrays.stream(valuesStrUpper).map(s -> lowerCase ? s.toLowerCase() : s).toArray(String[]::new);
                
                // allow upgrading boolean to string list
                ConfigCategory cat = config.getCategory(annCat.toLowerCase());
                String propName = fieldName + suffix;
                Property oldProp = cat.get(propName);
                String oldVal = null;
                if(oldProp != null && oldProp.getType() != Type.STRING) {
                    oldVal = oldProp.getString();
                    cat.remove(propName);
                }
                
                String newValueStr = config.getString(propName, annCat,
                        lowerCase ? annDef.toLowerCase() : annDef.toUpperCase(), annCom, valuesStr);
                if(oldVal != null) {
                    newValueStr = oldVal;
                }
                if(!enumMap.containsKey(newValueStr.toUpperCase())) {
                    newValueStr = annDef.toUpperCase();
                    if(lowerCase) {
                        newValueStr = newValueStr.toLowerCase();
                    }
                }
                newValue = enumMap.get(newValueStr.toUpperCase());
                
                Property newProp = cat.get(propName);
                if(!newProp.getString().equals(newValueStr)) {
                    newProp.set(newValueStr);
                }
            } else if(configStringList != null) {
                newValue = config.getStringList(field.getName(), configStringList.cat(), configStringList.def(), configStringList.com());
            } else if(configString != null) {
                newValue = config.getString(field.getName(), configString.cat(), configString.def(), configString.com());
            }
            
            if(needsReload != null && !newValue.equals(currentValue)) {
                needReload = true;
            }
            
            callback.accept(field, newValue, config);
        }
        
        return needReload;
    }
    
    public void saveFields(Configuration config) {
        iterateOverConfigAnd(config, (field, newValue, conf) -> {
            try {
                Object fieldValue = field.get(null);
                if(!fieldValue.equals(newValue)) {
                    for(String catName : config.getCategoryNames()) {
                        ConfigCategory cat = config.getCategory(catName);
                        Property prop = cat.get(field.getName());
                        if(prop != null) {
                            try {
                                setProperty(prop, fieldValue);
                            } catch(Exception e) {
                                logger.error("Failed to save field " + field.getName());
                                e.printStackTrace();
                            }
                            return;
                        }   
                    }
                    logger.error("Couldn't find property named " + field.getName() + ", can't save new value");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        
        if(config.hasChanged()) {
            config.save();
        }
    }
    
    private static void setProperty(Property prop, Object newValue) {
        switch(prop.getType()) {
        case BOOLEAN:
            prop.set((Boolean)newValue);
            break;
        case DOUBLE:
            prop.set((Double)newValue);
            break;
        case INTEGER:
            prop.set((Integer)newValue);
            break;
        case STRING:
            prop.set((String)newValue);
            break;
        default:
            throw new UnsupportedOperationException();
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface NeedsReload {

    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ConfigBoolean {

        String cat();
        boolean def();
        String com() default "";

    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ConfigInt {

        String cat();
        int min();
        int max();
        int def();
        String com() default "";

    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ConfigFloat {

        String cat();
        float min();
        float max();
        float def();
        String com() default "";

    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ConfigString {

        String cat();
        String def();
        String com() default "";

    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ConfigStringList {

        String cat();
        String[] def();
        String com() default "";

    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ConfigEnum {

        String cat();
        String def();
        String com() default "";

    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ConfigLoadable {

        String cat();
        String def();
        String com() default "";

    }
    
    public static interface ILoadable {
        public void setValue(Object newValue, Field field, Configuration config);
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface ILoadableClass {

        Class<? extends Enum<?>> enumClass();
        String suffix() default "";

    }
    
    @FunctionalInterface
    private static interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}
