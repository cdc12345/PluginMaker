package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.JColor;
import net.mcreator.ui.component.JStringListField;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.Validator;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.validation.validators.RegistryNameValidator;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.VariableModElement;
import org.cdc.generator.utils.Rules;
import org.cdc.generator.utils.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

public class VariableModElementGUI extends ModElementGUI<VariableModElement> {

	private final VTextField name = new VTextField();

	private final VTextField blocklyVariableType = new VTextField();
	private final JCheckBox ignoredByCoverage = L10N.checkbox("elementgui.common.enable");
	private final JCheckBox nullable = L10N.checkbox("elementgui.common.enable");
	private final JStringListField requiredApis;
	private final JColor color;
	private final VComboBox<String> builtInColor;

	public VariableModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);

		this.requiredApis = new JStringListField(mcreator, vTextField -> new Validator() {
			private final Validator parent = new RegistryNameValidator(vTextField,
					L10N.t("dialog.workspace.settings.workspace_modid")).setMaxLength(32);

			@Override public ValidationResult validate() {
				if (!Rules.VALID_MODID.matcher(vTextField.getText()).matches())
					return new ValidationResult(ValidationResult.Type.ERROR,
							L10N.t("dialog.workspace.settings.workspace_modid_invalid"));
				return parent.validate();
			}
		});
		this.color = new JColor(mcreator, true, false);
		this.builtInColor = new VComboBox<>(
				new String[] { Rules.NONE, "%{BKY_MATH_HUE}", "%{BKY_LOGIC_HUE}", "%{BKY_TEXTS_HUE}" });
		if (editingMode) {
			name.setEnabled(false);
		}

		this.initGUI();
		this.finalizeGUI();
	}

	@Override protected void initGUI() {
		JPanel configuration = new JPanel(new GridLayout(7, 2, 5, 5));
		configuration.setOpaque(false);
		configuration.setBorder(BorderFactory.createTitledBorder("Configuration"));

		name.setOpaque(false);
		name.setText(modElement.getRegistryName());
		name.setValidator(Rules.getTextfieldValidator(name));
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariable/name"),
				L10N.label("elementgui.common.name")));
		configuration.add(name);

		blocklyVariableType.setOpaque(false);
		blocklyVariableType.setText(modElement.getName());
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariable/blocklyvariabletype"),
				L10N.label("elementgui.pluginvariable.blockly_variable_type")));
		configuration.add(blocklyVariableType);

		ignoredByCoverage.setOpaque(false);
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariable/ignoredbycoverage"),
				L10N.label("elementgui.pluginvariable.ignored_by_coverage")));
		configuration.add(ignoredByCoverage);

		nullable.setOpaque(false);
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariable/nullable"),
				L10N.label("elementgui.pluginvariable.nullable")));
		configuration.add(nullable);

		requiredApis.setOpaque(false);
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariable/nullable"),
				L10N.label("elementgui.common.required_apis")));
		configuration.add(requiredApis);

		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariable/color"),
				L10N.label("elementgui.pluginvariable.color")));
		configuration.add(color);

		builtInColor.setOpaque(false);
		builtInColor.setEditable(true);
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariable/color"),
				L10N.label("elementgui.pluginvariable.builtincolor")));
		configuration.add(builtInColor);

		addPage(PanelUtils.totalCenterInPanel(configuration)).validate(name);
	}

	@Override protected void openInEditingMode(VariableModElement generatableElement) {
		this.name.setText(generatableElement.name);
		this.blocklyVariableType.setText(generatableElement.blocklyVariableType);
		this.nullable.setSelected(generatableElement.nullable);
		this.ignoredByCoverage.setSelected(generatableElement.ignoredByCoverage);
		this.requiredApis.setTextList(generatableElement.required_apis);
		this.color.setColor(generatableElement.color);
		this.builtInColor.setSelectedItem(Utils.nullToNoneOrNoneToNull(generatableElement.strColor));
	}

	@Override public VariableModElement getElementFromGUI() {
		VariableModElement variableModElement = new VariableModElement(modElement);
		variableModElement.name = name.getText();
		variableModElement.blocklyVariableType = blocklyVariableType.getText();
		variableModElement.nullable = nullable.isSelected();
		variableModElement.ignoredByCoverage = ignoredByCoverage.isSelected();
		variableModElement.required_apis = requiredApis.getTextList();
		variableModElement.color = color.getColor();
		variableModElement.strColor = Utils.nullToNoneOrNoneToNull(builtInColor.getSelectedItem());
		return variableModElement;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return null;
	}
}
