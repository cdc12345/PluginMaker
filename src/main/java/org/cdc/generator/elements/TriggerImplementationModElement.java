package org.cdc.generator.elements;

import com.google.j2objc.annotations.UsedByReflection;
import net.mcreator.element.GeneratableElement;
import net.mcreator.generator.Generator;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.utils.ElementsUtils;

import java.util.Locale;

public class TriggerImplementationModElement extends GeneratableElement {

	public String generatorName;
	public String triggerElementName;

	public String eventName;
	public String methodBody;

	public TriggerImplementationModElement(ModElement element) {
		super(element);
	}

	@UsedByReflection public String getTriggerName() {
		if (triggerElementName == null) {
			return null;
		}
		return ElementsUtils.getTriggerName(getModElement().getWorkspace(), triggerElementName);
	}

	@UsedByReflection public String getGeneratorName() {
		return generatorName;
	}

	// a probable bug: neoforge or forge change their event registration in new version
	// so here needs a new solution.
	@UsedByReflection public String getGeneratorFlavor() {
		var generator = Generator.GENERATOR_CACHE.get(generatorName);
		if (generator != null) {
			return generator.getGeneratorFlavor().name();
		}
		return generatorName.split("-")[0].toUpperCase(Locale.ROOT);
	}

	@UsedByReflection public String getEventNameUsedAsMethodName() {
		if (eventName.contains(".")) {
			return eventName.split("\\.")[0];
		}
		return eventName;
	}

	@UsedByReflection public String[] getMethodBodyLines() {
		return methodBody.split("\n");
	}
}
