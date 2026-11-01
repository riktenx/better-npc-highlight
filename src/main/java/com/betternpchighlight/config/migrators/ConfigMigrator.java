package com.betternpchighlight.config.migrators;

import java.util.Set;

import com.betternpchighlight.BetterNpcHighlightConfig;
import com.betternpchighlight.BetterNpcHighlightConfig.tagStyleMode;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
public final class ConfigMigrator {
  public static void migrateOldConfigs(ConfigManager configManager) {
  	migrateTagStyleMode(configManager);
  	migrateUseGlobalTileColor(configManager);
  }

  // migrate old tag style mode config into the new set version of config if possible
  private static void migrateTagStyleMode(ConfigManager configManager) {
  	try
  	{
  		String oldValue = configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "tagStyleMode");
  		String newValue = configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "tagStyleModeSet");
  		if (oldValue != null && newValue == null)
  		{
  			log.debug("BNPC: Migrating old tag style mode config to new set version");

			// Parse the old enum string value
			tagStyleMode oldMode = tagStyleMode.valueOf(oldValue.toUpperCase());

			// Convert to Set format
			Set<tagStyleMode> newModeSet = Set.of(oldMode);

  			// Save the properly serialized Set to the new config
  			configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "tagStyleModeSet", newModeSet);
  		}
  	}
  	catch (Exception e)
  	{
  		// Could not migrate, ignore
  		log.debug("BNPC: Could not migrate old tag style mode config to new set version", e);
  	}
  }

  // migrate the old single global tile color toggle into the per-style Use Global Color toggles
  private static void migrateUseGlobalTileColor(ConfigManager configManager) {
  	try
  	{
  		String oldValue = configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "useGlobalTileColor");
  		String newValue = configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "tileUseGlobalColor");
  		if (oldValue != null && newValue == null)
  		{
  			log.debug("BNPC: Migrating old global tile color toggle to per-style toggles");

			// Apply the old setting to every style the old toggle affected
  			boolean useGlobal = Boolean.parseBoolean(oldValue);
  			configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "tileUseGlobalColor", useGlobal);
  			configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "trueTileUseGlobalColor", useGlobal);
  			configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "swTileUseGlobalColor", useGlobal);
  			configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, "swTrueTileUseGlobalColor", useGlobal);
  		}
  	}
  	catch (Exception e)
  	{
  		// Could not migrate, ignore
  		log.debug("BNPC: Could not migrate old global tile color toggle to per-style toggles", e);
  	}
  }
}
