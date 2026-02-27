package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.JStringListField;
import net.mcreator.ui.component.TranslatedComboBox;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.Validator;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.validation.validators.RegistryNameValidator;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.TriggerModElement;
import org.cdc.generator.utils.Rules;
import org.cdc.generator.utils.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TriggerModElementGUI extends ModElementGUI<TriggerModElement> implements ISearchable {

	private String[] columns = new String[] { "Name", "Type" };

	private VTextField triggerName;
	private JStringListField requiredApis;
	private JCheckBox cancelable;
	private JCheckBox hasResult;
	private TranslatedComboBox side;

	public List<TriggerModElement.Dependency> dependencies;

	// the 0 is the last search index
	private final ArrayList<Integer> lastSearchResult;
	private JTable jTable;

	public TriggerModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);

		this.triggerName = new VTextField();
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
		this.side = new TranslatedComboBox(
				// @formatter:off
				Map.entry("SERVER", "elementgui.plugintrigger.side.server"),
				Map.entry("CLIENT", "elementgui.plugintrigger.side.client"),
				Map.entry("BOTH", "elementgui.plugintrigger.side.both")
				// @formatter:on
		);
		this.cancelable = new JCheckBox();
		this.hasResult = new JCheckBox();
		this.dependencies = new ArrayList<>();
		this.lastSearchResult = new ArrayList<>();
		if (editingMode) {
			triggerName.setEnabled(false);
		}

		this.initGUI();
		this.finalizeGUI();
	}

	@Override protected void initGUI() {
		JPanel configuration = new JPanel(new GridLayout(5, 2));
		configuration.setOpaque(false);
		configuration.setBorder(BorderFactory.createTitledBorder("Configuration"));

		this.triggerName.setOpaque(false);
		this.triggerName.setPreferredSize(Utils.tryToGetTextFieldSize());
		this.triggerName.setText(modElement.getRegistryName());
		this.triggerName.setValidator(Rules.getTriggerNameValidator(this.triggerName));
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintrigger/name"),
				L10N.label("elementgui.common.name")));
		configuration.add(triggerName);

		this.hasResult.setOpaque(false);
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintrigger/has_result"),
				L10N.label("elementgui.plugintrigger.has_result")));
		configuration.add(hasResult);

		this.cancelable.setOpaque(false);
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintrigger/cancelable"),
				L10N.label("elementgui.plugintrigger.cancelable")));
		configuration.add(cancelable);

		this.side.setOpaque(false);
		this.side.setSelectedItem("BOTH");
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintrigger/side"),
				L10N.label("elementgui.plugintrigger.side")));
		configuration.add(side);

		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintrigger/required_apis"),
				L10N.label("elementgui.plugintrigger.required_apis")));
		configuration.add(requiredApis);

		JPanel edit = new JPanel(new BorderLayout());
		edit.setOpaque(false);
		edit.setBorder(BorderFactory.createTitledBorder("Parameters"));

		jTable = new JTable(new TriggerModElementGUITableModul());
		jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane jScrollPane = new JScrollPane(jTable);
		edit.add("Center", jScrollPane);

		JToolBar bar = new JToolBar();
		bar.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
		bar.setFloatable(false);
		bar.setOpaque(false);
		JButton addrow = new JButton(UIRES.get("16px.add"));
		addrow.setContentAreaFilled(false);
		addrow.setOpaque(false);
		ComponentUtils.deriveFont(addrow, 11);
		addrow.setBorder(BorderFactory.createEmptyBorder(1, 1, 0, 2));
		bar.add(addrow);
		JButton remrow = new JButton(UIRES.get("16px.delete"));
		remrow.setContentAreaFilled(false);
		remrow.setOpaque(false);
		ComponentUtils.deriveFont(remrow, 11);
		remrow.setBorder(BorderFactory.createEmptyBorder(1, 1, 0, 1));
		bar.add(remrow);
		bar.add(Utils.initSearchComponent(lastSearchResult, this));
		edit.add("North", bar);

		addrow.addActionListener(a -> {
			dependencies.add(new TriggerModElement.Dependency("name", "type"));
			refreshTable();
		});
		remrow.addActionListener(a -> {
			var rowIndex = jTable.getSelectedRows();
			for (int index : rowIndex) {
				dependencies.remove(index);
			}
			refreshTable();
		});

		addPage("trigger", PanelUtils.totalCenterInPanel(PanelUtils.northAndCenterElement(configuration, edit)));
	}

	public void doSearch(String text) {
		lastSearchResult.clear();
		// cache
		lastSearchResult.add(0);
		for (int i = 0; i < dependencies.size(); i++) {
			var entry = dependencies.get(i);
			if (Stream.of(entry.getName(), entry.getType())
					.anyMatch(a -> a != null && Rules.SearchRules.applyIgnoreCaseRule(a).contains(text))) {
				lastSearchResult.add(i);
			}
		}
	}

	@Override public void refreshTable() {
		SwingUtilities.invokeLater(() -> {
			jTable.repaint();
			jTable.revalidate();
		});
	}

	@Override public void showSearch(int index) {
		jTable.changeSelection(index, 0, false, false);
	}

	@Override protected void openInEditingMode(TriggerModElement generatableElement) {
		this.triggerName.setText(generatableElement.getName());
		this.hasResult.setSelected(generatableElement.has_result);
		this.cancelable.setSelected(generatableElement.cancelable);
		this.side.setSelectedItem(generatableElement.side);
		this.requiredApis.setTextList(generatableElement.required_apis);
		this.dependencies = generatableElement.dependencies_provided;
	}

	@Override public TriggerModElement getElementFromGUI() {
		var trigger = new TriggerModElement(modElement);
		trigger.cancelable = this.cancelable.isSelected();
		trigger.has_result = this.hasResult.isSelected();
		trigger.side = this.side.getSelectedItem();
		trigger.required_apis = this.requiredApis.getTextList();
		trigger.name = this.triggerName.getText();
		trigger.dependencies_provided = this.dependencies.stream().map(TriggerModElement.Dependency::clone).toList();
		return trigger;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return null;
	}

	private class TriggerModElementGUITableModul extends AbstractTableModel {

		@Override public int getRowCount() {
			return dependencies.size();
		}

		@Override public int getColumnCount() {
			return columns.length;
		}

		@Override public String getColumnName(int column) {
			return columns[column];
		}

		@Override public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override public Object getValueAt(int rowIndex, int columnIndex) {
			var row = dependencies.get(rowIndex);
			var columss = new String[] { row.getName(), row.getType() };
			return columss[columnIndex];
		}

		@Override public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			var row = dependencies.get(rowIndex);
			switch (columns[columnIndex]) {
			case "Name" -> row.setName(aValue.toString());
			case "Type" -> row.setType(aValue.toString());
			}
		}
	}
}
