package org.cdc.generator.utils;

import net.mcreator.generator.Generator;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.PluginMain;
import org.cdc.generator.elements.DataListModElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class GeneratorUtils {

	public static final Pattern MAPPING_INNER_KEY = Pattern.compile("(_default|_mcreator_map_template)");

	public static boolean isNotPluginGenerator(Generator generator) {
		return !generator.getGeneratorConfiguration().getRaw().containsKey("is_plugin_maker");
	}

	public static Set<String> getAllSupportedGenerators() {
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
					return new ArrayList<>(List.of(oe.toString()));
				}
			}
		}
		return null;
	}

	public static String getDataListName(Workspace workspace,String name){
		var datalist = workspace.getModElementByName(name);
		if (datalist == null){
			return null;
		}
		return getDataListName(datalist);
	}

	public static String getDataListName(ModElement modElement){
		if (modElement.getGeneratableElement() instanceof DataListModElement dataListModElement){
			return dataListModElement.datalistName;
		}
		return modElement.getRegistryName();
	}
}
