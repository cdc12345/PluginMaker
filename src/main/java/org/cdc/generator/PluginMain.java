package org.cdc.generator;

import net.mcreator.Launcher;
import net.mcreator.io.FileIO;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.events.ApplicationLoadedEvent;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.plugin.events.ui.TabEvent;
import net.mcreator.plugin.events.workspace.MCreatorLoadedEvent;
import net.mcreator.plugin.events.workspace.WorkspaceBuildStartedEvent;
import net.mcreator.ui.MCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdc.generator.init.Menus;
import org.cdc.generator.init.ResourcePanels;
import org.cdc.generator.ui.elements.DataListModElementGUI;
import org.cdc.generator.ui.preferences.PluginMakerPreference;
import org.cdc.generator.utils.Utils;
import org.cdc.generator.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PluginMain extends JavaPlugin {
	public static final Logger LOG = LogManager.getLogger("PluginMaker");

	private static PluginMain INSTANCE = null;

	public static PluginMain getINSTANCE() {
		return INSTANCE;
	}

	public PluginMain(Plugin plugin) {
		super(plugin);

		INSTANCE = this;
		addListener(MCreatorLoadedEvent.class, event -> {
			var mcreator = event.getMCreator();

			if (Utils.isNotPluginGenerator(mcreator.getGenerator())) {
				LOG.debug("{} is not plugin maker", mcreator.getGenerator().getGeneratorName());
				return;
			}

			registerAll(mcreator);

			// ensure that the plugin support self
			var selfDependants = "mcreator" + Launcher.version.versionlong;
			if (!mcreator.getWorkspaceSettings().dependants.contains(selfDependants)) {
				LOG.debug("Try to add self to dependants");
				mcreator.getWorkspaceSettings().dependants.add(selfDependants);
			}
			if (mcreator.getWorkspaceSettings().dependants.stream().noneMatch(str -> str.startsWith("weight_"))) {
				LOG.debug("Try to add weight_0 to dependants");
				mcreator.getWorkspaceSettings().dependants.add("weight_0");
			}

			var libs = new File(mcreator.getWorkspaceFolder(), "libs");
			if (libs.isDirectory() && !Launcher.version.isDevelopment()) {
				FileIO.deleteDir(libs);
				LOG.debug("Plugin maker has removed all old jars");
			}

			var mcreatorJar = new File("mcreator.jar");
			var mcreatorExe = new File("mcreator.exe");
			var mcreatorLibJar = new File(libs, "mcreator.jar");
			if (mcreatorJar.isFile()) {
				FileIO.copyFile(mcreatorJar, mcreatorLibJar);
				LOG.debug("Plugin maker has copied main mcreator lib, type: jar");
			} else if (mcreatorExe.isFile()) {
				try {
					var pureMCreatorJar = ZipUtils.tryToConvertExeToJar(mcreatorExe);
					FileIO.copyFile(pureMCreatorJar, mcreatorLibJar);
					LOG.debug("Plugin maker has copied main mcreator libs, type: exe");
					Files.deleteIfExists(pureMCreatorJar.toPath());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			var mcreatorLibs = new File("lib");
			if (mcreatorLibs.isDirectory()) {
				FileIO.copyDirectory(mcreatorLibs, libs);
				LOG.debug("Plugin maker has copied all mcreator libs");
			}

		});

		addListener(PreGeneratorsLoadingEvent.class, event -> {
			try {
				Class.forName("org.cdc.generator.init.ModElementTypes");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		});

		addListener(WorkspaceBuildStartedEvent.class, event -> {
			FileIO.removeEmptyDirs(event.getMCreator().getGenerator().getModAssetsRoot());
		});

		addListener(TabEvent.Shown.class, event -> {
			Menus.DATALIST_UTILS.setVisible(event.getTab().getContent() instanceof DataListModElementGUI);
		});

		addListener(ApplicationLoadedEvent.class,event -> {
			PluginMakerPreference.INSTANCE = new PluginMakerPreference("plugin_generator");
		});
	}

	public void registerAll(MCreator mcreator) {
		ResourcePanels.register(mcreator);
		Menus.registerAllMenus(mcreator);
	}
}
