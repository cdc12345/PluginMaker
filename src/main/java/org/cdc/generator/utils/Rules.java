package org.cdc.generator.utils;

import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.Validator;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import org.cdc.generator.PluginMain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class Rules {
	public static final Pattern DATALIST_ENTRY_NAME = Pattern.compile("[a-zA-Z_1-9.]+");
	public static final Pattern FILE_NAME = Pattern.compile("[a-z_]+");
	public static final Pattern VALID_MODID = Pattern.compile("^(?=.{2,64}$)[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$");

	public static Validator getDataListNameValidator(VComboBox<String> datalistName) {
		return () -> {
			if (Rules.FILE_NAME.matcher(Objects.requireNonNull(datalistName.getSelectedItem())).matches()) {
				return ValidationResult.PASSED;
			}
			return new ValidationResult(ValidationResult.Type.ERROR,
					"You must use whole english and whole lower letters");
		};
	}

	public static Validator getTriggerNameValidator(VTextField datalistName) {
		return () -> {
			if (Rules.FILE_NAME.matcher(Objects.requireNonNull(datalistName.getText())).matches()) {
				return ValidationResult.PASSED;
			}
			return new ValidationResult(ValidationResult.Type.ERROR,
					"You must use whole english and whole lower letters");
		};
	}

	public static class SearchRules {
		private SearchRules() {

		}

		private static boolean ignoreCase = true;

		/**
		 * Used by searchbar
		 */
		public static String applyIgnoreCaseRule(String origin) {
			if (ignoreCase) {
				return origin.toLowerCase(Locale.ROOT);
			}
			return origin;
		}

		public static void setIgnoreCase(boolean ignoreCase1) {
			PluginMain.LOG.debug("Notify rule changed: {}->{}", ignoreCase, ignoreCase1);
			ignoreCase = ignoreCase1;
		}

		public static boolean isIgnoreCase() {
			return ignoreCase;
		}
	}

}
