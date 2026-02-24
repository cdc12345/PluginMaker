package org.cdc.generator.utils;

import net.mcreator.generator.Generator;
import net.mcreator.ui.MCreator;

import java.util.Set;

public class GeneratorUtils {
    public static boolean isNotPluginGenerator(Generator generator) {
        return !generator.getGeneratorConfiguration().getRaw().containsKey("is_plugin_maker");
    }
    
    public static Set<String> getAllSupportedGenerators(MCreator mCreator){
        return Generator.GENERATOR_CACHE.keySet();
    }
}
