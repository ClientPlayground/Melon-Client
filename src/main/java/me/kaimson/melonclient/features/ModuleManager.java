package me.kaimson.melonclient.features;

import com.google.common.collect.*;
import org.reflections.scanners.*;
import org.reflections.*;
import me.kaimson.melonclient.features.modules.utils.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.*;

public class ModuleManager
{
    public static final ModuleManager INSTANCE;
    public LinkedHashSet<Module> modules;
    
    public ModuleManager() {
        this.modules = Sets.newLinkedHashSet();
    }
    
    public void init() {
        final Set<Class<? extends Module>> modules = new Reflections("me.kaimson.melonclient.features.modules").getSubTypesOf(Module.class);
        modules.forEach(module -> {
            if (module == IModuleRenderer.class || module == DefaultModuleRenderer.class) {
                return;
            }
            else {
                try {
                    module.newInstance();
                }
                catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return;
            }
        });
        this.modules = this.modules.stream().sorted((o1, o2) -> {
            if (o1.isRender() && !o2.isRender()) {
                return -1;
            }
            else if (!o1.isRender() && o2.isRender()) {
                return 1;
            }
            else {
                return o1.getKey().compareTo(o2.getKey());
            }
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    static {
        INSTANCE = new ModuleManager();
    }
}
