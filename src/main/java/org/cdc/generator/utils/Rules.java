package org.cdc.generator.utils;

import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.Validator;
import net.mcreator.ui.validation.component.VComboBox;

import java.util.Objects;
import java.util.regex.Pattern;

public class Rules {
	public static final Pattern MAPPING_INNER_KEY = Pattern.compile("(_default|_mcreator_map_template)");
	public static final Pattern DATALIST_ENTRY_NAME = Pattern.compile("[a-zA-Z_1-9.]+");
	public static final Pattern DATALIST_NAME = Pattern.compile("[a-z_]+");

	public static Validator getDataListNameValidator(VComboBox<String> datalistName){
		return () -> {
			if (Rules.DATALIST_NAME.matcher(Objects.requireNonNull(datalistName.getSelectedItem())).matches()) {
				return ValidationResult.PASSED;
			}
			return new ValidationResult(ValidationResult.Type.ERROR,
					"You must use whole english and whole lower letters");
		};
	}
}
