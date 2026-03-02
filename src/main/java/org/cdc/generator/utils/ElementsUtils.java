package org.cdc.generator.utils;

import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.DataListModElement;
import org.cdc.generator.elements.TriggerModElement;

public class ElementsUtils {
	public static String getDataListName(Workspace workspace, String name) {
		var datalist = workspace.getModElementByName(name);
		if (datalist == null) {
			return null;
		}
		return getDataListName(datalist);
	}

	public static String getDataListName(ModElement modElement) {
		if (modElement.getGeneratableElement() instanceof DataListModElement dataListModElement) {
			return dataListModElement.datalistName;
		}
		return modElement.getRegistryName();
	}

	public static String getTriggerName(Workspace workspace, String name) {
		var trigger = workspace.getModElementByName(name);
		if (trigger == null) {
			return null;
		}
		return getTriggerName(trigger);
	}

	public static String getTriggerName(ModElement modElement) {
		if (modElement.getGeneratableElement() instanceof TriggerModElement triggerModElement) {
			return triggerModElement.getName();
		}
		return modElement.getRegistryName();
	}
}
