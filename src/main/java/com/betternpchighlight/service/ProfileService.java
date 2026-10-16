package com.betternpchighlight.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import com.betternpchighlight.BetterNpcHighlightConfig;

import lombok.Value;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.config.ConfigManager;

/**
 * Converts the plugin settings to and from a plain text profile that can be shared with other players.
 *
 * A profile is a version header followed by one {@code keyName=value} line per setting. The value is the raw
 * config string encoded as unpadded URL-safe Base64, so names lists containing commas, colons or newlines
 * survive copy and paste. An empty value means the setting is unset, so RuneLite falls back to its default.
 */
public class ProfileService {
	static final String HEADER = "BETTER_NPC_HIGHLIGHT_PROFILE=1";

	@Inject
	private ConfigManager configManager;

	@Inject
	private BetterNpcHighlightConfig config;

	/**
	 * Exports every setting, including unset ones as empty values, so importing the profile reproduces the
	 * exporter's settings exactly rather than merging them into the importer's.
	 */
	public String exportProfile() {
		StringBuilder profile = new StringBuilder(HEADER).append('\n');
		for (String key : settingKeys())
		{
			String value = configManager.getConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, key);
			profile.append(key).append('=').append(value == null ? "" : encode(value)).append('\n');
		}
		return profile.toString();
	}

	/**
	 * Parses and validates the whole profile before anything is written, so a malformed profile changes nothing.
	 * Settings this version of the plugin does not recognise (for example from a newer version) are skipped.
	 */
	public ImportResult importProfile(String profile) {
		if (profile == null || profile.trim().isEmpty())
		{
			return new ImportResult(false, "Paste a profile into the box first.");
		}

		String[] lines = profile.trim().split("\r?\n");
		if (!HEADER.equals(lines[0].trim()))
		{
			return new ImportResult(false, "This is not a Better NPC Highlight profile.");
		}

		Set<String> knownKeys = settingKeys();
		Map<String, String> values = new LinkedHashMap<>();
		List<Integer> badLines = new ArrayList<>();
		int skipped = 0;
		for (int i = 1; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.isEmpty())
			{
				continue;
			}

			int separator = line.indexOf('=');
			if (separator <= 0)
			{
				badLines.add(i + 1);
				continue;
			}

			String key = line.substring(0, separator);
			if (!knownKeys.contains(key))
			{
				skipped++;
				continue;
			}

			try
			{
				values.put(key, decode(line.substring(separator + 1)));
			}
			catch (IllegalArgumentException e)
			{
				badLines.add(i + 1);
			}
		}

		if (!badLines.isEmpty())
		{
			String more = badLines.size() > 1 ? " and " + (badLines.size() - 1) + " more" : "";
			return new ImportResult(false, "Line " + badLines.get(0) + more + " could not be read. Nothing was imported.");
		}
		if (values.isEmpty())
		{
			return new ImportResult(false, "The profile does not contain any settings.");
		}

		for (Map.Entry<String, String> entry : values.entrySet())
		{
			if (entry.getValue().isEmpty())
			{
				configManager.unsetConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, entry.getKey());
			}
			else
			{
				configManager.setConfiguration(BetterNpcHighlightConfig.CONFIG_GROUP, entry.getKey(), entry.getValue());
			}
		}

		String message = "Imported " + values.size() + " settings.";
		if (skipped > 0)
		{
			message += " Skipped " + skipped + " unrecognised.";
		}
		return new ImportResult(true, message);
	}

	/**
	 * Key names of every visible config item, sorted so exports are stable and easy to compare. Hidden items are
	 * internal (e.g. the deprecated tagStyleMode kept for migration) and are not part of a profile.
	 */
	private Set<String> settingKeys() {
		Set<String> keys = new TreeSet<>();
		for (ConfigItemDescriptor item : configManager.getConfigDescriptor(config).getItems())
		{
			if (!item.getItem().hidden())
			{
				keys.add(item.key());
			}
		}
		return keys;
	}

	private static String encode(String value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String decode(String value) {
		return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
	}

	@Value
	public static class ImportResult {
		boolean success;
		String message;
	}
}
