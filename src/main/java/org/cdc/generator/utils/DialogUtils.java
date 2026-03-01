package org.cdc.generator.utils;

import net.mcreator.ui.component.util.PanelUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.stream.Collectors;

public class DialogUtils {
	public static int showOptionPaneWithTextArea(JTextArea jTextArea, Component parent, String title,
			Collection<?> collections) {
		jTextArea.setOpaque(false);
		jTextArea.setRows(5);
		JScrollPane jScrollPane = new JScrollPane(jTextArea);
		jScrollPane.setBorder(BorderFactory.createTitledBorder("Lines"));
		if (!collections.isEmpty()) {
			jTextArea.setText(collections.stream().map(Object::toString).collect(Collectors.joining("\n")));
		}
		return JOptionPane.showConfirmDialog(parent, jScrollPane, title, JOptionPane.YES_NO_OPTION);
	}

	public static int showOptionPaneWithTextAreaAndToolBar(JTextArea jTextArea, JToolBar toolbar, Component parent,
			String title, Collection<?> collections) {
		jTextArea.setOpaque(false);
		jTextArea.setRows(5);
		JScrollPane jScrollPane = new JScrollPane(jTextArea);
		jScrollPane.setBorder(BorderFactory.createTitledBorder("Lines"));
		if (!collections.isEmpty()) {
			jTextArea.setText(collections.stream().map(Object::toString).collect(Collectors.joining("\n")));
		}
		return JOptionPane.showConfirmDialog(parent, PanelUtils.northAndCenterElement(toolbar, jScrollPane), title,
				JOptionPane.YES_NO_OPTION);
	}
}
