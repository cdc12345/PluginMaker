package org.cdc.generator.ui.elements;

import net.mcreator.generator.Generator;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.SearchableComboBox;
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
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.elements.MappingsModElement;
import org.cdc.generator.init.ModElementTypes;
import org.cdc.generator.utils.GeneratorUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class MappingsModElementGUI extends ModElementGUI<MappingsModElement> {

	private final String[] columns = new String[] { "Name", "Mapping" };

	private final VComboBox<String> generator = new VComboBox<>();
	private final VComboBox<String> datalistName = new VComboBox<>();
	private final SearchableComboBox<String> defaultMapping = new SearchableComboBox<>();
	private final VComboBox<String> mcreatorMapTemplate = new VComboBox<>();

	public List<MappingsModElement.MappingEntry> mappingEntries;

	// the 0 is the last search index
	private final ArrayList<Integer> lastSearchResult;

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
		JPanel configuration = new JPanel(new GridLayout(4, 2));
		configuration.setBorder(BorderFactory.createTitledBorder("Config"));
		configuration.setOpaque(false);

		generator.setEditable(true);
		generator.setValidator(() -> {
			if (generator.getSelectedItem() != null && !generator.getSelectedItem().isBlank()) {
				return ValidationResult.PASSED;
			}
			return new ValidationResult(ValidationResult.Type.ERROR, "can not be empty");
		});
		for (String supportedGenerator : GeneratorUtils.getAllSupportedGenerators()) {
			generator.addItem(supportedGenerator);
		}
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

		JLabel importM1 = new JLabel(UIRES.get("18px.add"));
		importM1.setToolTipText("import from current generator");
		defaultMapping.setEditable(true);
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginmappings/datalistname"),
				L10N.label("elementgui.pluginmappings.default_mapping")));
		configuration.add(PanelUtils.centerAndEastElement(defaultMapping, importM1));
		importM1.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				var datalist = mcreator.getWorkspace().getModElementByName(datalistName.getSelectedItem());
				String mappingName;
				if (datalist != null) {
					mappingName = GeneratorUtils.getDataListName(datalist);
					var memoryMapping = Generator.GENERATOR_CACHE.get(generator.getSelectedItem()).getMappingLoader()
							.getMapping(mappingName);
					if (memoryMapping != null) {
						if (memoryMapping.containsKey("_default")) {
							defaultMapping.setSelectedItem(memoryMapping.get("_default"));
						}
						for (Map.Entry<?, ?> entry : memoryMapping.entrySet()) {
							if (entry.getValue() instanceof List<?> list) {
								for (Object o : list) {
									defaultMapping.addItem(o.toString());
								}
							} else {
								defaultMapping.addItem(entry.getValue().toString());
							}
						}
						SwingUtilities.invokeLater(() -> {
							defaultMapping.repaint();
							defaultMapping.revalidate();
						});
					}
				}
			}
		});

		JLabel importM2 = new JLabel(UIRES.get("18px.add"));
		importM2.setToolTipText("import from current generator");
		mcreatorMapTemplate.setEditable(true);
		configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginmappings/mcreatormaptemplate"),
				L10N.label("elementgui.pluginmappings.mcreator_map_template")));
		configuration.add(PanelUtils.centerAndEastElement(mcreatorMapTemplate, importM2));
		importM2.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				var datalist = mcreator.getWorkspace().getModElementByName(datalistName.getSelectedItem());
				String mappingName;
				if (datalist != null) {
					mappingName = GeneratorUtils.getDataListName(datalist);
					var memoryMapping = Generator.GENERATOR_CACHE.get(generator.getSelectedItem()).getMappingLoader()
							.getMapping(mappingName);
					if (memoryMapping != null) {
						if (memoryMapping.containsKey("_mcreator_map_template")) {
							mcreatorMapTemplate.setSelectedItem(memoryMapping.get("_mcreator_map_template"));
						}
					}
				}
			}
		});

		JPanel mapping = new JPanel(new BorderLayout());
		JToolBar bar = new JToolBar();
		bar.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
		bar.setFloatable(false);
		bar.setOpaque(false);
		bar.setOpaque(false);

		JPanel buttons = new JPanel(new GridLayout(1, 2));
		buttons.setOpaque(false);
		VTextField searchbar = new VTextField();
		searchbar.setCustomDefaultMessage("click to search");
		searchbar.setValidator(() -> {
			if (lastSearchResult.size() == 1) {
				return new ValidationResult(ValidationResult.Type.ERROR, "No results");
			}
			return ValidationResult.PASSED;
		});
		JButton upSearch = new JButton(UIRES.get("18px.up"));
		upSearch.setOpaque(false);
		JButton downSearch = new JButton(UIRES.get("18px.down"));
		downSearch.setOpaque(false);
		buttons.add(upSearch);
		buttons.add(downSearch);

		var impfile = new JButton(UIRES.get("impfile"));
		impfile.setToolTipText("Import from element or memory");
		impfile.setOpaque(false);

		JTable jTable = new JTable(new AbstractTableModel() {
			@Override public int getRowCount() {
				return mappingEntries.size();
			}

			@Override public int getColumnCount() {
				return 2;
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
				return columnIndex != 0;
			}

			@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				super.setValueAt(aValue, rowIndex, columnIndex);
			}
		});
		jTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int rowIndex, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus,
						rowIndex, column);
				if (value != null)
					label.setToolTipText(value.toString());
				return label;
			}
		});
		jTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value1, boolean isSelected, int rowIndex,
					int column) {
				var row = mappingEntries.get(rowIndex);
				if (columns[column].equals("Mapping")) {
					JTextArea jTextArea = new JTextArea();
					for (String entry : row.getMappingContent()) {
						jTextArea.append(entry);
					}
					int op = JOptionPane.showConfirmDialog(mcreator, jTextArea, "Edit Mapping (one line one item)",
							JOptionPane.YES_NO_OPTION);
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
								GeneratorUtils.getDataListName(mcreator.getWorkspace(), datalistName.getSelectedItem()),
								row));
					}
					return null;
				}
				return super.getTableCellEditorComponent(jTable, value1, isSelected, rowIndex, column);
			}
		});
		jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable.setFillsViewportHeight(true);
		jTable.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(jTable);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setOpaque(false);

		impfile.addActionListener(e -> {
			var datalist = mcreator.getWorkspace().getModElementByName(datalistName.getSelectedItem());
			//
			impfile.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			//
			mappingEntries.clear();
			if (datalist != null) {
				String dataListName = GeneratorUtils.getDataListName(datalist);
				var memory = Generator.GENERATOR_CACHE.get(generator.getSelectedItem()).getMappingLoader()
						.getMapping(dataListName);
				var set = new HashSet<String>();
				if (memory != null) {
					for (Map.Entry<?, ?> entry : memory.entrySet()) {
						var key = entry.getKey().toString();
						// exclude
						if (!GeneratorUtils.MAPPING_INNER_KEY.matcher(key).matches()) {
							set.add(key);
							if (entry.getValue() instanceof List<?> list) {
								var ar = new ArrayList<String>();
								for (Object o : list) {
									ar.add(o.toString());
								}
								mappingEntries.add(new MappingsModElement.MappingEntry(key, ar).setEdited(false));
							} else {
								mappingEntries.add(new MappingsModElement.MappingEntry(key,
										new ArrayList<>(List.of(entry.getValue().toString()))).setEdited(false));
							}
						}
					}
				}
				// add custom
				for (DataListModElement.DataListEntry entry : ((DataListModElement) Objects.requireNonNull(
						datalist.getGeneratableElement())).entries) {
					if (!set.contains(entry.getName()))
						mappingEntries.add(new MappingsModElement.MappingEntry(entry.getName(), new ArrayList<>()));
				}
			}

			impfile.setCursor(Cursor.getDefaultCursor());
			SwingUtilities.invokeLater(() -> {
				jTable.repaint();
				jTable.revalidate();
			});
		});
		searchbar.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) {
				doSearch(searchbar);
			}

			@Override public void removeUpdate(DocumentEvent e) {
				doSearch(searchbar);
			}

			@Override public void changedUpdate(DocumentEvent e) {
				doSearch(searchbar);
			}
		});
		searchbar.registerKeyboardAction(a -> {
			downSearch.doClick();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
		downSearch.addActionListener(a -> {
			var index = lastSearchResult.getFirst() + 1;
			if (index >= lastSearchResult.size()) {
				index = 1;
			}
			jTable.changeSelection(lastSearchResult.get(index), 0, false, false);
			lastSearchResult.set(0, index);
			downSearch.setToolTipText(index + "/" + (lastSearchResult.size() - 1));
		});
		upSearch.addActionListener(a -> {
			var index = lastSearchResult.getFirst() - 1;
			if (index < 1) {
				index = lastSearchResult.size() - 1;
			}
			jTable.changeSelection(lastSearchResult.get(index), 0, false, false);
			lastSearchResult.set(0, index);
			upSearch.setToolTipText(index + "/" + (lastSearchResult.size() - 1));
		});

		bar.add(impfile);
		bar.add(PanelUtils.centerAndEastElement(searchbar, buttons));

		mapping.add("Center", scrollPane);
		mapping.add("North", bar);

		addPage("edit",
				PanelUtils.totalCenterInPanel(PanelUtils.northAndCenterElement(configuration, mapping))).validate(
				generator).validate(datalistName);
	}

	@Override protected void openInEditingMode(MappingsModElement generatableElement) {
		datalistName.setSelectedItem(generatableElement.datalistName);
		generator.setSelectedItem(generatableElement.generatorName);
		defaultMapping.setSelectedItem(generatableElement.defaultMapping);
		mcreatorMapTemplate.setSelectedItem(generatableElement.mcreatorMapTemplate);
		mappingEntries = new ArrayList<>(generatableElement.mappingsContent);
	}

	@Override public MappingsModElement getElementFromGUI() {
		var element = new MappingsModElement(modElement);
		element.datalistName = datalistName.getSelectedItem();
		element.generatorName = generator.getSelectedItem();
		element.defaultMapping = defaultMapping.getSelectedItem();
		element.mcreatorMapTemplate = mcreatorMapTemplate.getSelectedItem();
		element.mappingsContent = mappingEntries.stream().map(a -> {
			try {
				return a.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}).toList();
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

	private void doSearch(VTextField searchbar) {
		lastSearchResult.clear();
		// cache
		lastSearchResult.add(0);
		for (int i = 0; i < mappingEntries.size(); i++) {
			var entry = mappingEntries.get(i);
			if (Stream.of(entry.getName(), entry.getMappingContent().toString())
					.anyMatch(a -> a != null && a.contains(searchbar.getText()))) {
				lastSearchResult.add(i);
			}
		}
		searchbar.getValidationStatus();
	}
}
