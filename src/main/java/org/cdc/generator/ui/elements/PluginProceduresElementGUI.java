package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.JColor;
import net.mcreator.ui.component.JStringListField;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.PluginProcedureModElement;
import org.cdc.generator.utils.Rules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;

public class PluginProceduresElementGUI extends AbstractConfigurationTableModElementGUI<PluginProcedureModElement> {

    protected final VTextField name = new VTextField();
    protected final JCheckBox inputsInline = createDefaultCheckBox();
    protected final VTextField previousStatement = new VTextField();
    protected final VTextField nextStatement = new VTextField();
    protected final JColor color;
    protected final JStringListField outputs;
    protected final VComboBox<String> toolbox_id = new VComboBox<>();
    protected final VTextField group = new VTextField();
    protected final JStringListField warnings;
    protected final JStringListField required_apis;

    public PluginProceduresElementGUI(MCreator mcreator, @Nonnull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode, new String[] {});
        this.color = new JColor(mcreator, false, false);
        this.outputs = new JStringListField(mcreator, a -> Rules.getFileNameValidator(a::getText));
        this.warnings = new JStringListField(mcreator, null);
        this.required_apis = new JStringListField(mcreator, a -> Rules.getFileNameValidator(a::getText));
    }

    @Override protected void initGUI() {

    }

    @Override protected void openInEditingMode(PluginProcedureModElement generatableElement) {

    }

    @Override public PluginProcedureModElement getElementFromGUI() {
        return null;
    }

    @Override @Nullable public URI contextURL() throws URISyntaxException {
        return null;
    }
}
