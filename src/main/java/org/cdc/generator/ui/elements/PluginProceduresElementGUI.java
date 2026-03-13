package org.cdc.generator.ui.elements;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.PluginProcedureModElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;

public class PluginProceduresElementGUI extends ModElementGUI<PluginProcedureModElement> {

    public PluginProceduresElementGUI(MCreator mcreator, @Nonnull ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);
    }

    @Override protected void initGUI() {

    }

    @Override protected void openInEditingMode(PluginProcedureModElement generatableElement) {

    }

    @Override public PluginProcedureModElement getElementFromGUI() {
        return null;
    }

    @Override @Nullable public URI contextURL() throws URISyntaxException {
        return null;
    }
}
