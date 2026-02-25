package org.cdc.generator.elements;

import com.google.j2objc.annotations.UsedByReflection;
import net.mcreator.element.GeneratableElement;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.utils.GeneratorUtils;

import java.util.ArrayList;
import java.util.List;

public class MappingsModElement extends GeneratableElement {

	public String generatorName;
	public String datalistName;
	// map _default
	public String defaultMapping;
	// map _mcreator_map_template
	public String mcreatorMapTemplate;
	public List<MappingEntry> mappingsContent;

	public MappingsModElement(ModElement element) {
		super(element);
	}

	@UsedByReflection
	public String getGeneratorName() {
		return generatorName;
	}

	@UsedByReflection
	public String getDatalistName() {
		if (datalistName == null) {
			return null;
		}
		return GeneratorUtils.getDataListName(getModElement().getWorkspace(),datalistName);
	}

	@UsedByReflection public String getDefaultMapping() {
		if (defaultMapping != null && defaultMapping.isBlank()) {
			return null;
		}
		return defaultMapping;
	}

	@UsedByReflection public String getMcreatorMapTemplate() {
		if (mcreatorMapTemplate != null && mcreatorMapTemplate.isBlank()) {
			return null;
		}
		return mcreatorMapTemplate;
	}

	public static class MappingEntry implements Cloneable {
		private String name;
		private ArrayList<String> mappingContent;

		private boolean edited;

		public MappingEntry(String name, ArrayList<String> mappingContent) {
			this.name = name;
			this.mappingContent = mappingContent;
			this.edited = true;
		}

		public String getName() {
			return name;
		}

		public ArrayList<String> getMappingContent() {
			if (mappingContent == null)
				mappingContent = new ArrayList<>();
			return mappingContent;
		}

		@UsedByReflection public String getFirst() {
			return mappingContent.getFirst();
		}

		public MappingEntry setEdited(boolean edited) {
			this.edited = edited;
			return this;
		}

		@UsedByReflection
		public static boolean isEdited(String generatorName, String datalistName, MappingEntry entry) {
			var list = GeneratorUtils.getMappingResult(generatorName, datalistName, entry.name);
			if (list != null) {
				return !list.equals(entry.getMappingContent());
			}
			return true;
		}

		public boolean isEdited() {
			return edited;
		}

		@Override public MappingEntry clone() throws CloneNotSupportedException {
			MappingEntry mappingEntry = (MappingEntry) super.clone();
			mappingEntry.name = name;
			mappingEntry.mappingContent = new ArrayList<>(mappingContent);
			return mappingEntry;
		}
	}
}
