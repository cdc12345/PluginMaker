package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.util.image.ImageUtils;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.VariableImplementationModElement;
import org.cdc.generator.elements.VariableModElement;
import org.cdc.generator.init.ModElementTypes;
import org.cdc.generator.ui.preferences.PluginMakerPreference;
import org.cdc.generator.utils.Constants;
import org.cdc.generator.utils.DialogUtils;
import org.cdc.generator.utils.Utils;
import org.cdc.generator.utils.YamlUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class VariableImplementationModElementGUI extends ModElementGUI<VariableImplementationModElement> {

    private final String[] columns = new String[] { "Scope name", "Init", "Get", "Set", "Read", "Write" };

    private final VComboBox<String> generator = new VComboBox<>();
    private final VComboBox<String> variableElementName = new VComboBox<>();
    private final VTextField defaultValue = new VTextField();

    private List<VariableImplementationModElement.VariableScope> scopeList = new ArrayList<>();

    private final Map<String, MethodHandle> cacheHandles = new HashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public VariableImplementationModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);

        if (editingMode) {
            generator.setEnabled(false);
            variableElementName.setEnabled(false);
        }

        this.initGUI();
        this.finalizeGUI();
    }

    @Override protected void initGUI() {
        JPanel configuration = new JPanel(new GridLayout(3, 2));
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
        configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariableimpl/generator"),
                L10N.label("elementgui.common.generator")));
        configuration.add(generator);

        variableElementName.setEditable(false);
        variableElementName.setValidator(() -> {
            if (variableElementName.getSelectedItem() != null && !variableElementName.getSelectedItem().isBlank()) {
                return ValidationResult.PASSED;
            }
            return new ValidationResult(ValidationResult.Type.ERROR, "can not be empty");
        });
        variableElementName.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel jLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (mcreator.getWorkspace().getModElementByName(value.toString())
                        .getGeneratableElement() instanceof VariableModElement variableModElement) {
                    jLabel.setIcon(ImageUtils.createColorSquare(variableModElement.color, 32, 32));
                }
                return jLabel;
            }
        });
        configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariableimpl/variableelementname"),
                L10N.label("elementgui.pluginvariableimpl.variable_element_name")));
        configuration.add(variableElementName);

        defaultValue.setText("null");
        defaultValue.setValidator(() -> {
            if (defaultValue.getText() != null && !defaultValue.getText().isBlank()) {
                return ValidationResult.PASSED;
            }
            return new ValidationResult(ValidationResult.Type.ERROR, "can not be empty");
        });
        defaultValue.setPreferredSize(Utils.tryToGetTextFieldSize());
        configuration.add(HelpUtils.wrapWithHelpButton(this.withEntry("pluginvariableimpl/defaultvalue"),
                L10N.label("elementgui.pluginvariableimpl.default_value")));
        configuration.add(defaultValue);

        JTable jTable = new JTable(new ScopesTableModel());
        Utils.initTable(jTable);
        jTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value1, boolean isSelected, int rowIndex,
                    int column) {
                var row = scopeList.get(rowIndex);
                var jTextArea = new RSyntaxTextArea();
                var columnName = columns[column];
                int op = DialogUtils.showOptionPaneWithTextArea(jTextArea, mcreator,
                        "Edit" + columnName + " lines (one line one item)",
                        YamlUtils.splitString(Objects.requireNonNullElse(value1, "").toString()));
                if (op == JOptionPane.YES_OPTION) {
                    try {
                        MethodHandle set;
                        if (cacheHandles.containsKey(columnName)) {
                            set = cacheHandles.get(columnName);
                        } else {
                            set = lookup.findVirtual(VariableImplementationModElement.VariableScope.class,
                                    "set" + columnName, MethodType.methodType(Void.TYPE, String.class));
                        }
                        cacheHandles.put(columnName, set);
                        set.invoke(row, jTextArea.getText());
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            }
        });
        for (Field field : Constants.VariableScopes.class.getFields()) {
            try {
                scopeList.add(new VariableImplementationModElement.VariableScope((String) field.get(null)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        jTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(jTable);

        addPage("Configuration",
                PanelUtils.northAndCenterElement(PanelUtils.totalCenterInPanel(configuration), scrollPane)).validate(
                variableElementName).validate(generator).validate(defaultValue);

    }

    @Override protected void openInEditingMode(VariableImplementationModElement generatableElement) {
        this.generator.setSelectedItem(generatableElement.getGeneratorName());
        this.variableElementName.setSelectedItem(generatableElement.variableElementName);
        this.defaultValue.setText(generatableElement.defaultValue);
        this.scopeList = generatableElement.scopes;
    }

    @Override public VariableImplementationModElement getElementFromGUI() {
        VariableImplementationModElement element = new VariableImplementationModElement(modElement);
        element.generator = generator.getSelectedItem();
        element.variableElementName = variableElementName.getSelectedItem();
        element.defaultValue = defaultValue.getText();
        element.scopes = scopeList.stream().map(VariableImplementationModElement.VariableScope::clone).toList();
        return element;
    }

    @Override public @Nullable URI contextURL() throws URISyntaxException {
        return null;
    }

    @Override public void reloadDataLists() {
        ArrayList<String> stringArrayList = new ArrayList<>();
        for (ModElement element : mcreator.getWorkspaceInfo()
                .getElementsOfType(ModElementTypes.VARIABLE.getRegistryName())) {
            stringArrayList.add(element.getName());
        }
        ComboBoxUtil.updateComboBoxContents(variableElementName, stringArrayList);
    }

    //TODO: autocomplete create

    private class ScopesTableModel extends AbstractTableModel {

        @Override public int getRowCount() {
            return scopeList.size();
        }

        @Override public int getColumnCount() {
            return columns.length;
        }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            var row = scopeList.get(rowIndex);
            var columns = new String[] { row.getName(), row.getInit(), row.getGet(), row.getSet(), row.getRead(),
                    row.getWrite() };
            return columns[columnIndex];
        }

        @Override public String getColumnName(int column) {
            return columns[column];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
            return !columns[columnIndex].equals("Scope name");
        }

        @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            var row = scopeList.get(rowIndex);
            switch (columns[columnIndex]) {
            case "Init" -> row.setInit(aValue.toString());
            case "Get" -> row.setGet(aValue.toString());
            case "Set" -> row.setSet(aValue.toString());
            case "Read" -> row.setRead(aValue.toString());
            case "Write" -> row.setWrite(aValue.toString());
            }
        }
    }
}
