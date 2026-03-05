package org.cdc.generator.ui.elements;

import net.mcreator.element.GeneratableElement;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.utils.Utils;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

public abstract class AbstractConfigurationTableModElementGUI<E extends GeneratableElement> extends ModElementGUI<E> {

    protected JPanel configurationPanel;
    protected JTable jTable;

    public AbstractConfigurationTableModElementGUI(MCreator mcreator, @NonNull ModElement modElement,
            boolean editingMode) {
        super(mcreator, modElement, editingMode);
    }

    protected void initConfiguration(LayoutManager layoutManager) {
        configurationPanel = new JPanel(layoutManager);
        configurationPanel.setOpaque(false);
        configurationPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
    }

    protected void initTable(TableModel tableModel) {
        jTable = new JTable(tableModel);
        Utils.initTable(jTable);
    }

    protected void addConfigurationWithHelpEntry(String name, JComponent component) {
        configurationPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry(modElement.getTypeString() + "/" + name),
                L10N.label("elementgui." + modElement.getTypeString() + "." + name)));
        configurationPanel.add(component);
    }
}
