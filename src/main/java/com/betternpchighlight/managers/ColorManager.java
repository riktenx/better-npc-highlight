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
	 * Returns color of either the config or a preset if selected
	 *
	 * @return Color
	 */
	public Color getHighlightColor(String preset, Color color) {
		switch (preset) {
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
	 * Returns fill color of either the config or a preset if selected
	 *
	 * @return Color
	 */
	public Color getHighlightFillColor(String preset, Color color) {
		switch (preset) {
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
		if (config.useGlobalTileColor())
		{
			return config.globalTileColor();
		}
		if (config.tagStyleModeSet().contains(tagStyleMode.TILE))
		{
			return config.tileColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.TRUE_TILE))
		{
			return config.trueTileColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.SW_TILE))
		{
			return config.swTileColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.SW_TRUE_TILE))
		{
			return config.swTrueTileColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.HULL))
		{
			return config.hullColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.AREA))
		{
			return config.areaColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.OUTLINE))
		{
			return config.outlineColor();
		}
		else if (config.tagStyleModeSet().contains(tagStyleMode.CLICKBOX))
		{
			return config.clickboxColor();
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