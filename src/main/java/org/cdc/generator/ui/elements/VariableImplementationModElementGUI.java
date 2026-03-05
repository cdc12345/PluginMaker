package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.util.image.ImageUtils;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.VariableImplementationModElement;
import org.cdc.generator.elements.VariableModElement;
import org.cdc.generator.init.ModElementTypes;
import org.cdc.generator.ui.preferences.PluginMakerPreference;
import org.cdc.generator.utils.DialogUtils;
import org.cdc.generator.utils.Utils;
import org.cdc.generator.utils.YamlUtils;
import org.cdc.generator.utils.builder.AutoCompletionFactory;
import org.cdc.generator.utils.builder.RSyntaxTextAreaFactory;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class VariableImplementationModElementGUI
        extends AbstractConfigurationTableModElementGUI<VariableImplementationModElement> {
    private final VComboBox<String> generator = new VComboBox<>();
    private final VComboBox<String> variableElementName = new VComboBox<>();
    private final VTextField defaultValue = new VTextField();

    private List<VariableImplementationModElement.VariableScope> scopeList = new ArrayList<>();

    private final Map<String, MethodHandle> cacheHandles = new HashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public VariableImplementationModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode, new String[] { "Scope name", "Init", "Get", "Set", "Read", "Write" });

        if (editingMode) {
            generator.setEnabled(false);
            variableElementName.setEnabled(false);
        }

        this.initGUI();
        this.finalizeGUI();
    }

    @Override protected void initGUI() {
        initConfiguration(new GridLayout(3, 2));

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
        addGeneratorConfiguration(generator);

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
        addConfigurationWithHelpEntry("variable_element_name", variableElementName);

        defaultValue.setText("null");
        defaultValue.setValidator(() -> {
            if (defaultValue.getText() != null && !defaultValue.getText().isBlank()) {
                return ValidationResult.PASSED;
            }
            return new ValidationResult(ValidationResult.Type.ERROR, "can not be empty");
        });
        defaultValue.setPreferredSize(Utils.tryToGetTextFieldSize());
        addConfigurationWithHelpEntry("default_value", defaultValue);

        initTable(new ScopesTableModel());
        jTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value1, boolean isSelected, int rowIndex,
                    int column) {
                var row = scopeList.get(rowIndex);
                var jTextArea = RSyntaxTextAreaFactory.createDefaultRSyntaxTextArea();
                AutoCompletionFactory.createDefaultCompletion(jTextArea,
                        VariableImplementationModElementGUI.this::createCompletionProvider);
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
        for (String s : Utils.getAllVariableScope()) {
            scopeList.add(new VariableImplementationModElement.VariableScope(s));
        }

        addPage("Configuration", PanelUtils.northAndCenterElement(configurationPanel, wrapTable())).validate(
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
        element.scopes = scopeList.stream().filter(VariableImplementationModElement.VariableScope::hasNotNull)
                .map(VariableImplementationModElement.VariableScope::clone).toList();
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

    private CompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        provider.addCompletion(new BasicCompletion(provider, "${name", "the name of variable"));
        provider.addCompletion(new BasicCompletion(provider, "${scope", "the scope of variable"));
        provider.addCompletion(new BasicCompletion(provider, "${type", "the type of variable"));
        provider.addCompletion(new BasicCompletion(provider, "${value", "the value of variable"));
        provider.addCompletion(new BasicCompletion(provider, "${entity", "the entity of variable (nullable)"));
        provider.addCompletion(
                new BasicCompletion(provider, "${var", "net.mcreator.workspace.elements.VariableElement"));

        Utils.initCompletionWithGenerator(provider, mcreator.getGenerator());

        return provider;
    }

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
    }
}
