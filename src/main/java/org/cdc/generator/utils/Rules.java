package org.cdc.generator.utils;

import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.Validator;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import org.cdc.generator.PluginMain;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class Rules {
	public static final Pattern DATALIST_ENTRY_NAME = Pattern.compile("[a-zA-Z_1-9.]+");
	public static final Pattern FILE_NAME = Pattern.compile("[a-z_]+");
	public static final Pattern VALID_MODID = Pattern.compile("^(?=.{2,64}$)[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$");

	public static final int defaultHsvSaturation = 45;
	public static final int defaultHsvValue = 65;

	public static final String NONE = "None";

	public static Validator getComboBoxValidator(VComboBox<String> comboBox) {
		return () -> {
			if (Rules.FILE_NAME.matcher(Objects.requireNonNull(comboBox.getSelectedItem())).matches()) {
				return ValidationResult.PASSED;
			}
			return new ValidationResult(ValidationResult.Type.ERROR,
					"You must use whole english and whole lower letters");
		};
	}

	public static Validator getTextfieldValidator(VTextField textField) {
		return () -> {
			if (Rules.FILE_NAME.matcher(Objects.requireNonNull(textField.getText())).matches()) {
				return ValidationResult.PASSED;
			}
			return new ValidationResult(ValidationResult.Type.ERROR,
					"You must use whole english and whole lower letters");
		};
	}

	public static String convertColor(Color color) {
		if (color == null) {
			return "0";
		}

		float[] hsbvals = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbvals);

		double hue = Math.ceil(hsbvals[0] * 100);
		double saturation = Math.ceil(hsbvals[1] * 100);
		double brightness = Math.ceil(hsbvals[2] * 100);

		if (saturation == defaultHsvSaturation && brightness == defaultHsvValue) {
			return String.valueOf((int) hue);
		}
		return "\"" + Utils.formatColor(color) + "\"";
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
