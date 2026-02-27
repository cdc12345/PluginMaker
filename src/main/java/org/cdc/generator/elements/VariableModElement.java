package org.cdc.generator.elements;

import javax.annotation.Nullable;
import java.util.List;

//TODO
public class VariableModElement {
	private String name;
	private String color;
	private String blocklyVariableType;
	private boolean ignoredByCoverage;
	private boolean nullable;

	@Nullable public List<String> required_apis;
}
