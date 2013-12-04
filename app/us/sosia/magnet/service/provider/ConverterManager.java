package us.sosia.magnet.service.provider;

import us.sosia.magnet.service.provider.impl.TorCacheProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public class ConverterManager {


    private static Priority priority ;
    private static ConverterManager instance = null;
    private static Map<String,ConverterProvider> providers = new HashMap<String,ConverterProvider>();

    private ConverterManager() {
        providers.put("TorCache",new TorCacheProvider());
    }

    public static ConverterManager manager(){
        if (instance == null){
            instance = new ConverterManager();
        }
        return instance;
    }

    public static Priority getPriority() {
        return priority;
    }

    public static void setPriority(Priority priority) {
        ConverterManager.priority = priority;
    }

    public ConverterProvider provider(){
        return providers.get("TorCache");
    }

    public ConverterProvider provider(final String name){
        return null;
    }

    public ConverterProvider provider(int priority){
        return null;
    }

    public void register(String name,ConverterProvider provider){
         providers.put(name,provider);
    }

}
