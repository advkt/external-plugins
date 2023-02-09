package com.seaweedspotter;

import java.awt.Color;
import java.util.Locale;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(SeaweedSpotterConfig.GROUP)
public interface SeaweedSpotterConfig extends Config
{
	final static String GROUP = "seaweedspotter";
	@ConfigItem(
		keyName = "notifyQuantity",
		name = "Notify quantity",
		description = "The minimum number of Spores to be notified",
		position = 0
	)
	@Range(min = 0, max = 3)
	default int minNotifyQuantity()
	{
		return 1;
	}

	@ConfigSection(
		name = "Markers",
		description = "Marker configuration",
		position = 1
	)
	String markerSection = "markerStates";

	@ConfigItem(
		keyName = "despawnIndicator",
		name = "Despawn Indicator",
		description = "Toggle despawn indicator",
		section = markerSection
	)
	default boolean despawnIndicator()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tileMarker",
		name = "Tile",
		description = "Toggle tile marker",
		position = 1,
		section = markerSection
	)
	default boolean tileMarker()
	{
		return true;
	}

	@ConfigItem(
		keyName = "minimapMarker",
		name = "Minimap",
		description = "Toggle minimap marker",
		position = 3,
		section = markerSection
	)
	default boolean minimapMarker()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "tileColour",
		name = "Tile marker",
		description = "Configures the colour for highlighted tiles",
		position = 2,
		section = markerSection
	)
	default Color tileColour()
	{
		return new Color(75, 190, 45, 128);
	}

	@Alpha
	@ConfigItem(
		keyName = "minimapColour",
		name = "Minimap marker",
		description = "Configures the color for minimap markers",
		position = 4,
		section = markerSection
	)
	default Color minimapColour()
	{
		return Color.GREEN;
	}
}
