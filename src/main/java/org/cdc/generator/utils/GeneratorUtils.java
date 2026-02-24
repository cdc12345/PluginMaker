package org.cdc.generator.utils;

import net.mcreator.generator.Generator;
import net.mcreator.ui.MCreator;
import org.cdc.generator.PluginMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GeneratorUtils {
	public static boolean isNotPluginGenerator(Generator generator) {
		return !generator.getGeneratorConfiguration().getRaw().containsKey("is_plugin_maker");
	}

	public static Set<String> getAllSupportedGenerators(MCreator mCreator) {
		return Generator.GENERATOR_CACHE.keySet();
	}

	public static ArrayList<String> getMappingResult(String generator, String datalist, String name) {
		var memory = Generator.GENERATOR_CACHE.get(generator).getMappingLoader().getMapping(datalist);
		if (memory != null) {
			if (memory.containsKey(name)) {
				var oe = memory.get(name);
				PluginMain.LOG.info(oe);
				if (oe instanceof String str) {
					return new ArrayList<>(List.of(str));
				} else if (oe instanceof List<?> list) {
					return new ArrayList<>(list.stream().map(Object::toString).toList());
				} else {
					return new ArrayList<String>(List.of(oe.toString()));
				}
			}
		}
		return null;
	}
}
