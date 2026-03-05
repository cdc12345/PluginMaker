package org.cdc.generator.init;

import net.mcreator.plugin.events.ui.TabEvent;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.dialogs.file.FileDialogs;
import net.mcreator.ui.init.L10N;
import org.cdc.generator.PluginMain;
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.ui.elements.DataListModElementGUI;
import org.cdc.generator.utils.builders.JMenuBuilder;
import org.cdc.generator.utils.builders.JMenuItemBuilder;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class Menus {
    public static final JMenu PLUGIN_MAKER = register(L10N.menu("menus.plugin_maker"));
    public static final JMenu DATALIST_UTILS = register(L10N.menu("menus.datalist_utils"));

    private static ArrayList<JMenu> menus;

    private static JMenu register(JMenu menu) {
        if (menus == null) {
            menus = new ArrayList<>();
        }
        menus.add(menu);
        return menu;
    }

    public static void registerMenuVisibleControls() {
        PluginMain.getINSTANCE().addListener(TabEvent.Shown.class, event -> {
            Menus.DATALIST_UTILS.setVisible(event.getTab().getContent() instanceof DataListModElementGUI);
        });
    }

    public static void registerAllMenus(MCreator mcreator) {
        for (JMenu menu : menus) {
            mcreator.getMainMenuBar().add(menu);
        }
        PLUGIN_MAKER.add(new JMenuItemBuilder().setParentMenuName("datalist_utils").setName("load_from_external_en_us")
                .setActionListener(a -> {
                    var file = FileDialogs.getOpenDialog(mcreator, new String[] { "*.properties" });
                    Properties properties = new Properties();
                    try {
                        properties.load(new FileReader(file));
                        var langmap = mcreator.getWorkspace().getLanguageMap();
                        var en = langmap.get("en_us");
                        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
                            en.put(objectObjectEntry.getKey().toString(), objectObjectEntry.getValue().toString());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).build());
        DATALIST_UTILS.add(new JMenuBuilder().setParentMenuName("datalist_utils").setName("builtin_entries")
                .setInit(menu -> Stream.of("_default", "_mcreator_map_template", "_bypass_prefix").forEach(a -> {
                    JMenuItem menuItem = new JMenuItem(a);
                    menuItem.addActionListener(even -> {
                        if (mcreator.getTabs().getCurrentTab()
                                .getContent() instanceof DataListModElementGUI dataListModElementGUI) {
                            dataListModElementGUI.entries.add(new DataListModElement.DataListEntry(a));
                            dataListModElementGUI.refreshTable();
                        }
                    });
                    menu.add(menuItem);
                })).build());
        DATALIST_UTILS.add(
                new JMenuBuilder().setParentMenuName("datalist_utils").setName("calculate_types").setReload(a -> {
                    JMenu jMenu = (JMenu) a.getSource();
                    if (mcreator.getTabs().getCurrentTab()
                            .getContent() instanceof DataListModElementGUI dataListModElementGUI) {
                        jMenu.removeAll();
                        dataListModElementGUI.getTypes().forEach(b -> {
                            var menuItem = new JMenuItem(b);
                            menuItem.addActionListener(e1 -> {
                                var content = new StringSelection(b);
                                mcreator.getToolkit().getSystemClipboard().setContents(content, content);
                                JOptionPane.showMessageDialog(mcreator, "Copied");
                            });
                            jMenu.add(menuItem);
                        });
                    }
                }).build());
    }
}
