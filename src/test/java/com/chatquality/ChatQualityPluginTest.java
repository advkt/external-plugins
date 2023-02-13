package com.chatquality;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChatQualityPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ChatQualityPlugin.class);
		RuneLite.main(args);
	}
}