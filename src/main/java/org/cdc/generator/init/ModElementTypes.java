package org.cdc.generator.init;

import net.mcreator.element.GeneratableElement;
import net.mcreator.element.ModElementType;
import net.mcreator.element.ModElementTypeLoader;
import org.cdc.generator.elements.*;
import org.cdc.generator.ui.elements.*;

import javax.annotation.Nullable;

@SuppressWarnings("unused") public class ModElementTypes {
	public static final ModElementType<DataListModElement> DATA_LIST = register("plugindatalist", null,
			DataListModElementGUI::new, DataListModElement.class);
	public static final ModElementType<MappingsModElement> MAPPINGS = register("pluginmappings", null,
			MappingsModElementGUI::new, MappingsModElement.class);
	public static final ModElementType<TriggerModElement> TRIGGER = register("plugintrigger", null,
			TriggerModElementGUI::new, TriggerModElement.class);
	public static final ModElementType<VariableModElement> VARIABLE = register("pluginvariable", null,
			VariableModElementGUI::new, VariableModElement.class);
	public static final ModElementType<APIModElement> APIS = register("pluginapis", null, APIModElementGUI::new,
			APIModElement.class);

	private static <E extends GeneratableElement> ModElementType<E> register(String registryName,
			@Nullable Character shortcut, ModElementType.ModElementGUIProvider<E> modElementGUIProvider,
			Class<E> modElementStorageClass) {
		var modElementType = new ModElementType<>(registryName, shortcut, modElementGUIProvider,
				modElementStorageClass);
		ModElementTypeLoader.register(modElementType);
		return modElementType;
	}
}
