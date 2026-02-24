package org.cdc.generator.ui.elements;

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
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
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

    private JCheckBox generateDataList = L10N.checkbox("elementgui.common.enable");

    public List<DataListModElement.DataListEntry> entries;

    public DataListModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);

        this.entries = new ArrayList<>();

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
        generateDataList.setSelected(true);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("List"));
        listPanel.setOpaque(false);

        JTable jTable = new JTable(new AbstractTableModel() {
            String[] columns = new String[]{"Name", "Readable name", "Type", "Texture", "Others"};

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

                if (columnIndex == columns.length - 1) {
                    var label = new JLabel(row.getOther().toString());
                    label.setOpaque(false);
                    return label;
                }

                var columns = new String[]{row.getName(), row.getReadableName(), row.getType(), row.getTexture()};
                return columns[columnIndex];
            }

            @Override
            public String getColumnName(int column) {
                return columns[column];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == columns.length - 1) {
                    return JLabel.class;
                }
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
                        if (nameMatcher.matcher(aValue.toString()).matches()) {
                            row.setName(aValue.toString());
                        } else {
                            JOptionPane.showMessageDialog(listPanel, "It doesn't match the rule, redo!", "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    case "Readable name" -> row.setReadableName(aValue.toString());
                    case "Type" -> row.setType(aValue.toString());
                    case "Texture" -> row.setTexture(aValue.toString());
                }
            }
        });
        jTable.setDefaultRenderer(JLabel.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int column) {
                var value2 = ((JLabel) value).getText();
                JLabel label = (JLabel) super.getTableCellRendererComponent(jTable, value2, isSelected, hasFocus, rowIndex, column);
                label.setToolTipText(value2);
                return label;
            }
        });
        jTable.setDefaultEditor(JLabel.class, new TableCellEditor() {

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value1, boolean isSelected, int rowIndex, int column) {
                var row = entries.get(rowIndex);
                JTextArea jTextArea = new JTextArea();
                for (Map.Entry<String, String> other : row.getOthers()) {
                    jTextArea.append(other.getKey() + "=" + other.getValue());
                }
                int op = JOptionPane.showConfirmDialog(mcreator, jTextArea, "Edit others", JOptionPane.YES_NO_OPTION);
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

            @Override
            public Object getCellEditorValue() {
                return null;
            }

            @Override
            public boolean isCellEditable(EventObject anEvent) {
                return true;
            }

            @Override
            public boolean shouldSelectCell(EventObject anEvent) {
                return true;
            }

            @Override
            public boolean stopCellEditing() {
                return true;
            }

            @Override
            public void cancelCellEditing() {

            }

            @Override
            public void addCellEditorListener(CellEditorListener l) {

            }

            @Override
            public void removeCellEditorListener(CellEditorListener l) {

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
            SwingUtilities.invokeLater(() -> {
                jTable.invalidate();
                jTable.revalidate();
            });
        });
        remrow.addActionListener(e -> {
            var rowIndex = jTable.getSelectedRow();
            entries.remove(rowIndex);
            SwingUtilities.invokeLater(() -> {
                jTable.invalidate();
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
