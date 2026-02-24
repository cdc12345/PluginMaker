package org.cdc.generator.elements;

import net.mcreator.element.GeneratableElement;
import net.mcreator.workspace.elements.ModElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataListModElement extends GeneratableElement {

    public boolean generateDataList;

    public List<DataListEntry> entries;

    public DataListModElement(ModElement element) {
        super(element);
    }

    public static class DataListEntry {
        private String name;
        // map readable_name
        private String readableName;
        private String type;
        private String texture;
        // map other
        private Map<String,String> others;

        private boolean builtIn;

        public DataListEntry(String name, @Nullable String readable_name, @Nullable String type, @Nullable String texture) {
            this.name = name;
            this.readableName = readable_name;
            this.type = type;
            this.texture = texture;
            this.builtIn = false;
            this.others = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public String getReadableName() {
            if (this.readableName != null && this.readableName.isBlank()){
                return null;
            }
            return readableName;
        }

        public String getTexture() {
            if (this.texture != null && this.texture.isBlank()){
                return null;
            }
            return texture;
        }

        public String getType() {
            if (this.type != null && this.type.isBlank()){
                return null;
            }
            return type;
        }

        public Set<Map.Entry<String, String>> getOthers() {
            if (others == null){
                others = new HashMap<>();
            }
            return others.entrySet();
        }

        public Map<String,String> getOther(){
            if (this.others == null) {
                this.others = new HashMap<>();
            }
            return others;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setReadableName(String readable_name) {
            this.readableName = readable_name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setTexture(String texture) {
            this.texture = texture;
        }

        public void setOthers(Map<String, String> others) {
            if (others == null) {
                this.others = new HashMap<>();
                return;
            }
            this.others = others;
        }

        public boolean isBuiltIn() {
            return builtIn;
        }

        public void setBuiltIn(boolean builtIn) {
            this.builtIn = builtIn;
        }
    }
}
