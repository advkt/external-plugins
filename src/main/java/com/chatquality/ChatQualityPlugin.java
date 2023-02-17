package com.chatquality;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatLineBuffer;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.MessageNode;
import net.runelite.api.ScriptID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ChatQualityPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatQualityConfig config;

	private Widget privateChat;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
		privateChat = client.getWidget(WidgetInfo.PRIVATE_CHAT_MESSAGE);
		/* 0-17 */
//		WidgetID.PRIVATE_CHAT
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

//	@Subscribe
//	public void onCallback(ScriptPostFired event) {
//
//
//		if (event.getScriptId() == ScriptID.SPLITPM_CHANGED) {
//
//			final int MSG_LENGTH = 4;
//			final Widget pmBox = client.getWidget(WidgetInfo.PRIVATE_CHAT_MESSAGE);
//			Widget[] children = pmBox.getChildren();
//			for (int i = 0; i < children.length; i+= MSG_LENGTH) {
//				Widget name = children[i];
//				Widget text = children[i+1];
//			}
//		}
//	}

//	@Subscribe
//	public void onScriptCallbackEvent(ScriptCallbackEvent event)
//	{
//		final String eventName = event.getEventName();
//
//		boolean wrap;
//		switch (eventName)
//		{
//			case "splitPrivChatUsernameColor":
//				wrap = false;
//				break;
////			case "privChatUsername":
////				wrap = true;
////				break;
//			case "chatMessageBuilding":
////				colorChatMessage();
//				return;
//			default:
//				return;
//		}
//		final String[] stringStack = client.getStringStack();
//		final int stringStackSize = client.getStringStackSize();
////		String fromToUsername = stringStack[stringStackSize - 1];
//		stringStack[stringStackSize - 1] = "";
//
////		if (event.getScript() == ScriptID.SPLITPM_CHANGED) {
////
////			final int MSG_LENGTH = 4;
////			final Widget pmBox = client.getWidget(WidgetInfo.PRIVATE_CHAT_MESSAGE);
////			assert pmBox != null;
////			Widget[] children = pmBox.getChildren();
////			if (children != null)
////			{
////				for (int i = 0; i < children.length; i += MSG_LENGTH)
////				{
////					Widget name = children[i];
////					Widget text = children[i + 1];
////				}
////			}
////		}
//	}

	final int MSG_LENGTH = 4;
	final ChatMessageType[] TYPES = {ChatMessageType.PRIVATECHAT, ChatMessageType.PRIVATECHATOUT, ChatMessageType.MODPRIVATECHAT};
	final Duration TIMEOUT_MSG = Duration.ofMinutes(1);

	@Getter
	private Instant timeoutCheckTime;
	private void createTimer(Duration duration)
	{
		removeTimer();
		timeoutCheckTime = Instant.now().plus(duration);

		if (duration.isNegative())
		{
			return;
		}
	}

	private void removeTimer()
	{
//		infoBoxManager.removeIf(t -> t instanceof AggressionTimer);
		timeoutCheckTime = null;
//		notifyOnce = false;
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event) {
		if (ScriptID.SPLITPM_CHANGED != event.getScriptId()) return;
		log.info("SPLITPM prefired");
	}
	@Subscribe(priority = 1)
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
//		"LOGINLOGOUTNOTIFICATION"
		if (!"chatMessageBuilding".equals(event.getEventName())) return;

//		clientThread.invoke(this::hideSplitPrivateChat);
//		clientThread.invokeLater(this::hideSplitPrivateChat);

		final int[] intStack = client.getIntStack();
		final String[] stringStack = client.getStringStack();
		final int size = client.getStringStackSize();
		final int isize = client.getIntStackSize();
		final int uid = intStack[isize - 1];
		final boolean splitpmbox = intStack[isize - 2] == 1;
		log.info("Chat build, split: {}", splitpmbox);
		if (!splitpmbox) return;

		final int MAX_ELEMENTS = 5;
		final Deque<Boolean> hideFlags = new ArrayDeque<>(MAX_ELEMENTS);
