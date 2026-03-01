package org.cdc.generator.ui.elements;

import com.google.common.io.Files;
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
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.workspace.WorkspacePanel;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.PluginMain;
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.ui.ResourcePanelIcons;
import org.cdc.generator.utils.DialogUtils;
import org.cdc.generator.utils.Rules;
import org.cdc.generator.utils.Utils;
import org.cdc.generator.utils.ZipUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DataListModElementGUI extends ModElementGUI<DataListModElement> implements ISearchable {

	private final String[] columns = new String[] { "Name", "Readable name", "Type", "Texture", "Description",
			"Others" };

	private final VComboBox<String> datalistName = new VComboBox<>();
	private final JCheckBox generateDataList = L10N.checkbox("elementgui.common.enable");
	private final VTextField dialogMessage = new VTextField();

	public List<DataListModElement.DataListEntry> entries;

	// the 0 is the last search index
	private final ArrayList<Integer> lastSearchResult;
	private JTable entriesTable;
	private ResourcePanelIcons resourcePanelIcons;
	private HashSet<String> types;

	private ThreadPoolExecutor searchThread = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(1));

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
		JPanel generateConfig = new JPanel(new GridLayout(3, 2));
		generateConfig.setBorder(BorderFactory.createTitledBorder("Config"));
		generateConfig.setOpaque(false);

		generateConfig.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugindatalist/datalistname"),
				L10N.label("elementgui.common.name")));

		datalistName.setValidator(Rules.getComboBoxValidator(datalistName));
		datalistName.setEditable(true);
		datalistName.setSelectedItem(modElement.getRegistryName());
		datalistName.setPreferredSize(Utils.tryToGetTextFieldSize());
		var list = DataListLoader.getCache().keySet().stream().sorted().toList();
		ComboBoxUtil.updateComboBoxContents(datalistName, list);
		generateConfig.add(datalistName);

		generateConfig.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugindatalist/generate"),
				L10N.label("elementgui.plugindatalist.generate_datalists")));
		generateDataList.setSelected(true);
		generateDataList.setOpaque(false);
		generateConfig.add(generateDataList);

		generateConfig.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugindatalist/dialogmessage"),
				L10N.label("elementgui.plugindatalist.dialog_message")));
		dialogMessage.setOpaque(false);
		generateConfig.add(dialogMessage);

		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.setBorder(BorderFactory.createTitledBorder("Edit"));
		listPanel.setOpaque(false);

		entriesTable = new JTable(new DataListTableModel());
		Utils.initTable(entriesTable, entries);
		entriesTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int rowIndex, int column) {
				var row = entries.get(rowIndex);
				JLabel label = (JLabel) super.getTableCellRendererComponent(entriesTable, value, isSelected, hasFocus,
						rowIndex, column);
				if (value != null)
					label.setToolTipText(value + ", index=" + rowIndex);
				if (row.isBuiltIn())
					label.setToolTipText("BuiltIn: " + label.getToolTipText());
				return label;
			}
		});
		var comboBox = new VComboBox<>();
		comboBox.setEditable(true);
		types = new HashSet<>();
		entriesTable.setDefaultEditor(String.class, new DefaultCellEditor(comboBox) {

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value1, boolean isSelected, int rowIndex,
					int column) {
				comboBox.removeAllItems();
				var row = entries.get(rowIndex);
				if (columns[column].equals("Others")) {
					JTextArea jTextArea = new JTextArea();
					int op = DialogUtils.showOptionPaneWithTextArea(jTextArea, mcreator,
							"Edit others (Format: properties)", row.getOthers());
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
				} else if ("Texture".equals(columns[column])) {
					for (File element : resourcePanelIcons.getAllElements()) {
						comboBox.addItem(Files.getNameWithoutExtension(element.getName()));
					}
					var file = Utils.tryToFindCorePlugin();
					PluginMain.LOG.info(file);
					if (file.isDirectory()) {
						var file1 = new File(file, "datalists/icons");
						for (File listFile : Objects.requireNonNullElse(file1.listFiles(), new File[0])) {
							comboBox.addItem(Files.getNameWithoutExtension(listFile.getName()));
						}
					} else {
						for (String s : ZipUtils.tryToGetTexturesFromZip(file)) {
							comboBox.addItem(s);
						}
					}
				} else if ("Type".equals(columns[column])) {
					for (String type : types) {
						comboBox.addItem(type);
					}
				}
				return super.getTableCellEditorComponent(entriesTable, value1, isSelected, rowIndex, column);
			}
		});

		JScrollPane scrollPane = new JScrollPane(entriesTable);
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
			entriesTable.editCellAt(-1, 0);
			Arrays.stream(entriesTable.getSelectedRows()).mapToObj(b -> entries.get(b)).forEach(c -> {
				entries.remove(c);
			});
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
					entriesTable.changeSelection(i, 0, false, false);
					return new AggregatedValidationResult.FAIL("Duplicative name in datalist");
				}

			}
			return new AggregatedValidationResult.PASS();
		}).validate(datalistName);

		resourcePanelIcons = new ResourcePanelIcons((WorkspacePanel) mcreator.getWorkspacePanel(), this);
		resourcePanelIcons.reloadElements();
		addPage("Icons", resourcePanelIcons);
	}

	@Override public void doSearch(Map.Entry<String, String> search) {
		lastSearchResult.clear();
		// cache
		lastSearchResult.add(0);
		for (int i = 0; i < entries.size(); i++) {
			var entry = entries.get(i);
			AtomicInteger atomicInteger = new AtomicInteger();
			if (Stream.of(entry.getName(), entry.getReadableName(), entry.getType(), entry.getTexture(),
							entry.getDescription(), entry.getOther().toString())
					.map(a -> Map.entry(columns[atomicInteger.getAndIncrement()],
							Rules.SearchRules.applyIgnoreCaseRule(a))).anyMatch(a -> {
						if (!search.getKey().isBlank()) {
							if (a.getKey().equalsIgnoreCase(search.getKey())) {
								return a.getValue().contains(search.getValue());
							}
							return false;
						}
						return a.getValue().contains(search.getValue());
					})) {
				lastSearchResult.add(i);
			}
		}
	}

	@Override protected void openInEditingMode(DataListModElement generatableElement) {
		this.entries = new ArrayList<>(generatableElement.entries);
		this.generateDataList.setSelected(generatableElement.generateDataList);
		this.datalistName.setSelectedItem(generatableElement.datalistName);
		this.dialogMessage.setText(generatableElement.dialogMessage);

		for (DataListModElement.DataListEntry entry : entries) {
			types.add(entry.getType());
		}
	}

	@Override public DataListModElement getElementFromGUI() {
		modElement.setRegistryName(datalistName.getSelectedItem());
		DataListModElement dataListModElement = new DataListModElement(modElement);
		dataListModElement.datalistName = datalistName.getSelectedItem();
		dataListModElement.generateDataList = generateDataList.isSelected();
		dataListModElement.entries = entries.stream().map(DataListModElement.DataListEntry::clone).toList();
		dataListModElement.dialogMessage = dialogMessage.getText();
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

	public HashSet<String> getTypes() {
		return types;
	}

	public void refreshTable() {
		SwingUtilities.invokeLater(() -> {
			entriesTable.repaint();
			entriesTable.revalidate();
		});
	}

	@Override public void showSearch(int index) {
		entriesTable.changeSelection(index, 0, false, false);
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
