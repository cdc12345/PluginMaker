package org.cdc.generator.elements;

import net.mcreator.element.GeneratableElement;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.utils.Constants;
import org.cdc.generator.utils.ElementsUtils;

import java.beans.BeanProperty;
import java.util.List;

public class VariableImplementationModElement extends GeneratableElement {

    public String variableElementName;
    public String defaultValue;

    public List<VariableScope> scopes;

    public VariableImplementationModElement(ModElement element) {
        super(element);
    }

    public String getVariableName() {
        if (variableElementName == null) {
            return null;
        }
        return ElementsUtils.getVariableName(getModElement().getWorkspace(), variableElementName);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public static class VariableScope {
        private String name;
        private String init;
        private String get;
        private String set;
        private String read;
        private String write;

        public VariableScope(String name) {
            this.name = name;
        }

        @BeanProperty(enumerationValues = { Constants.VariableScopes.LOCAL, Constants.VariableScopes.GLOBAL_MAP,
                Constants.VariableScopes.GLOBAL_SESSION, Constants.VariableScopes.GLOBAL_WORLD,
                Constants.VariableScopes.PLAYER_LIFETIME,
                Constants.VariableScopes.PLAYER_PERSISTENT }, description = "The name of scope")
        public void setName(String name) {
            this.name = name;
        }

        public void setInit(String init) {
            this.init = init;
        }

        public void setGet(String get) {
            this.get = get;
        }

        public void setSet(String set) {
            this.set = set;
        }

        public void setRead(String read) {
            this.read = read;
        }

        public void setWrite(String write) {
            this.write = write;
        }

        public String getWrite() {
            return write;
        }

        public String getRead() {
            return read;
        }

        public String getSet() {
            return set;
        }

        public String getGet() {
            return get;
        }

        public String getInit() {
            return init;
        }

        public String getName() {
            return name;
        }
    }
}
