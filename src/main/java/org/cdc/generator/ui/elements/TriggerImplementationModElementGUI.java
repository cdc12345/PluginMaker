package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.TriggerImplementationModElement;
import org.cdc.generator.elements.TriggerModElement;
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
import java.util.stream.Collectors;

public class TriggerImplementationModElementGUI extends ModElementGUI<TriggerImplementationModElement> {

	private final VComboBox<String> generator = new VComboBox<>();
	private final VComboBox<String> triggerElementName = new VComboBox<>();

	private final VTextField eventName = new VTextField();
	private final JTextArea methodBody = new JTextArea();

	public TriggerImplementationModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);

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
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintriggerimpl/generator"),
				L10N.label("elementgui.common.generator")));
		configuration.add(generator);

		triggerElementName.setEditable(false);
		triggerElementName.setValidator(() -> {
			if (triggerElementName.getSelectedItem() != null && !triggerElementName.getSelectedItem().isBlank()) {
				return ValidationResult.PASSED;
			}
			return new ValidationResult(ValidationResult.Type.ERROR, "can not be empty");
		});
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintriggerimpl/trigger_element_name"),
				L10N.label("elementgui.plugintriggerimpl.element_name")));
		configuration.add(triggerElementName);

		eventName.setOpaque(false);
		eventName.setValidator(() -> {
			if (eventName.getText() == null || eventName.getText().isEmpty()) {
				return new ValidationResult(ValidationResult.Type.ERROR, "Not empty");
			}
			return ValidationResult.PASSED;
		});
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintriggerimpl/event_name"),
				L10N.label("elementgui.plugintriggerimpl.event_name")));
		configuration.add(eventName);

		var toolbar = new JToolBar();
		JButton generate = new JButton(UIRES.get("18px.import"));
		generate.addActionListener(e -> {
			var str = """
					<#assign dependenciesCode>
					  <@procedureDependenciesCode dependencies, {
					        %map%
					  }/>
					</#assign>
					execute(event<#if dependenciesCode?has_content>,</#if>${dependenciesCode});
					""";
			str = str.replace("%map%", getTriggerModElement().dependencies_provided.stream()
					.map(a -> "\"" + a.getName() + "\":" + "\"" + a.getName() + "\"")
					.collect(Collectors.joining(",\n        ")));
			methodBody.setText(str);
		});
		toolbar.add(generate);
		var scrollpane = new JScrollPane(methodBody);
		var panel = PanelUtils.northAndCenterElement(toolbar, scrollpane);

		addPage(PanelUtils.northAndCenterElement(configuration, panel)).validate(generator).validate(triggerElementName)
				.validate(eventName);
	}

	@Override protected void openInEditingMode(TriggerImplementationModElement generatableElement) {
		this.generator.setSelectedItem(generatableElement.generatorName);
		this.triggerElementName.setSelectedItem(generatableElement.triggerElementName);
		this.eventName.setText(generatableElement.eventName);
		this.methodBody.setText(generatableElement.methodBody);
	}

	@Override public TriggerImplementationModElement getElementFromGUI() {
		var element = new TriggerImplementationModElement(modElement);
		element.triggerElementName = triggerElementName.getSelectedItem();
		element.generatorName = generator.getSelectedItem();
		element.eventName = eventName.getText();
		element.methodBody = methodBody.getText();
		return element;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return null;
	}

	public TriggerModElement getTriggerModElement() {
		var trigger = mcreator.getWorkspace().getModElementByName(triggerElementName.getSelectedItem());
		if (trigger.getGeneratableElement() instanceof TriggerModElement triggerModElement) {
			return triggerModElement;
		}
		return null;
	}

	@Override public void reloadDataLists() {
		ArrayList<String> stringArrayList = new ArrayList<>();
		for (ModElement element : mcreator.getWorkspaceInfo()
				.getElementsOfType(ModElementTypes.TRIGGER.getRegistryName())) {
			stringArrayList.add(element.getName());
		}
		ComboBoxUtil.updateComboBoxContents(triggerElementName, stringArrayList);
	}
}
