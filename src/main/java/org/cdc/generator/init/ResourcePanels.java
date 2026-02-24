package org.cdc.generator.init;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.workspace.WorkspacePanel;
import org.cdc.generator.ui.ResourcePanelModTypes;
import org.cdc.generator.ui.ResourcePanelTemplates;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ResourcePanels {

    static {
        nameToPanels = new HashMap<>();
        register("mod_types", ResourcePanelModTypes::new);
        register("templates", ResourcePanelTemplates::new);
    }

    private static Map<String, Function<WorkspacePanel, JPanel>> nameToPanels;

    private static void register(String name, Function<WorkspacePanel, JPanel> resourcePanel) {
        nameToPanels.put(name, resourcePanel);

    }

    public static void register(MCreator mCreator) {
        if (mCreator.getWorkspacePanel() instanceof WorkspacePanel modMaker) {
            nameToPanels.forEach((a, b) -> {
                modMaker.resourcesPan.addResourcesTab(a, b.apply(modMaker));
            });
        }
    }
}
