package com.seaweedspotter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SeaweedSpotterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SeaweedSpotterPlugin.class);
		RuneLite.main(args);
	}
}