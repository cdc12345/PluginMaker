package org.cdc.generator.ui.elements;

import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.blockly.BlocklyEditorType;
import net.mcreator.ui.component.JColor;
import net.mcreator.ui.component.SearchableComboBox;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.ProcedureCategoryModElement;
import org.cdc.generator.init.ModElementTypes;
import org.cdc.generator.utils.Rules;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

public class ProcedureCategoryModElementGUI
        extends AbstractConfigurationTableModElementGUI<ProcedureCategoryModElement> {

    // aitasks and so on can extends the class.
    protected final VTextField name;
    protected final JColor color;
    protected final SearchableComboBox<String> parentCategory = new SearchableComboBox<>();

    public ProcedureCategoryModElementGUI(MCreator mcreator, @NonNull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode, null);
        this.name = new VTextField();
        this.color = new JColor(mcreator, false, false);

        if (editingMode) {
            name.setEnabled(false);
        }

        this.initGUI();
        this.finalizeGUI();
    }

    @Override protected void initGUI() {
        initConfiguration(new GridLayout(4, 2, 5, 5));

        name.setText(modElement.getRegistryName());
        name.setValidator(Rules.getFileNameValidator(name::getText));
        addNameConfiguration(name);

        addConfigurationWithHelpEntry("color", color);

        addConfigurationWithHelpEntry("parent_category", parentCategory);

        addPage("edit", PanelUtils.totalCenterInPanel(configurationPanel));
    }

    @Override protected void openInEditingMode(ProcedureCategoryModElement generatableElement) {
        this.color.setColor(generatableElement.color);
        this.parentCategory.setSelectedItem(generatableElement.parentCategory);
    }

    @Override public ProcedureCategoryModElement getElementFromGUI() {
        modElement.setRegistryName(name.getText());
        var element = new ProcedureCategoryModElement(modElement);
        element.parentCategory = parentCategory.getSelectedItem();
        element.color = color.getColor();
        return element;
    }

    @Override @Nullable public URI contextURL() throws URISyntaxException {
        return null;
    }

    @Override public void reloadDataLists() {
        var stringArrayList = new HashSet<String>();
        for (ModElement element : mcreator.getWorkspaceInfo()
                .getElementsOfType(ModElementTypes.PROCEDURE_CATEGORY.getRegistryName())) {
            stringArrayList.add(element.getRegistryName());
        }
        stringArrayList.addAll(BlocklyLoader.getBuiltinCategories());
        BlocklyLoader.INSTANCE.getBlockLoader(BlocklyEditorType.PROCEDURE).getDefinedBlocks().values().forEach(a -> {
            if (a.getToolboxCategory() != null) {
                stringArrayList.add(a.getToolboxCategoryRaw());
            }
        });
        ComboBoxUtil.updateComboBoxContents(parentCategory, stringArrayList.stream().sorted().toList());
    }
}
