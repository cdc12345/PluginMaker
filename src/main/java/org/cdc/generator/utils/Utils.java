package org.cdc.generator.utils;

import net.mcreator.generator.Generator;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.DataListModElement;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Utils {

	public static boolean isNotPluginGenerator(Generator generator) {
		return !generator.getGeneratorConfiguration().getRaw().containsKey("is_plugin_maker");
	}

	public static Set<String> getAllSupportedGenerators() {
		return Generator.GENERATOR_CACHE.keySet();
	}

	public static List<String> getMappingResult(String generator, String datalist, String name) {
		var memory = Generator.GENERATOR_CACHE.get(generator).getMappingLoader().getMapping(datalist);
		if (memory != null) {
			if (memory.containsKey(name)) {
				var oe = memory.get(name);
				return convertYamlMappingToList(oe);
			}
		}
		return null;
	}

	public static List<String> convertYamlMappingToList(Object oe) {
		if (oe instanceof String str) {
			return new ArrayList<>(List.of(str));
		} else if (oe instanceof List<?> list) {
			return new ArrayList<>(list.stream().map(Object::toString).toList());
		} else {
			return new ArrayList<>(List.of(oe.toString()));
		}
	}

	public static String getDataListName(Workspace workspace, String name) {
		var datalist = workspace.getModElementByName(name);
		if (datalist == null) {
			return null;
		}
		return getDataListName(datalist);
	}

	public static String getDataListName(ModElement modElement) {
		if (modElement.getGeneratableElement() instanceof DataListModElement dataListModElement) {
			return dataListModElement.datalistName;
		}
		return modElement.getRegistryName();
	}

	public static JPanel initSearchComponent(ArrayList<Integer> lastSearchResult, Consumer<Integer> showSearch,
			Consumer<String> doSearch) {
		VTextField searchbar = new VTextField();
		searchbar.setOpaque(false);
		searchbar.setBorder(BorderFactory.createEmptyBorder());
		JPanel buttons = new JPanel(new FlowLayout());
		buttons.setOpaque(false);
		searchbar.setCustomDefaultMessage("enter to search");
		searchbar.setValidator(() -> {
			if (lastSearchResult.size() == 1) {
				return new ValidationResult(ValidationResult.Type.ERROR, "No results");
			}
			return ValidationResult.PASSED;
		});
		JCheckBox ignoreCase = new JCheckBox("Ignore case");
		ignoreCase.setSelected(Rules.SearchRules.isIgnoreCase());
		ignoreCase.addActionListener(e -> {
			Rules.SearchRules.setIgnoreCase(ignoreCase.isSelected());
			doSearch.accept(Rules.SearchRules.applyIgnoreCaseRule(searchbar.getText()));
		});
		JButton upSearch = new JButton(UIRES.get("18px.up"));
		upSearch.setToolTipText("0/0");
		upSearch.setOpaque(false);
		JButton downSearch = new JButton(UIRES.get("18px.down"));
		downSearch.setToolTipText("0/0");
		downSearch.setOpaque(false);
		buttons.add(ignoreCase);
		buttons.add(upSearch);
		buttons.add(downSearch);
		searchbar.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) {
				doSearch.accept(Rules.SearchRules.applyIgnoreCaseRule(searchbar.getText()));
				searchbar.getValidationStatus();
			}

			@Override public void removeUpdate(DocumentEvent e) {
				doSearch.accept(Rules.SearchRules.applyIgnoreCaseRule(searchbar.getText()));
				searchbar.getValidationStatus();
			}

			@Override public void changedUpdate(DocumentEvent e) {
				doSearch.accept(Rules.SearchRules.applyIgnoreCaseRule(searchbar.getText()));
				searchbar.getValidationStatus();
			}
		});
		searchbar.registerKeyboardAction(a -> {
			downSearch.doClick();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
		downSearch.addActionListener(a -> {
			var index = lastSearchResult.getFirst() + 1;
			if (index >= lastSearchResult.size() && lastSearchResult.size() > 1) {
				index = 1;
			}
			showSearch.accept(lastSearchResult.get(index));
			lastSearchResult.set(0, index);
			downSearch.setToolTipText(index + "/" + (lastSearchResult.size() - 1));
		});
		upSearch.addActionListener(a -> {
			var index = lastSearchResult.getFirst() - 1;
			if (index < 1 && lastSearchResult.size() > 1) {
				index = lastSearchResult.size() - 1;
			}
			showSearch.accept(lastSearchResult.get(index));
			lastSearchResult.set(0, index);
			upSearch.setToolTipText(index + "/" + (lastSearchResult.size() - 1));
		});
		var panel = PanelUtils.centerAndEastElement(searchbar, buttons);
		panel.setOpaque(true);
		return panel;
	}
}
