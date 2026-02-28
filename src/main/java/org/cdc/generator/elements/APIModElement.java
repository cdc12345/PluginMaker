package org.cdc.generator.elements;

import net.mcreator.element.GeneratableElement;
import net.mcreator.workspace.elements.ModElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO
public class APIModElement extends GeneratableElement {
	public String ApiName;
	
	public Map<String,Configuration> generatorToConfiguration;

	public APIModElement(ModElement element) {
		super(element);
	}

	public Set<Map.Entry<String, Configuration>> getGeneratorToConfigurationEntries() {
		return generatorToConfiguration.entrySet();
	}
	
	public static class Configuration {
		private boolean requiredWhenEnable;

		private String gradle;
		private List<String> updateFiles;
		private String versionRange;

		public Configuration(){}

		public List<String> getUpdateFiles() {
			return updateFiles;
		}

		public String getGradle() {
			return gradle;
		}

		public String getVersionRange() {
			return versionRange;
		}

		public boolean isRequiredWhenEnable() {
			return requiredWhenEnable;
		}
	}
}
