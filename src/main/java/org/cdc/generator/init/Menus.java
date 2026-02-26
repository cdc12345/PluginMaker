package org.cdc.generator.init;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.init.L10N;

import javax.swing.*;

public class Menus {
	public static final JMenu PLUGIN_MAKER = L10N.menu("menus.plugin_maker");
	public static final JMenu DATALIST_UTILS = L10N.menu("menus.datalist_utils");

	public static void registerAllMenus(MCreator mCreator) {
		mCreator.getMainMenuBar().add(PLUGIN_MAKER);

		mCreator.getMainMenuBar().add(DATALIST_UTILS);
	}
}
