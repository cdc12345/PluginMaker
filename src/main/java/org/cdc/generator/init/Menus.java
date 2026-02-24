package org.cdc.generator.init;

import net.mcreator.ui.MCreator;

import javax.swing.*;

public class Menus {
    public static final JMenu PLUGIN_MAKER = new JMenu("Plugin maker");

    private static void registerAllMenus(MCreator mCreator){

        mCreator.getMainMenuBar().add(PLUGIN_MAKER);
    }
}
