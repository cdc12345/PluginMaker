package org.cdc.generator.init;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.dialogs.file.FileDialogs;
import net.mcreator.ui.init.L10N;
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.ui.elements.DataListModElementGUI;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.datatransfer.StringSelection;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class Menus {
	public static final JMenu PLUGIN_MAKER = L10N.menu("menus.plugin_maker");
	public static final JMenu DATALIST_UTILS = L10N.menu("menus.datalist_utils");
	public static final JMenu MAPPING_UTILS = L10N.menu("menus.mapping_utils");

	public static void registerAllMenus(MCreator mCreator) {
		mCreator.getMainMenuBar().add(PLUGIN_MAKER);
		JMenuItem loadLangFromExternal = new JMenuItem("Load properties lang file");
		loadLangFromExternal.addActionListener(a -> {
			var file = FileDialogs.getOpenDialog(mCreator, new String[] { "*.properties" });
			Properties properties = new Properties();
			try {
				properties.load(new FileReader(file));
				var langmap = mCreator.getWorkspace().getLanguageMap();
				var en = langmap.get("en_us");
				for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
					en.put(objectObjectEntry.getKey().toString(), objectObjectEntry.getValue().toString());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		PLUGIN_MAKER.add(loadLangFromExternal);

		mCreator.getMainMenuBar().add(DATALIST_UTILS);
		JMenu calculateTypes = L10N.menu("menus.datalist_utils.calculate_types");
		calculateTypes.addMenuListener(new MenuListener() {
			@Override public void menuSelected(MenuEvent e) {
				if (mCreator.getTabs().getCurrentTab()
						.getContent() instanceof DataListModElementGUI dataListModElementGUI) {
					calculateTypes.removeAll();
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
						calculateTypes.add(menuItem);
					});
				}
			}

			@Override public void menuDeselected(MenuEvent e) {

			}

			@Override public void menuCanceled(MenuEvent e) {

			}
		});
		JMenu builtInEntries = L10N.menu("menus.datalist_utils.builtin_entries");
		Stream.of("_default", "_mcreator_map_template", "_bypass_prefix").forEach(a -> {
			JMenuItem menuItem = new JMenuItem(a);
			menuItem.addActionListener(even -> {
				if (mCreator.getTabs().getCurrentTab()
						.getContent() instanceof DataListModElementGUI dataListModElementGUI) {
					dataListModElementGUI.entries.add(new DataListModElement.DataListEntry(a, null, null, null, null));
					dataListModElementGUI.refreshTable();
				}
			});
			builtInEntries.add(menuItem);
		});
		DATALIST_UTILS.add(builtInEntries);
		DATALIST_UTILS.add(calculateTypes);
	}
}
