package com.application.foundation.utils;

import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

public class ColorUtils {

	private static final float MIN_CONTRAST_TEXT = 4.5f;


	private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();



	public static @ColorInt int lerpColor(@ColorInt int startColor,
								@ColorInt int endColor,
								@FloatRange(from = 0, to = 1) float fraction) {

		float inverseFraction = 1 - fraction;

		float a = Color.alpha(startColor) * inverseFraction + Color.alpha(endColor) * fraction;
		float r = Color.red(startColor) * inverseFraction + Color.red(endColor) * fraction;
		float g = Color.green(startColor) * inverseFraction + Color.green(endColor) * fraction;
		float b = Color.blue(startColor) * inverseFraction + Color.blue(endColor) * fraction;

		return Color.argb((int) a, (int) r, (int) g, (int) b);
	}

	// alpha in range [0;255]
	public static @ColorInt int lerpColorAlpha(@ColorInt int color,
												 @IntRange(from = 0, to = 255) int startAlpha,
												 @IntRange(from = 0, to = 255) int endAlpha,
												 @FloatRange(from = 0, to = 1) float fraction) {

		return Color.argb((int) (Color.alpha(startAlpha) * (1 - fraction) + Color.alpha(endAlpha) * fraction),
				Color.red(color),
				Color.green(color),
				Color.blue(color));
	}


	private static double[] getTempDouble3Array() {
		double[] result = TEMP_ARRAY.get();
		if (result == null) {
			result = new double[3];
			TEMP_ARRAY.set(result);
		}
		return result;
	}


	// also see ContrastColorUtil
	public static boolean isColorLight(@ColorInt int color) {
		return androidx.core.graphics.ColorUtils.calculateLuminance(color) > 0.5f;
	}

	public static boolean isColorNearWhite(@ColorInt int color) {
		return getColorLightness(color) > 85;
	}

	/**
	 * Change a color by a specified value
	 * @param color the base color to lighten
	 * @param amount the amount to lighten the color from 0 to 100. This corresponds to the L
	 *               increase in the LAB color space. A negative value will darken the color and
	 *               a positive will lighten it.
	 * @return the changed color
	 */
	public static @ColorInt int changeColorLightness(@ColorInt int color, int amount) {
		double[] result = getTempDouble3Array();

		androidx.core.graphics.ColorUtils.colorToLAB(color, result);
		result[0] = Math.max(Math.min(100, result[0] + amount), 0);
		return androidx.core.graphics.ColorUtils.LABToColor(result[0], result[1], result[2]);
	}

	public static double getColorLightness(@ColorInt int color) {
		double[] result = getTempDouble3Array();

		androidx.core.graphics.ColorUtils.colorToLAB(color, result);
		return result[0];
	}

	public static @ColorInt int adjustForegroundColorLightness(@ColorInt int foreground, @ColorInt int background) {

		if (androidx.core.graphics.ColorUtils.calculateContrast(foreground, background) > MIN_CONTRAST_TEXT) {
			return foreground;
		}

		double lightnessFg = getColorLightness(foreground);

		AdjustForegroundColorLightnessResult resultIncrease = adjustForegroundColorLightness(foreground, background, lightnessFg, true);
		AdjustForegroundColorLightnessResult resultDecrease = adjustForegroundColorLightness(foreground, background, lightnessFg, false);

		if (resultIncrease.contrast >= MIN_CONTRAST_TEXT && resultDecrease.contrast >= MIN_CONTRAST_TEXT) {
			return resultIncrease.steps < resultDecrease.steps ? resultIncrease.foreground : resultDecrease.foreground;

		} else if (resultIncrease.contrast >= MIN_CONTRAST_TEXT) {
			return resultIncrease.foreground;
		} else {
			return resultDecrease.foreground;
		}
	}

	public static @ColorInt int changeForegroundColorContrast(@ColorInt int foreground, @ColorInt int background, int amountLightness) {

		int amountLightnessPositive = Math.abs(amountLightness);

		int fgIncrease = ColorUtils.changeColorLightness(foreground, amountLightnessPositive);
		int fgDecrease = ColorUtils.changeColorLightness(foreground, -amountLightnessPositive);

		double fgIncreaseContrast = androidx.core.graphics.ColorUtils.calculateContrast(fgIncrease, background);
		double fgDecreaseContrast = androidx.core.graphics.ColorUtils.calculateContrast(fgDecrease, background);

		if (amountLightness > 0) {
			return adjustForegroundColorLightness(fgIncreaseContrast > fgDecreaseContrast ? fgIncrease : fgDecrease, background);
		} else {
			return adjustForegroundColorLightness(fgIncreaseContrast > fgDecreaseContrast ? fgDecrease : fgIncrease, background);
		}
	}

	private static AdjustForegroundColorLightnessResult
			adjustForegroundColorLightness(int foreground, int background, double lightnessFg, boolean increaseLightness) {

		int steps = 0;
		int maxSteps = (int) (increaseLightness ? 100 - lightnessFg : lightnessFg) + 1;
		double contrast;

		do {
			foreground = changeColorLightness(foreground, increaseLightness ? 1 : -1);
			contrast = androidx.core.graphics.ColorUtils.calculateContrast(foreground, background);
			steps++;
		} while (contrast < MIN_CONTRAST_TEXT && steps < maxSteps);


		return new AdjustForegroundColorLightnessResult(foreground, contrast, steps);
	}

	private static class AdjustForegroundColorLightnessResult {
		final int foreground;
		final double contrast;
		final int steps;

		AdjustForegroundColorLightnessResult(int foreground, double contrast, int steps) {
			this.foreground = foreground;
			this.contrast = contrast;
			this.steps = steps;
		}
	}

	public static int getAverageColor(@ColorInt int... colors) {

		int redColors = 0;
		int greenColors = 0;
		int blueColors = 0;

		for (int c : colors) {
			redColors += Color.red(c);
			greenColors += Color.green(c);
			blueColors += Color.blue(c);
		}

		return Color.rgb(redColors / colors.length, greenColors / colors.length, blueColors / colors.length);
	}
}
