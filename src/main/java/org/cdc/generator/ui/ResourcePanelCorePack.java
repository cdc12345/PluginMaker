package org.cdc.generator.ui;

import net.mcreator.io.tree.FileNode;
import net.mcreator.io.tree.FileTree;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.ui.component.tree.FilterTreeNode;
import net.mcreator.ui.component.tree.FilteredTreeModel;
import net.mcreator.ui.component.tree.JFileTree;
import net.mcreator.ui.minecraft.recourcepack.ResourcePackTreeCellRenderer;
import net.mcreator.ui.workspace.IReloadableFilterable;
import net.mcreator.ui.workspace.WorkspacePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

public class ResourcePanelCorePack extends JPanel implements IReloadableFilterable {

    private final JFileTree tree;
    private final FilteredTreeModel model;
    private String selected;

    public ResourcePanelCorePack(WorkspacePanel workspacePanel) {
        super(new BorderLayout());
        this.model = new FilteredTreeModel(new FilterTreeNode(""));
        this.tree = new JFileTree(model);
        tree.setCellRenderer(new ResourcePackTreeCellRenderer());

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyName = new JMenuItem("Copy name");
        copyName.addActionListener(e -> {
            if (tree.getSelectionPath() != null) {
                var content = new StringSelection(tree.getSelectionPath().getLastPathComponent().toString().split("\\.")[0]);
                tree.getToolkit().getSystemClipboard().setContents(content, content);
                tree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS).execute(() -> {
                    tree.setCursor(Cursor.getDefaultCursor());
                });
            }
        });
        popupMenu.add(copyName);

        tree.setComponentPopupMenu(popupMenu);

        JScrollPane scrollPane = new JScrollPane(tree);

        this.add("Center", scrollPane);
    }

    @Override
    public void reloadElements() {
        FilterTreeNode root = new FilterTreeNode("");

        FileTree<String> fileTree = new FileTree<>(new FileNode<>("", ""));
        File file = PluginLoader.INSTANCE.getPlugins().stream().filter(a -> a.getID().equals("core")).findFirst().get().getFile();
        if (file.isFile() && file.getName().endsWith(".zip")) {
            try (ZipFile zipFile = new ZipFile(file)) {
                zipFile.stream().forEach(a -> {
                    fileTree.addElement(a.getName(), a.toString());
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (var walker = Files.walk(file.toPath())) {
                walker.forEach(a -> {
                    if (Files.isRegularFile(a)) {
                        fileTree.addElement(a.toString().replace('\\', '/').replace("./plugins/mcreator-core",""), a.toString());
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        JFileTree.addFileNodeToRoot(root, fileTree.root());
        model.setRoot(root);
        model.refilter();
    }

    @Override
    public void refilterElements() {

    }
}
