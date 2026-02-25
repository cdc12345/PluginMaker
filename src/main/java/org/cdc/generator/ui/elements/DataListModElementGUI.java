package org.cdc.generator.ui.elements;

import net.mcreator.minecraft.DataListEntry;
import net.mcreator.minecraft.DataListLoader;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.AggregatedValidationResult;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.utils.Rules;
import org.cdc.generator.utils.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class DataListModElementGUI extends ModElementGUI<DataListModElement> {

	private final String[] columns = new String[] { "Name", "Readable name", "Type", "Texture", "Description",
			"Others" };

	private final JCheckBox generateDataList = L10N.checkbox("elementgui.common.enable");
	private final VTextField datalistName = new VTextField();

	public List<DataListModElement.DataListEntry> entries;

	// the 0 is the last search index
	private ArrayList<Integer> lastSearchResult;

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
				L10N.label("elementgui.plugindatalist.datalistname")));

		datalistName.setValidator(Rules.getDataListValidator(datalistName));
		datalistName.setCustomDefaultMessage("Enter to load");
		datalistName.setText(modElement.getRegistryName());
		generateConfig.add(datalistName);

		generateConfig.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugindatalist/generate"),
				L10N.label("elementgui.plugindatalist.generate_datalists")));
		generateDataList.setSelected(true);
		generateDataList.setOpaque(false);
		generateConfig.add(generateDataList);

		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.setBorder(BorderFactory.createTitledBorder("Edit"));
		listPanel.setOpaque(false);

		JTable jTable = new JTable(new DataListTableModel());
		jTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int rowIndex, int column) {
				var row = entries.get(rowIndex);
				JLabel label = (JLabel) super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus,
						rowIndex, column);
				if (value != null)
					label.setToolTipText(value.toString());
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
		jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

		VTextField searchbar = new VTextField();
		bar.add(Utils.initSearchComponent(searchbar, lastSearchResult, a -> jTable.changeSelection(a, 0, false, false),
				this::doSearch));

		addrow.addActionListener(e -> {
			entries.add(new DataListModElement.DataListEntry("name", "", "", "", ""));
			SwingUtilities.invokeLater(() -> {
				jTable.repaint();
				jTable.revalidate();
			});
		});
		remrow.addActionListener(e -> {
			var rowIndex = jTable.getSelectedRow();
			entries.remove(rowIndex);
			SwingUtilities.invokeLater(() -> {
				jTable.repaint();
				jTable.revalidate();
			});
		});
		datalistName.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					reloadDataLists();
					SwingUtilities.invokeLater(() -> {
						jTable.repaint();
						jTable.revalidate();
					});
				}
			}
		});

		listPanel.add("North", bar);
		listPanel.add("Center", scrollPane);

		addPage("datalist", PanelUtils.totalCenterInPanel(
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
	}

	private void doSearch(VTextField searchbar) {
		lastSearchResult.clear();
		// cache
		lastSearchResult.add(0);
		for (int i = 0; i < entries.size(); i++) {
			var entry = entries.get(i);
			if (Stream.of(entry.getName(), entry.getReadableName(), entry.getDescription(), entry.getTexture(),
							entry.getType(), entry.getOther().toString())
					.anyMatch(a -> a != null && a.contains(searchbar.getText()))) {
				lastSearchResult.add(i);
			}
		}
		searchbar.getValidationStatus();
	}

	@Override protected void openInEditingMode(DataListModElement generatableElement) {
		this.entries = new ArrayList<>(generatableElement.entries);
		this.generateDataList.setSelected(generatableElement.generateDataList);
		this.datalistName.setText(generatableElement.datalistName);
	}

	@Override public DataListModElement getElementFromGUI() {
		DataListModElement dataListModElement = new DataListModElement(modElement);
		dataListModElement.datalistName = datalistName.getText();
		dataListModElement.generateDataList = generateDataList.isSelected();
		dataListModElement.entries = entries.stream().map(a -> {
			try {
				return a.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}).toList();
		return dataListModElement;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return null;
	}

	@Override public void reloadDataLists() {
		if (DataListLoader.getCache().containsKey(datalistName.getText()) && entries.isEmpty()) {
			for (DataListEntry dataListEntry : DataListLoader.loadDataList(datalistName.getText())) {
				var dataListEntry1 = DataListModElement.DataListEntry.copyValueOf(dataListEntry);
				dataListEntry1.setBuiltIn(true);
				entries.add(dataListEntry1);
			}
		}
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
			return true;
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
