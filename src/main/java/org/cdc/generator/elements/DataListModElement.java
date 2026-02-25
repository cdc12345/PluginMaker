package org.cdc.generator.elements;

import net.mcreator.element.GeneratableElement;
import net.mcreator.workspace.elements.ModElement;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataListModElement extends GeneratableElement {

    public boolean generateDataList;
    public String datalistName;

    public List<DataListEntry> entries;

    public DataListModElement(ModElement element) {
        super(element);
    }

    public String getDatalistName() {
        return datalistName;
    }

    public static class DataListEntry implements Cloneable{
        public static DataListEntry copyValueOf(net.mcreator.minecraft.DataListEntry dataListEntry){
            var dataListEntry1 = new DataListModElement.DataListEntry(dataListEntry.getName(),
                    dataListEntry.getReadableName(), dataListEntry.getType(), dataListEntry.getTexture(),
                    dataListEntry.getDescription());
            var ma = new HashMap<String, String>();
            if (dataListEntry.getOther() instanceof Map<?, ?> map) {
                map.forEach((key, value) -> ma.put(key.toString(), value.toString()));
            }
            dataListEntry1.setOthers(ma);
            return dataListEntry1;
        }

        private String name;
        // map readable_name
        private String readableName;
        private String type;
        private String texture;
        private String description;
        // map other
        private Map<String,String> others;

        private boolean builtIn;

        public DataListEntry(String name, @Nullable String readable_name, @Nullable String type, @Nullable String texture,@Nullable String description) {
            this.name = name;
            this.readableName = readable_name;
            this.type = type;
            this.texture = texture;
            this.description = description;
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

        public String getDescription() {
            return description;
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

        public void setDescription(String description) {
            this.description = description;
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

        @Override public DataListEntry clone() throws CloneNotSupportedException {
			DataListEntry dataListEntry = (DataListEntry) super.clone();
            dataListEntry.others = new HashMap<>(others);
            dataListEntry.builtIn = builtIn;
            return dataListEntry;
        }

        @Override public String toString() {
            return "DataListEntry{" + "name='" + name + '\'' + ", readableName='" + readableName + '\'' + ", type='"
                    + type + '\'' + ", texture='" + texture + '\'' + ", description='" + description + '\''
                    + ", others=" + others + ", builtIn=" + builtIn + '}';
        }
    }
}
