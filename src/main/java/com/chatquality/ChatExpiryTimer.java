package com.chatquality;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.mta.alchemy.AlchemyRoomTimer;
import net.runelite.client.ui.overlay.infobox.Timer;
import net.runelite.client.util.ImageUtil;

public class ChatExpiryTimer extends Timer
{
	private static final int RESET_PERIOD = 1;
	final Duration TIMEOUT_MSG = Duration.ofMinutes(1);
	private static BufferedImage image;
	public ChatExpiryTimer(Plugin plugin)
	{
		super(RESET_PERIOD, ChronoUnit.MINUTES, getResetImage(), plugin);
	}

	private static BufferedImage getResetImage()
	{
		if (image != null)
		{
			return image;
		}

		image = ImageUtil.loadImageResource(ChatExpiryTimer.class, "/util/reset.png");

		return image;
	}
}
