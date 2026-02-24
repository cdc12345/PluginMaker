package org.cdc.generator.elements;

import net.mcreator.element.GeneratableElement;
import net.mcreator.minecraft.RegistryNameFixer;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.utils.GeneratorUtils;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("unused")
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

    public String getGeneratorName() {
        return generatorName;
    }

    public String getDatalistName() {
        if (datalistName == null) {
            return null;
        }
        return RegistryNameFixer.fromCamelCase(datalistName);
    }

    public String getDefaultMapping() {
        if (defaultMapping != null && defaultMapping.isBlank()){
            return null;
        }
        return defaultMapping;
    }

    public String getMcreatorMapTemplate() {
        if (mcreatorMapTemplate != null && mcreatorMapTemplate.isBlank()){
            return null;
        }
        return mcreatorMapTemplate;
    }

    public static class MappingEntry implements Cloneable{
        private String name;
        private ArrayList<String> mappingContent;

        public MappingEntry(String name, ArrayList<String> mappingContent) {
            this.name = name;
            this.mappingContent = mappingContent;
        }

        public String getName() {
            return name;
        }

        public ArrayList<String> getMappingContent() {
            if (mappingContent == null) mappingContent = new ArrayList<>();
            return mappingContent;
        }

        public boolean isEdited(MappingsModElement mappingsModElement) {
            var list = GeneratorUtils.getMappingResult(mappingsModElement.getGeneratorName(),
                    mappingsModElement.getDatalistName(), name);
            if (list != null){
                return !list.equals(this.getMappingContent());
            }
            return true;
        }

        @Override public MappingEntry clone() throws CloneNotSupportedException {
            MappingEntry mappingEntry = (MappingEntry) super.clone();
            mappingEntry.name = name;
            mappingEntry.mappingContent = new ArrayList<>(mappingContent);
            return mappingEntry;
        }
    }
}
