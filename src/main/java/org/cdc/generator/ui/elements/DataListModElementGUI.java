package org.cdc.generator.ui.elements;

import net.mcreator.minecraft.DataListEntry;
import net.mcreator.minecraft.DataListLoader;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.AggregatedValidationResult;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.workspace.WorkspacePanel;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.ui.ResourcePanelIcons;
import org.cdc.generator.utils.Rules;
import org.cdc.generator.utils.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class DataListModElementGUI extends ModElementGUI<DataListModElement> implements ISearchable {

	private final String[] columns = new String[] { "Name", "Readable name", "Type", "Texture", "Description",
			"Others" };

	private final VComboBox<String> datalistName = new VComboBox<>();
	private final JCheckBox generateDataList = L10N.checkbox("elementgui.common.enable");

	public List<DataListModElement.DataListEntry> entries;

	// the 0 is the last search index
	private final ArrayList<Integer> lastSearchResult;
	private JTable jTable;

	public DataListModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);

		this.entries = new ArrayList<>();
		this.lastSearchResult = new ArrayList<>(List.of(0));

		if (editingMode) {
			datalistName.setEnabled(false);
		}

		this.initGUI();
		this.finalizeGUI();
	}

	@Override protected void initGUI() {
		JPanel generateConfig = new JPanel(new GridLayout(2, 2));
		generateConfig.setBorder(BorderFactory.createTitledBorder("Config"));
		generateConfig.setOpaque(false);

		generateConfig.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugindatalist/datalistname"),
				L10N.label("elementgui.common.name")));

		datalistName.setValidator(Rules.getComboBoxValidator(datalistName));
		datalistName.setEditable(true);
		datalistName.setSelectedItem(modElement.getRegistryName());
		var list = DataListLoader.getCache().keySet().stream().sorted().toList();
		ComboBoxUtil.updateComboBoxContents(datalistName, list);
		generateConfig.add(datalistName);

		generateConfig.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugindatalist/generate"),
				L10N.label("elementgui.plugindatalist.generate_datalists")));
		generateDataList.setSelected(true);
		generateDataList.setOpaque(false);
		generateConfig.add(generateDataList);

		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.setBorder(BorderFactory.createTitledBorder("Edit"));
		listPanel.setOpaque(false);

		jTable = new JTable(new DataListTableModel());
		jTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int rowIndex, int column) {
				var row = entries.get(rowIndex);
				JLabel label = (JLabel) super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus,
						rowIndex, column);
				if (value != null)
					label.setToolTipText(value + ", index=" + rowIndex);
				if (row.isBuiltIn())
					label.setToolTipText("BuiltIn: " + label.getToolTipText());
				return label;
			}
		});
		jTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value1, boolean isSelected, int rowIndex,
					int column) {
				var row = entries.get(rowIndex);
				if (columns[column].equals("Others")) {
					JTextArea jTextArea = new JTextArea();
					for (Map.Entry<String, String> other : row.getOthers()) {
						jTextArea.append(other.getKey() + "=" + other.getValue());
					}
					int op = JOptionPane.showConfirmDialog(mcreator, jTextArea, "Edit others (Format: properties)",
							JOptionPane.YES_NO_OPTION);
					if (op == JOptionPane.YES_OPTION) {
						String str = jTextArea.getText();
						var prop = new Properties();
						try {
							prop.load(new StringReader(str));
							var cacheMap = new HashMap<String, String>();
							prop.forEach((key, value) -> cacheMap.put(key.toString(), value.toString()));
							row.setOthers(cacheMap);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					return null;
				}
				return super.getTableCellEditorComponent(jTable, value1, isSelected, rowIndex, column);
			}
		});
		jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTable.setFillsViewportHeight(true);
		jTable.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(jTable);
		scrollPane.setOpaque(false);

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

		addrow.addActionListener(e -> {
			entries.add(entries.isEmpty() ?
					new DataListModElement.DataListEntry("name", "", "", "", "") :
					DataListModElement.DataListEntry.copyCommonValueOf(entries.getLast()));
			if (!isEditingMode()) {
				JOptionPane.showMessageDialog(mcreator, "If you edit datalist name, you will lose your work", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
			refreshTable();
		});
		remrow.addActionListener(e -> {
			var rowIndex = jTable.getSelectedRows();
			for (int index : rowIndex) {
				entries.remove(index);
			}
			refreshTable();
		});
		datalistName.addItemListener(e -> {
			reloadDataLists();
			refreshTable();
		});

		listPanel.add("North", bar);
		listPanel.add("Center", scrollPane);

		addPage("Configuration", PanelUtils.totalCenterInPanel(
				PanelUtils.northAndCenterElement(generateConfig, listPanel))).lazyValidate(() -> {
			Set<String> names = new HashSet<>();
			for (int i = 0; i < entries.size(); i++) {
				var entry = entries.get(i);
				if (!names.contains(entry.getName())) {
					names.add(entry.getName());
				} else {
					jTable.changeSelection(i, 0, false, false);
					return new AggregatedValidationResult.FAIL("Duplicative name in datalist");
				}

			}
			return new AggregatedValidationResult.PASS();
		}).validate(datalistName);

		ResourcePanelIcons resourcePanelIcons = new ResourcePanelIcons((WorkspacePanel) mcreator.getWorkspacePanel(),this);
		resourcePanelIcons.reloadElements();
		addPage("Icons",resourcePanelIcons);
	}

	public void doSearch(String text) {
		lastSearchResult.clear();
		// cache
		lastSearchResult.add(0);
		for (int i = 0; i < entries.size(); i++) {
			var entry = entries.get(i);
			if (Stream.of(entry.getName(), entry.getReadableName(), entry.getDescription(), entry.getTexture(),
							entry.getType(), entry.getOther().toString())
					.anyMatch(a -> a != null && Rules.SearchRules.applyIgnoreCaseRule(a).contains(text))) {
				lastSearchResult.add(i);
			}
		}
	}

	@Override protected void openInEditingMode(DataListModElement generatableElement) {
		this.entries = new ArrayList<>(generatableElement.entries);
		this.generateDataList.setSelected(generatableElement.generateDataList);
		this.datalistName.setSelectedItem(generatableElement.datalistName);
	}

	@Override public DataListModElement getElementFromGUI() {
		DataListModElement dataListModElement = new DataListModElement(modElement);
		dataListModElement.datalistName = datalistName.getSelectedItem();
		dataListModElement.generateDataList = generateDataList.isSelected();
		dataListModElement.entries = entries.stream().map(DataListModElement.DataListEntry::clone).toList();
		return dataListModElement;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return null;
	}

	@Override public void reloadDataLists() {
		if (DataListLoader.getCache().containsKey(datalistName.getSelectedItem()) && !isEditingMode()) {
			entries.clear();
			for (DataListEntry dataListEntry : DataListLoader.loadDataList(datalistName.getSelectedItem())) {
				var dataListEntry1 = DataListModElement.DataListEntry.copyValueOf(dataListEntry);
				dataListEntry1.setBuiltIn(true);
				entries.add(dataListEntry1);
			}
		}
	}

	public void refreshTable() {
		SwingUtilities.invokeLater(() -> {
			jTable.repaint();
			jTable.revalidate();
		});
	}

	@Override public void showSearch(int index) {
		jTable.changeSelection(index, 0, false, false);
	}

	private class DataListTableModel extends AbstractTableModel {

		@Override public int getRowCount() {
			return entries.size();
		}

		@Override public int getColumnCount() {
			return columns.length;
		}

		@Override public Object getValueAt(int rowIndex, int columnIndex) {
			var row = entries.get(rowIndex);
			var columns = new String[] { row.getName(), row.getReadableName(), row.getType(), row.getTexture(),
					row.getDescription(), row.getOther().toString() };
			return columns[columnIndex];
		}

		@Override public String getColumnName(int column) {
			return columns[column];
		}

		@Override public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override public boolean isCellEditable(int rowIndex, int columnIndex) {
			var row = entries.get(rowIndex);
			return columnIndex == 0 || row.getName().charAt(0) != '_';
		}

		@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			var row = entries.get(rowIndex);
			if (aValue == null) {
				return;
			}
			switch (getColumnName(columnIndex)) {
			case "Name" -> {
				if (Rules.DATALIST_ENTRY_NAME.matcher(aValue.toString()).matches()) {
					row.setName(aValue.toString());
				} else {
					JOptionPane.showMessageDialog(mcreator, "It doesn't match the rule!", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}
			}
			case "Readable name" -> row.setReadableName(aValue.toString());
			case "Type" -> row.setType(aValue.toString());
			case "Texture" -> row.setTexture(aValue.toString());
			case "Description" -> row.setDescription(aValue.toString());
			}
		}
	}
}
