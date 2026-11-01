package com.betternpchighlight.managers;

import java.awt.Color;
import java.util.Random;

import javax.inject.Inject;

import com.betternpchighlight.BetterNpcHighlightConfig;
import com.betternpchighlight.BetterNpcHighlightConfig.tagStyleMode;
import com.betternpchighlight.data.NPCInfo;
import com.betternpchighlight.data.NameAndIdContainer;

import net.runelite.api.Client;

public class ColorManager {
	/**
	 * Fill opacity used for a hex color suffix when the style has no fill color
	 * to take its opacity from (area and outline). Matches the default alpha of
	 * the fill colors in the config.
	 */
	private static final int DEFAULT_FILL_ALPHA = 20;

	@Inject
	private Client client;

	@Inject
	private BetterNpcHighlightConfig config;

	@Inject
	private NameAndIdContainer nameAndIdContainer;

	/**
	 * Color of the NPC in the list Used for Minimap dot and displayed names
	 *
	 * @return Color
	 */
	public Color getSpecificColor(NPCInfo n) {
		if (shouldUseSlayerHighlight(n))
		{
			return applyConfigOrRaveColor(config.taskColor(), config.slayerRave(), config.slayerRaveSpeed());
		}
		else if (n.getTile().isHighlight() && config.tileHighlight())
		{
			return applyConfigOrRaveColor(n.getTile().getColor(), config.tileRave(), config.tileRaveSpeed());
		}
		else if (n.getTrueTile().isHighlight() && config.trueTileHighlight())
		{
			return applyConfigOrRaveColor(n.getTrueTile().getColor(), config.trueTileRave(), config.trueTileRaveSpeed());
		}
		else if (n.getSwTile().isHighlight() && config.swTileHighlight())
		{
			return applyConfigOrRaveColor(n.getSwTile().getColor(), config.swTileRave(), config.swTileRaveSpeed());
		}
		else if (n.getSwTrueTile().isHighlight() && config.swTrueTileHighlight())
		{
			return applyConfigOrRaveColor(n.getSwTrueTile().getColor(), config.swTrueTileRave(), config.swTrueTileRaveSpeed());
		}
		else if (n.getHull().isHighlight() && config.hullHighlight())
		{
			return applyConfigOrRaveColor(n.getHull().getColor(), config.hullRave(), config.hullRaveSpeed());
		}
		else if (n.getArea().isHighlight() && config.areaHighlight())
		{
			return applyConfigOrRaveColor(n.getArea().getColor(), config.areaRave(), config.areaRaveSpeed());
		}
		else if (n.getOutline().isHighlight() && config.outlineHighlight())
		{
			return applyConfigOrRaveColor(n.getOutline().getColor(), config.outlineRave(), config.outlineRaveSpeed());
		}
		else if (n.getClickbox().isHighlight() && config.clickboxHighlight())
		{
			return applyConfigOrRaveColor(n.getClickbox().getColor(), config.clickboxRave(), config.clickboxRaveSpeed());
		}
		else
		{
			return null;
		}
	}

	/**
	 * Parses a hex color tag suffix such as "#FF1EF818" (AARRGGBB, the first two
	 * digits are the opacity) or "#1EF818" (RRGGBB, fully opaque). The leading
	 * "#" is required. Returns null when the value is not a valid hex color, so
	 * numeric preset suffixes and anything else fall through to the preset and
	 * default color handling.
	 *
	 * @return Color
	 */
	public static Color parseHexColor(String value) {
		if (value == null || !value.startsWith("#") || (value.length() != 7 && value.length() != 9))
		{
			return null;
		}

		String digits = value.substring(1);
		for (int i = 0; i < digits.length(); i++)
		{
			if (Character.digit(digits.charAt(i), 16) == -1)
			{
				return null;
			}
		}

		long hex = Long.parseLong(digits, 16);
		if (hasExplicitOpacity(value))
		{
			// #AARRGGBB - the first two digits are the opacity
			int rgb = (int) (hex & 0xFFFFFF);
			int alpha = (int) ((hex >> 24) & 0xFF);
			return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
		}

		// #RRGGBB - fully opaque
		return new Color((int) ((hex >> 16) & 0xFF), (int) ((hex >> 8) & 0xFF), (int) (hex & 0xFF), 255);
	}

	/**
	 * Whether a hex color suffix includes an explicit opacity value, i.e. it is
	 * an "#AARRGGBB" value instead of a plain "#RRGGBB". Only meaningful for
	 * values parseHexColor accepts.
	 */
	private static boolean hasExplicitOpacity(String value) {
		return value != null && value.length() == 9;
	}

