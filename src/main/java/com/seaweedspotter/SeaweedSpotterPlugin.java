package com.seaweedspotter;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.ItemDespawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Seaweed Spore Spotter"
)
public class SeaweedSpotterPlugin extends Plugin
{

	static int BUBBLES = 719;
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private SeaweedSpotterConfig config;

	@Inject
	private SeaweedSpotterOverlay overlay;

	@Inject
	private Notifier notifier;

	@Getter(AccessLevel.PACKAGE)
	private final Map<WorldPoint, SporeItem> collectedSporeItems = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		log.info("Seaweed Spore highlighter started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		log.info("Seaweed Spore highlighter stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			collectedSporeItems.clear();
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		if (event.getGraphicsObject().getId() == BUBBLES)
		{
			LocalPoint location = event.getGraphicsObject().getLocation();
			Tile tile = client.getScene()
				.getTiles()[client.getPlane()][location.getSceneX()][location.getSceneY()];
			List<TileItem> items = tile.getGroundItems();

			for (TileItem item : items)
			{
				if (item.getId() == ItemID.SEAWEED_SPORE)
				{

					final int quantity = item.getQuantity();
					SporeItem spore = new SporeItem(quantity, tile.getWorldLocation(), tile.getItemLayer().getHeight(), Instant.now());
					collectedSporeItems.put(spore.getLocation(), spore);
					notifySporeSpawn(quantity);
				}
			}
		}
	}

	private void notifySporeSpawn(int quantity) {
		int notifyQuantity = config.minNotifyQuantity();
		if (notifyQuantity != 0 && notifyQuantity <= quantity)
		{
			StringBuilder msg = new StringBuilder();
			String prefix;
			String suffix;
			if (quantity == 1)
			{
				prefix = "A";
				suffix = "";
			}
			else
			{
				prefix = "" + quantity;
				suffix = "s";
			}
			msg.append(prefix)
				.append(" seaweed spore")
				.append(suffix)
				.append(" appeared.");

			notifier.notify(msg.toString());
		}
	}
	@Subscribe
	private void onItemDespawned(ItemDespawned event)
	{

		TileItem item = event.getItem();
		if (item.getId() != ItemID.SEAWEED_SPORE)
		{
			return;
		}
		Tile tile = event.getTile();

		SporeItem seaweed = collectedSporeItems.get(tile.getWorldLocation());
		if (seaweed == null)
		{
			return;
		}

		if (seaweed.getQuantity() <= item.getQuantity())
		{
			collectedSporeItems.remove(tile.getWorldLocation());
		}
		else
		{
			seaweed.setQuantity(seaweed.getQuantity() - item.getQuantity());
			// When picking up an item when multiple stacks appear on the ground,
			// it is not known which item is picked up, so we invalidate the spawn
			// time
			seaweed.setSpawnTime(null);
		}
	}

	@Provides
	SeaweedSpotterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SeaweedSpotterConfig.class);
	}
}