//		final Boolean[] hideFlags = {false, false, false, false, false};

		int overallCount = 0;
		int invIndex = 4;
		for (MessageNode message : client.getMessages())
		{
			overallCount++;

//	if (message.getType()
			ChatMessageType type = message.getType();
			if (Arrays.asList(TYPES).contains(type))
			{
//		log.info("{}: {}, {}, {}",
//			message.getName(), message.getValue(), message.getType(), message.getId());
//		log.info("{}", client.getMessages().get(message.getId()).getValue());
				Instant timestamp = Instant.ofEpochSecond(message.getTimestamp());
				Duration timeSince = Duration.between(timestamp, Instant.now());

				int compare = timeSince.compareTo(TIMEOUT_MSG);
				boolean timedOut = compare >= 0;
//				if (timedOut)
//				{
//					hideFlags[invIndex] = true;
//				}
				if (hideFlags.size() >= MAX_ELEMENTS) {
					hideFlags.remove();
				}
				hideFlags.add(timedOut);

				log.info("{} {}: {}, {}, {}/{}", timedOut ? "EXPIRED" : "",
					message.getName(), message.getValue(), message.getType(), message.getId(), invIndex);
//				invIndex--;
//		log.info("{}, {}", timeSince, compare);
			}
//	else {
//		log.info("{}: {}, {}", message.getName(), message.getValue(), message.getType());
//	}
		}

		log.info("Message Count: {}", overallCount);
		final Widget pmBox = client.getWidget(WidgetInfo.PRIVATE_CHAT_MESSAGE);
		assert pmBox != null;
		Widget[] children = pmBox.getChildren();

		clientThread.invokeLater(() -> {
		if (children != null)
		{

//			int offset = hideFlags.size() - finalInvIndex;
//			int start = hideFlags.length + (finalInvIndex - hideFlags.length);
//			int end = hideFlags.length - 1;
//			final Boolean[] invFlags = Arrays.copyOfRange(hideFlags, start, end);
			final Deque<Widget[]> hiddenMessages = new ArrayDeque<>(MAX_ELEMENTS);
			for (int i = 0, j = 0; i < children.length; i += MSG_LENGTH, j++)
			{
//				log.info("{}", (Object) hideFlags);

				Boolean hide = hideFlags.poll();
				if (hide == null) hide = false;
				Widget name = children[i];
				Widget text = children[i + 1];
//				Widget three = children[i + 2];
//				Widget four = children[i + 3];
				String userName = name.getText();
				String msgText = text.getText();

				log.info("#{}, hiding: {}, {}: {}", j, hide,userName,msgText);
//				log.info("#{}/{} {}: {}", j, j - invIndex - 1, userName, msgText);

				if (hide)
				{
					name.setHidden(true);
					text.setHidden(true);
					name.setOpacity(0);
					text.setOpacity(0);
					hiddenMessages.add(new Widget[]{name, text});
				} else {
					Widget[] msg = hiddenMessages.poll();
					if (msg != null) {
						msg[0].setText(userName).setOpacity(0).setHidden(false);;
						msg[1].setText(msgText).setOpacity(0).setHidden(false);;

						name.setHidden(true);
						text.setHidden(true);
						name.setOpacity(0);
						text.setOpacity(0);
					}
				}
			}
		}


//		log.info("Private message count: {}", msgCount);
	});
	}

	private void hideSplitPrivateChat() {
		final Widget pmBox = client.getWidget(WidgetInfo.PRIVATE_CHAT_MESSAGE);
		assert pmBox != null;
		Widget[] children = pmBox.getChildren();
		if (children != null)
		{
			for (int i = 0; i < children.length; i += MSG_LENGTH)
			{
				Widget name = children[i];
				Widget text = children[i + 1];
				name.setHidden(true);
				text.setHidden(true);
				name.setOpacity(0);
				text.setOpacity(0);
			}
		}
	}
//	@Subscribe
//	public void onScriptPostFired(ScriptPostFired event) {
//
//
//		if (event.getScriptId() == ScriptID.SPLITPM_CHANGED) {
//			log.info("SPLIT PM CHANGED");
//			final int MSG_LENGTH = 4;
//			final Widget pmBox = client.getWidget(WidgetInfo.PRIVATE_CHAT_MESSAGE);
//			assert pmBox != null;
//			Widget[] children = pmBox.getChildren();
//			if (children != null)
//			{
//				for (int i = 0; i < children.length; i += MSG_LENGTH)
//				{
//					Widget name = children[i];
//					Widget text = children[i + 1];
//					name.setHidden(true);
//					text.setHidden(true);
//				}
//			}
//		}
//	}
//	private void clearChatboxHistory(ChatboxTab tab)
//	{
//		if (tab == null)
//		{
//			return;
//		}
//
//		log.debug("Clearing chatbox history for tab {}", tab);
//
//		clearMessageQueue(tab);
//
//		if (tab.getAfter() == null)
//		{
//			// if the tab has a vanilla Clear option, it isn't necessary to delete the messages ourselves.
//			return;
//		}
//
//		boolean removed = false;
//		for (ChatMessageType msgType : tab.getMessageTypes())
//		{
//			final ChatLineBuffer lineBuffer = client.getChatLineMap().get(msgType.getType());
//			if (lineBuffer == null)
//			{
//				continue;
//			}
//
//			final MessageNode[] lines = lineBuffer.getLines().clone();
//			for (final MessageNode line : lines)
//			{
//				if (line != null)
//				{
//					lineBuffer.removeMessageNode(line);
//					removed = true;
//				}
//			}
//		}
//
//		if (removed)
//		{
//			// this rebuilds both the chatbox and the pmbox
//			clientThread.invoke(() -> client.runScript(ScriptID.SPLITPM_CHANGED));
//		}
//	}

//	@Subscribe
//	public void onGame
	@Provides
	ChatQualityConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChatQualityConfig.class);
	}
}