	/**
	 * Returns color of either the config, a preset if selected, or a hex color
	 * suffix such as "#FF1EF818" (AARRGGBB) / "#1EF818" (RRGGBB). The suffix
	 * color is always fully opaque; the opacity from the suffix applies to the
	 * fill instead.
	 *
	 * @return Color
	 */
	public Color getHighlightColor(String presetOrHex, Color color) {
		Color hex = parseHexColor(presetOrHex);
		if (hex != null)
		{
			return new Color(hex.getRed(), hex.getGreen(), hex.getBlue(), 255);
		}

		switch (presetOrHex) {
		case "1":
			return config.presetColor1();
		case "2":
			return config.presetColor2();
		case "3":
			return config.presetColor3();
		case "4":
			return config.presetColor4();
		case "5":
			return config.presetColor5();
		}

		return color;
	}

	/**
	 * Returns fill color of either the config, a preset if selected, or the fill
	 * for a hex color suffix: the suffix color at the opacity from the suffix
	 * when it includes one, otherwise at the style fill color's opacity (or the
	 * default fill opacity when the style has no fill color)
	 *
	 * @return Color
	 */
	public Color getHighlightFillColor(String presetOrHex, Color color) {
		Color hex = parseHexColor(presetOrHex);
		if (hex != null)
		{
			int alpha;
			if (hasExplicitOpacity(presetOrHex))
			{
				alpha = hex.getAlpha();
			}
			else
			{
				alpha = color != null ? color.getAlpha() : DEFAULT_FILL_ALPHA;
			}

			return new Color(hex.getRed(), hex.getGreen(), hex.getBlue(), alpha);
		}

		switch (presetOrHex) {
		case "1":
			return config.presetFillColor1();
		case "2":
			return config.presetFillColor2();
		case "3":
			return config.presetFillColor3();
		case "4":
			return config.presetFillColor4();
		case "5":
			return config.presetFillColor5();
		}

		return color;
	}

	/**
	 * Color of the tag menu (ex. "Tag-Hull")
	 *
	 * @return Color
	 */
	public Color getTagColor() {
		if (config.tagStyleModeSet().contains(tagStyleMode.TILE))
		{
			return config.tileUseGlobalColor() ? config.globalTileColor() : config.tileColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.TRUE_TILE))
		{
			return config.trueTileUseGlobalColor() ? config.globalTileColor() : config.trueTileColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.SW_TILE))
		{
			return config.swTileUseGlobalColor() ? config.globalTileColor() : config.swTileColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.SW_TRUE_TILE))
		{
			return config.swTrueTileUseGlobalColor() ? config.globalTileColor() : config.swTrueTileColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.HULL))
		{
			return config.hullUseGlobalColor() ? config.globalTileColor() : config.hullColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.AREA))
		{
			return config.areaUseGlobalColor() ? config.globalTileColor() : config.areaColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.OUTLINE))
		{
			return config.outlineUseGlobalColor() ? config.globalTileColor() : config.outlineColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.CLICKBOX))
		{
			return config.clickboxUseGlobalColor() ? config.globalTileColor() : config.clickboxColor();
		}
		else
		{
			return Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F);
		}
	}

	/**
	 * Whether the NPC has a custom (non-slayer) highlight configured.
	 */
	public boolean hasCustomHighlight(NPCInfo n) {
		return (n.getTile().isHighlight() && config.tileHighlight())
				|| (n.getTrueTile().isHighlight() && config.trueTileHighlight())
				|| (n.getSwTile().isHighlight() && config.swTileHighlight())
				|| (n.getSwTrueTile().isHighlight() && config.swTrueTileHighlight())
				|| (n.getHull().isHighlight() && config.hullHighlight())
				|| (n.getArea().isHighlight() && config.areaHighlight())
				|| (n.getOutline().isHighlight() && config.outlineHighlight())
				|| (n.getClickbox().isHighlight() && config.clickboxHighlight());
	}

	/**
	 * Whether the slayer task highlight should be used for this NPC. The slayer
	 * highlight is used when the NPC is a slayer task and either deprioritization
	 * is disabled or the NPC has no custom highlight set.
	 */
	public boolean shouldUseSlayerHighlight(NPCInfo n) {
		return n.isTask() && config.slayerHighlight() && (!config.slayerDeprioritizeHighlight() || !hasCustomHighlight(n));
	}

	/**
	 * Resolves the color for a highlight style, preferring the slayer task color
	 * when the NPC is a task and otherwise using the provided custom color. Rave
	 * mode is applied to whichever color is selected.
	 */
	public Color resolveColor(boolean isTask, Color taskColor, Color customColor, boolean customRave, int customRaveSpeed) {
		return isTask
				? applyConfigOrRaveColor(taskColor, config.slayerRave(), config.slayerRaveSpeed())
				: applyConfigOrRaveColor(customColor, customRave, customRaveSpeed);
	}

	/**
	 * Returns the cycling rave color when rave mode is enabled, otherwise the
	 * base color.
	 */
	public Color applyConfigOrRaveColor(Color base, boolean raveEnabled, int raveSpeed) {
		return raveEnabled ? getRaveColor(raveSpeed) : base;
	}

	private Color getRaveColor(int speed) {
		int ticks = speed / 20;
		return Color.getHSBColor((client.getGameCycle() % ticks) / ((float) ticks), 1.0f, 1.0f);
	}
}