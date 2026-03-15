package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.JStringListField;
import net.mcreator.ui.component.TranslatedComboBox;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.Validator;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.validation.validators.RegistryNameValidator;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.workspace.elements.VariableTypeLoader;
import org.cdc.generator.elements.TriggerModElement;
import org.cdc.generator.utils.Rules;
import org.cdc.generator.utils.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TriggerModElementGUI extends AbstractConfigurationTableModElementGUI<TriggerModElement>
        implements ISearchable {

    private VTextField triggerName;
    private VTextField readableName;
    private JStringListField requiredApis;
    private JCheckBox cancelable;
    private JCheckBox hasResult;
    private TranslatedComboBox side;

    public List<TriggerModElement.Dependency> dependencies;

    // the 0 is the last search index
    private final ArrayList<Integer> lastSearchResult;

    public TriggerModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode, new String[] { "Name", "Type" });

        this.triggerName = new VTextField();
        this.readableName = new VTextField();
        this.requiredApis = new JStringListField(mcreator, vTextField -> Rules.getModidValidator(vTextField::getText));
        this.side = new TranslatedComboBox(
                // @formatter:off
				Map.entry("SERVER", "elementgui.plugintrigger.side.server"),
				Map.entry("CLIENT", "elementgui.plugintrigger.side.client"),
				Map.entry("BOTH", "elementgui.plugintrigger.side.both")
				// @formatter:on
        );
        this.cancelable = createDefaultCheckBox();
        this.hasResult = createDefaultCheckBox();
        this.dependencies = new ArrayList<>();
        this.lastSearchResult = new ArrayList<>();
        if (editingMode) {
            triggerName.setEnabled(false);
        }

        this.initGUI();
        this.finalizeGUI();
    }

    @Override protected void initGUI() {
        initConfiguration(new GridLayout(6, 2, 5, 5));

        this.triggerName.setText(modElement.getRegistryName());
        this.triggerName.setValidator(Rules.getFileNameValidator(this.triggerName::getText));
        addNameConfiguration(triggerName);

        this.readableName.setText(modElement.getName());
        addConfigurationWithHelpEntry("readable_name", readableName);

        addConfigurationWithHelpEntry("has_result", hasResult);

        addConfigurationWithHelpEntry("cancelable", cancelable);

        this.side.setSelectedItem("BOTH");
        this.side.setPreferredSize(Utils.tryToGetTextFieldSize());
        addConfigurationWithHelpEntry("side", side);

        configurationPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("plugintrigger/required_apis"),
                L10N.label("elementgui.common.required_apis")));
        configurationPanel.add(requiredApis);

        var typeComboBox = new VComboBox<String>();
        typeComboBox.setOpaque(false);
        typeComboBox.setEditable(true);

        initTable(new TriggerModElementGUITableModul());
        jTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                var label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setForeground(Theme.current().getForegroundColor());
                if (columns[column].equals("Type")) {
                    if (VariableTypeLoader.INSTANCE.doesVariableTypeExist(label.getText())) {
                        label.setForeground(VariableTypeLoader.INSTANCE.fromName(label.getText()).getBlocklyColor());
                    }
                }
                return label;
            }
        });
        jTable.setDefaultEditor(String.class, new DefaultCellEditor(typeComboBox) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
                    int columnIndex) {
                var columnName = columns[columnIndex];
                typeComboBox.removeAllItems();
                if (columnName.equals("Type")) {
                    for (String supportedType : Utils.getAllSupportedVariableTypes()) {
                        typeComboBox.addItem(supportedType);
                    }
                }
                return super.getTableCellEditorComponent(table, value, isSelected, rowIndex, columnIndex);
            }
        });

        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
        bar.setFloatable(false);
        bar.setOpaque(false);
        JButton addrow = createAddButton();
        bar.add(addrow);
        JButton remrow = createRemoveRowButton();
        bar.add(remrow);
        bar.add(Utils.initSearchComponent(lastSearchResult, this));

        addrow.addActionListener(a -> {
            dependencies.add(new TriggerModElement.Dependency("name" + dependencies.size(), "type"));
            refreshTable();
        });
        remrow.addActionListener(a -> {
            jTable.editCellAt(-1, 0);
            Arrays.stream(jTable.getSelectedRows()).mapToObj(b -> dependencies.get(b)).forEach(c -> {
                dependencies.remove(c);
            });
            refreshTable();
        });

        addPage("Attributes", PanelUtils.totalCenterInPanel(configurationPanel)).validate(triggerName);

        addPage("Parameters", toolbarAndTable(bar));
    }

    @Override public void doSearch(Map.Entry<String, String> search) {
        lastSearchResult.clear();
        // cache
        lastSearchResult.add(0);
        for (int i = 0; i < dependencies.size(); i++) {
            var entry = dependencies.get(i);
            var index = new AtomicInteger();
            if (Stream.of(entry.getName(), entry.getType())
                    .map(a -> Map.entry(columns[index.getAndIncrement()], Rules.SearchRules.applyIgnoreCaseRule(a)))
                    .anyMatch(a -> {
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
        this.readableName.setText(generatableElement.readableName);
        this.hasResult.setSelected(generatableElement.has_result);
        this.cancelable.setSelected(generatableElement.cancelable);
        this.side.setSelectedItem(generatableElement.side);
        this.requiredApis.setTextList(generatableElement.required_apis);
        this.dependencies = generatableElement.dependencies_provided;
    }

    @Override public TriggerModElement getElementFromGUI() {
        modElement.setRegistryName(triggerName.getText());
        var trigger = new TriggerModElement(modElement);
        trigger.readableName = readableName.getText();
        trigger.cancelable = this.cancelable.isSelected();
        trigger.has_result = this.hasResult.isSelected();
        trigger.side = this.side.getSelectedItem();
        trigger.required_apis = this.requiredApis.getTextList();
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
            if (columns[columnIndex].equals("Name")) {
                row.setName(aValue.toString());
            } else if (columns[columnIndex].equals("Type")) {
                row.setType(aValue.toString());
            }
        }
    }
}
