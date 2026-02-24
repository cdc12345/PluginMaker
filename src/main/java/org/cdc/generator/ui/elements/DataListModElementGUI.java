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
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.DataListModElement;
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
import java.util.regex.Pattern;

public class DataListModElementGUI extends ModElementGUI<DataListModElement> {

    private static final Pattern nameMatcher = Pattern.compile("[a-zA-Z_1-9]+");
    private final String[] columns = new String[]{"Name", "Readable name", "Type", "Texture", "Others"};

    private final JCheckBox generateDataList = L10N.checkbox("elementgui.common.enable");

    public List<DataListModElement.DataListEntry> entries;

    public DataListModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);

        if (DataListLoader.getCache().containsKey(modElement.getRegistryName()) && !editingMode) {
            this.entries = new ArrayList<>() {
                {
                    for (DataListEntry dataListEntry : DataListLoader.loadDataList(modElement.getRegistryName())) {
                        var dataListEntry1 = new DataListModElement.DataListEntry(dataListEntry.getName(), dataListEntry.getReadableName(), dataListEntry.getType(), dataListEntry.getTexture());
                        var ma = new HashMap<String, String>();
                        if (dataListEntry.getOther() instanceof Map<?, ?> map) {
                            map.forEach((key, value) -> {
                                ma.put(key.toString(), value.toString());
                            });
                        }
                        dataListEntry1.setOthers(ma);
                        dataListEntry1.setBuiltIn(true);
                        this.add(dataListEntry1);
                    }
                }
            };
            generateDataList.setSelected(false);
        } else {
            generateDataList.setSelected(true);
            this.entries = new ArrayList<>();
        }


        this.initGUI();
        this.finalizeGUI();
    }

    @Override
    protected void initGUI() {
        JPanel generateConfig = new JPanel(new GridLayout(1, 2));
        generateConfig.setBorder(BorderFactory.createTitledBorder("Config"));
        generateConfig.setOpaque(false);

        generateConfig.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugindatalist/generate"),
                L10N.label("elementgui.plugindatalist.generate_datalists")));
        generateDataList.setOpaque(false);
        generateConfig.add(generateDataList);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("List"));
        listPanel.setOpaque(false);

        JTable jTable = new JTable(new AbstractTableModel() {

            @Override
            public int getRowCount() {
                return entries.size();
            }

            @Override
            public int getColumnCount() {
                return 5;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var row = entries.get(rowIndex);
                var columns = new String[]{row.getName(), row.getReadableName(), row.getType(), row.getTexture(), row.getOther().toString()};
                return columns[columnIndex];
            }

            @Override
            public String getColumnName(int column) {
                return columns[column];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                var row = entries.get(rowIndex);
                switch (getColumnName(columnIndex)) {
                    case "Name" -> {
                        if (nameMatcher.matcher(aValue.toString()).matches() && !row.isBuiltIn()) {
                            row.setName(aValue.toString());
                        } else {
                            JOptionPane.showMessageDialog(listPanel, "It doesn't match the rule!", "Warning", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                    case "Readable name" -> row.setReadableName(aValue.toString());
                    case "Type" -> row.setType(aValue.toString());
                    case "Texture" -> row.setTexture(aValue.toString());
                }
                row.setBuiltIn(false);
            }
        });
        jTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, rowIndex, column);
                if (value != null)
                    label.setToolTipText(value.toString());
                return label;
            }
        });
        jTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value1, boolean isSelected, int rowIndex, int column) {
                var row = entries.get(rowIndex);
                if (columns[column].equals("Others")) {
                    JTextArea jTextArea = new JTextArea();
                    for (Map.Entry<String, String> other : row.getOthers()) {
                        jTextArea.append(other.getKey() + "=" + other.getValue());
                    }
                    int op = JOptionPane.showConfirmDialog(mcreator, jTextArea, "Edit others(Format: properties)", JOptionPane.YES_NO_OPTION);
                    if (op == JOptionPane.YES_OPTION) {
                        String str = jTextArea.getText();
                        var prop = new Properties();
                        try {
                            prop.load(new StringReader(str));
                            var cacheMap = new HashMap<String, String>();
                            prop.forEach((key, value) -> {
                                cacheMap.put(key.toString(), value.toString());
                            });
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
        jTable.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(jTable);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
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

        addrow.addActionListener(e -> {
            entries.add(new DataListModElement.DataListEntry("name", "", "", ""));
            SwingUtilities.invokeLater(()->{
                jTable.repaint();
                jTable.revalidate();
            });
        });
        remrow.addActionListener(e -> {
            var rowIndex = jTable.getSelectedRow();
            entries.remove(rowIndex);
            SwingUtilities.invokeLater(()->{
                jTable.repaint();
                jTable.revalidate();
            });
        });

        listPanel.add("North", bar);
        listPanel.add("Center", scrollPane);


        addPage("datalist", PanelUtils.totalCenterInPanel(PanelUtils.northAndCenterElement(generateConfig, listPanel))).lazyValidate(() -> {
            Set<String> names = new HashSet<>();
            for (DataListModElement.DataListEntry entry : entries) {
                if (!names.contains(entry.getName())) {
                    names.add(entry.getName());
                } else {
                    jTable.setBorder(BorderFactory.createLineBorder(Color.RED));
                    return new AggregatedValidationResult.FAIL("Duplicative name in datalist");
                }


            }
            jTable.setBorder(BorderFactory.createEmptyBorder());
            return new AggregatedValidationResult.PASS();
        });
    }

    @Override
    protected void openInEditingMode(DataListModElement generatableElement) {
        this.entries = generatableElement.entries;
        this.generateDataList.setSelected(generatableElement.generateDataList);
    }

    @Override
    public DataListModElement getElementFromGUI() {
        DataListModElement dataListModElement = new DataListModElement(modElement);
        dataListModElement.generateDataList = generateDataList.isSelected();
        dataListModElement.entries = entries;
        return dataListModElement;
    }

    @Override
    public @Nullable URI contextURL() throws URISyntaxException {
        return null;
    }
}
