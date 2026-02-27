package org.cdc.generator.ui.elements;

import net.mcreator.generator.Generator;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.elements.MappingsModElement;
import org.cdc.generator.init.ModElementTypes;
import org.cdc.generator.ui.preferences.PluginMakerPreference;
import org.cdc.generator.utils.Rules;
import org.cdc.generator.utils.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class MappingsModElementGUI extends ModElementGUI<MappingsModElement> implements ISearchable {

	private final String[] columns = new String[] { "Name", "Mapping" };

	private final VComboBox<String> generator = new VComboBox<>();
	private final VComboBox<String> datalistName = new VComboBox<>();

	public List<MappingsModElement.MappingEntry> mappingEntries;

	// the 0 is the last search index
	private final ArrayList<Integer> lastSearchResult;
	private JTable jTable;

	public MappingsModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);

		this.mappingEntries = new ArrayList<>();
		this.lastSearchResult = new ArrayList<>(List.of(0));

		if (editingMode) {
			generator.setEnabled(false);
			datalistName.setEnabled(false);
		}

		this.initGUI();
		this.finalizeGUI();
	}

	@Override protected void initGUI() {
		JPanel configuration = new JPanel(new GridLayout(2, 2));
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
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginmappings/generator"),
				L10N.label("elementgui.pluginmappings.generator")));
		configuration.add(generator);

		datalistName.setEditable(false);
		datalistName.setValidator(() -> {
			if (datalistName.getSelectedItem() != null && !datalistName.getSelectedItem().isBlank()) {
				return ValidationResult.PASSED;
			}
			return new ValidationResult(ValidationResult.Type.ERROR, "can not be empty");
		});
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginmappings/datalistname"),
				L10N.label("elementgui.pluginmappings.datalist_name")));
		configuration.add(datalistName);

		JPanel mapping = new JPanel(new BorderLayout());
		mapping.setOpaque(false);
		mapping.setBorder(BorderFactory.createTitledBorder("Edit"));
		JToolBar bar = new JToolBar();
		bar.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
		bar.setFloatable(false);
		bar.setOpaque(false);
		bar.setOpaque(false);

		var syncWithDatalist = new JButton(UIRES.get("impfile"));
		syncWithDatalist.setToolTipText("Import from element and memory");
		syncWithDatalist.setOpaque(false);

		jTable = new JTable(new MappingTableModel());
		jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable.setFillsViewportHeight(true);
		jTable.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(jTable);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setOpaque(false);

		jTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int rowIndex, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus,
						rowIndex, column);
				if (value != null)
					label.setToolTipText(value + ", index=" + rowIndex);
				return label;
			}
		});
		jTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value1, boolean isSelected, int rowIndex,
					int column) {
				var row = mappingEntries.get(rowIndex);
				if (columns[column].equals("Mapping")) {
					JToolBar placeholder = new JToolBar();
					placeholder.setFloatable(false);
					placeholder.setLayout(new GridLayout(2, 3));
					placeholder.setBorder(BorderFactory.createTitledBorder("Placeholders"));
					JTextArea jTextArea = new JTextArea();
					jTextArea.setOpaque(false);
					jTextArea.setBorder(BorderFactory.createTitledBorder("Lines"));

					Stream.of("@NAME", "@UPPERNAME", "@name", "@SnakeCaseName", "@registryname", "@REGISTRYNAME")
							.forEach(a -> {
								JButton appendName = new JButton(a);
								appendName.setContentAreaFilled(false);
								appendName.setOpaque(false);
								appendName.setHorizontalTextPosition(SwingConstants.LEFT);
								appendName.addActionListener(event -> {
									jTextArea.insert(a, jTextArea.getCaretPosition());
								});
								placeholder.add(appendName);
							});
					jTextArea.append(row.getMappingContent().getFirst());
					row.getMappingContent().stream().skip(1).forEach(a -> {
						jTextArea.append("\n");
						jTextArea.append(a);
					});
					int op = JOptionPane.showConfirmDialog(mcreator,
							PanelUtils.northAndCenterElement(placeholder, jTextArea),
							"Edit Mapping (one line one item)", JOptionPane.YES_NO_OPTION);
					if (op == JOptionPane.YES_OPTION) {
						String str = jTextArea.getText();
						BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
						try {
							var list = row.getMappingContent();
							list.clear();
							String line;
							while ((line = bufferedReader.readLine()) != null) {
								list.add(line);
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						row.setEdited(MappingsModElement.MappingEntry.isEdited(generator.getSelectedItem(),
								Utils.getDataListName(mcreator.getWorkspace(), datalistName.getSelectedItem()), row));
					}
					return null;
				}
				return super.getTableCellEditorComponent(jTable, value1, isSelected, rowIndex, column);
			}
		});
		syncWithDatalist.addActionListener(e -> {
			var datalist = mcreator.getWorkspace().getModElementByName(datalistName.getSelectedItem());
			//
			MappingsModElementGUI.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			//
			if (datalist != null) {
				String dataListName = Utils.getDataListName(datalist);
				var memory = Generator.GENERATOR_CACHE.get(generator.getSelectedItem()).getMappingLoader()
						.getMapping(dataListName);
				var cacheSet = new HashSet<MappingsModElement.MappingEntry>();
				var set = new HashSet<String>();
				mappingEntries.forEach(a -> {
					if (a.isEdited()) {
						cacheSet.add(a);
						set.add(a.getName());
					}
				});
				mappingEntries.clear();
				if (memory != null) {
					for (Map.Entry<?, ?> entry : memory.entrySet()) {
						var key = entry.getKey().toString();
						// exclude
						if (!set.contains(key)) {
							set.add(key);
							mappingEntries.add(new MappingsModElement.MappingEntry(key,
									Utils.convertYamlMappingToList(entry.getValue())));
						}
					}
				}
				// add custom
				for (DataListModElement.DataListEntry entry : ((DataListModElement) Objects.requireNonNull(
						datalist.getGeneratableElement())).entries) {
					if (set.isEmpty() || !set.contains(entry.getName()))
						mappingEntries.add(new MappingsModElement.MappingEntry(entry.getName(), new ArrayList<>()));
				}
				mappingEntries.addAll(cacheSet);
				JOptionPane.showMessageDialog(mcreator,
						"Total: " + mappingEntries.size() + ", Edited: " + cacheSet.size());
			}

			MappingsModElementGUI.this.setCursor(Cursor.getDefaultCursor());
			refreshTable();
		});

		bar.add(syncWithDatalist);
		bar.add(Utils.initSearchComponent(lastSearchResult, this));

		mapping.add("Center", scrollPane);
		mapping.add("North", bar);

		addPage("edit",
				PanelUtils.totalCenterInPanel(PanelUtils.northAndCenterElement(configuration, mapping))).validate(
				generator).validate(datalistName);
	}

	@Override protected void openInEditingMode(MappingsModElement generatableElement) {
		datalistName.setSelectedItem(generatableElement.datalistName);
		generator.setSelectedItem(generatableElement.generatorName);
		mappingEntries = new ArrayList<>(generatableElement.mappingsContent);
	}

	@Override public MappingsModElement getElementFromGUI() {
		var element = new MappingsModElement(modElement);
		element.datalistName = datalistName.getSelectedItem();
		element.generatorName = generator.getSelectedItem();
		element.mappingsContent = mappingEntries.stream().map(MappingsModElement.MappingEntry::clone).toList();
		return element;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return null;
	}

	@Override public void reloadDataLists() {
		ArrayList<String> stringArrayList = new ArrayList<>();
		for (ModElement element : mcreator.getWorkspaceInfo()
				.getElementsOfType(ModElementTypes.DATA_LIST.getRegistryName())) {
			stringArrayList.add(element.getName());
		}
		ComboBoxUtil.updateComboBoxContents(datalistName, stringArrayList);
	}

	public void doSearch(String text) {
		lastSearchResult.clear();
		// cache
		lastSearchResult.add(0);
		for (int i = 0; i < mappingEntries.size(); i++) {
			var entry = mappingEntries.get(i);
			if (Stream.of(entry.getName(), entry.getMappingContent().toString())
					.anyMatch(a -> a != null && Rules.SearchRules.applyIgnoreCaseRule(a).contains(text))) {
				lastSearchResult.add(i);
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

	private class MappingTableModel extends AbstractTableModel {
		@Override public int getRowCount() {
			return mappingEntries.size();
		}

		@Override public int getColumnCount() {
			return columns.length;
		}

		@Override public Object getValueAt(int rowIndex, int columnIndex) {
			var row = mappingEntries.get(rowIndex);
			var columns = new String[] { row.getName(), row.getMappingContent().toString() };
			return columns[columnIndex];
		}

		@Override public String getColumnName(int column) {
			return columns[column];
		}

		@Override public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			super.setValueAt(aValue, rowIndex, columnIndex);
		}
	}
}
