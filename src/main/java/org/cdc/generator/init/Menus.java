package org.cdc.generator.init;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.init.L10N;
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.ui.elements.DataListModElementGUI;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.datatransfer.StringSelection;
import java.util.HashSet;

public class Menus {
	public static final JMenu PLUGIN_MAKER = L10N.menu("menus.plugin_maker");
	public static final JMenu DATALIST_UTILS = L10N.menu("menus.datalist_utils");

	public static void registerAllMenus(MCreator mCreator) {
		mCreator.getMainMenuBar().add(PLUGIN_MAKER);

		mCreator.getMainMenuBar().add(DATALIST_UTILS);
		JMenu calculate_types = L10N.menu("menus.datalist_utils.calculate_types");
		calculate_types.addMenuListener(new MenuListener() {
			@Override public void menuSelected(MenuEvent e) {
				if (mCreator.getTabs().getCurrentTab()
						.getContent() instanceof DataListModElementGUI dataListModElementGUI) {
					calculate_types.removeAll();
					var types = new HashSet<String>();
					for (DataListModElement.DataListEntry entry : dataListModElementGUI.entries) {
						types.add(entry.getType());
					}
					types.forEach(a -> {
						var menuItem = new JMenuItem(a);
						menuItem.addActionListener(e1 -> {
							var content = new StringSelection(a);
							mCreator.getToolkit().getSystemClipboard().setContents(content, content);
							JOptionPane.showMessageDialog(mCreator, "Copied");
						});
						calculate_types.add(menuItem);
					});
				}
			}

			@Override public void menuDeselected(MenuEvent e) {

			}

			@Override public void menuCanceled(MenuEvent e) {

			}
		});
		DATALIST_UTILS.add(calculate_types);
	}
}
