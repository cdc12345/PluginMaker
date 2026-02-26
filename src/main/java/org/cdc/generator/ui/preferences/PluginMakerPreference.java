package org.cdc.generator.ui.preferences;

import net.mcreator.preferences.PreferencesSection;
import net.mcreator.preferences.entries.StringEntry;

public class PluginMakerPreference extends PreferencesSection {

	public static PluginMakerPreference INSTANCE;

	public StringEntry preferGenerator;

	public PluginMakerPreference(String preferencesIdentifier) {
		super(preferencesIdentifier);

		this.preferGenerator = addPluginEntry("plugin_generator",
				new StringEntry("prefer_generator", "quilt-1.7.10", true));
	}

	@Override public String getSectionKey() {
		return "plugin_maker";
	}
}
