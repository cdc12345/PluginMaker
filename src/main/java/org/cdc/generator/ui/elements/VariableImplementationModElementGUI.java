package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.VariableImplementationModElement;
import org.cdc.generator.init.ModElementTypes;
import org.cdc.generator.ui.preferences.PluginMakerPreference;
import org.cdc.generator.utils.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class VariableImplementationModElementGUI extends ModElementGUI<VariableImplementationModElement> {

    private final VComboBox<String> generator = new VComboBox<>();
    private final VComboBox<String> variableElementName = new VComboBox<>();
    private final VTextField defaultValue = new VTextField();

    public VariableImplementationModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);

        if (editingMode) {
            generator.setEnabled(false);
            variableElementName.setEnabled(false);
        }

        this.initGUI();
        this.finalizeGUI();
    }

    @Override protected void initGUI() {
        JPanel configuration = new JPanel(new GridLayout(3, 2));
        configuration.setBorder(BorderFactory.createTitledBorder("Config"));
        configuration.setOpaque(false);

        generator.setEditable(true);
        generator.setPreferredSize(Utils.tryToGetTextFieldSize());
        generator.setValidator(() -> {
            if (generator.getSelectedItem() != null && !generator.getSelectedItem().isBlank()) {
                return ValidationResult.PASSED;
            }
            return new ValidationResult(ValidationResult.Type.ERROR, "can not be empty");
        });
        for (String supportedGenerator : Utils.getAllSupportedGenerators()) {
            generator.addItem(supportedGenerator);
        }
        generator.setSelectedItem(PluginMakerPreference.INSTANCE.preferGenerator.get());
        configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariableimpl/generator"),
                L10N.label("elementgui.common.generator")));
        configuration.add(generator);

        variableElementName.setEditable(false);
        variableElementName.setValidator(() -> {
            if (variableElementName.getSelectedItem() != null && !variableElementName.getSelectedItem().isBlank()) {
                return ValidationResult.PASSED;
            }
            return new ValidationResult(ValidationResult.Type.ERROR, "can not be empty");
        });
        configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariableimpl/variableelementname"),
                L10N.label("elementgui.pluginvariableimpl.variable_element_name")));
        configuration.add(variableElementName);

        defaultValue.setText("null");
        defaultValue.setPreferredSize(Utils.tryToGetTextFieldSize());
        configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariableimpl/defaultvalue"),
                L10N.label("elementgui.pluginvariableimpl.default_value")));
        configuration.add(defaultValue);
    }

    @Override protected void openInEditingMode(VariableImplementationModElement generatableElement) {
        this.generator.setSelectedItem(generatableElement.getGeneratorName());
        this.variableElementName.setSelectedItem(generatableElement.variableElementName);
        this.defaultValue.setText(generatableElement.defaultValue);
    }

    @Override public VariableImplementationModElement getElementFromGUI() {
        VariableImplementationModElement element = new VariableImplementationModElement(modElement);
        element.generator = generator.getSelectedItem();
        element.variableElementName = variableElementName.getSelectedItem();
        element.defaultValue = defaultValue.getText();
        return element;
    }

    @Override public @Nullable URI contextURL() throws URISyntaxException {
        return null;
    }

    @Override public void reloadDataLists() {
        ArrayList<String> stringArrayList = new ArrayList<>();
        for (ModElement element : mcreator.getWorkspaceInfo()
                .getElementsOfType(ModElementTypes.VARIABLE.getRegistryName())) {
            stringArrayList.add(element.getName());
        }
        ComboBoxUtil.updateComboBoxContents(variableElementName, stringArrayList);
    }
}
