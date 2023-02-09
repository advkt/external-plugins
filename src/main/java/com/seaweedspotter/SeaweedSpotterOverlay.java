package com.seaweedspotter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

public class SeaweedSpotterOverlay extends Overlay
{
	private static final int OFFSET_Z = 20;
	private static final Duration SPORE_DESPAWN = Duration.ofSeconds(30);
	private static final int TIMER_OVERLAY_DIAMETER = 10;


	private final Client client;
	private final SeaweedSpotterPlugin plugin;

	private final SeaweedSpotterConfig config;

	private final ProgressPieComponent progressPieComponent = new ProgressPieComponent();

	@Inject
	private SeaweedSpotterOverlay(Client client, SeaweedSpotterPlugin plugin, SeaweedSpotterConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}
	@Override
	public Dimension render(Graphics2D graphics)
	{
		Collection<SporeItem> sporeList = plugin.getCollectedSporeItems().values();
		for (SporeItem spore : sporeList)
		{
			final LocalPoint groundPoint = LocalPoint.fromWorld(client, spore.getLocation());
			if (groundPoint != null)
			{
				if (config.despawnIndicator()) drawTimerPieOverlay(graphics, groundPoint, spore);
				if (config.tileMarker()) markTile(graphics, groundPoint, spore.getHeight(), config.tileColour());
				if (config.minimapMarker()) markMinimap(graphics, groundPoint, config.minimapColour());
			}
		}

		return null;
	}

	private Instant calculateDespawnTime(SporeItem spore) {
		Instant spawnTime = spore.getSpawnTime();
		if (spawnTime == null)
		{
			return null;
		}
		final Instant despawnTime;

		despawnTime = spawnTime.plus(SPORE_DESPAWN);
		return despawnTime;
	}

	private void markTile(Graphics2D graphics, LocalPoint groundPoint, int height, Color color) {
		final Polygon poly = Perspective.getCanvasTilePoly(client, groundPoint, height);

		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color);
		}
	}

	private void markMinimap(Graphics2D graphics, LocalPoint groundPoint, Color color) {
		Point minimapLocation = Perspective.localToMinimap(client, groundPoint);
		if (minimapLocation != null)
		{
			OverlayUtil.renderMinimapLocation(graphics, minimapLocation, color);
		}
	}


	private void drawTimerPieOverlay(Graphics2D graphics, int x, int y, SporeItem groundItem)
	{
		Instant now = Instant.now();
		Instant spawnTime = groundItem.getSpawnTime();
		Instant despawnTime = calculateDespawnTime(groundItem);
		Color fillColor = Color.GREEN;

		if (spawnTime == null || despawnTime == null || fillColor == null)
		{
			return;
		}

		float percent = (float) (now.toEpochMilli() - spawnTime.toEpochMilli()) / (despawnTime.toEpochMilli() - spawnTime.toEpochMilli());

		progressPieComponent.setDiameter(TIMER_OVERLAY_DIAMETER);

		progressPieComponent.setPosition(new Point(x, y));
		progressPieComponent.setFill(fillColor);
		progressPieComponent.setBorderColor(fillColor);
		progressPieComponent.setProgress(1 - percent); // inverse so pie drains over time
		progressPieComponent.render(graphics);
	}

	private void drawTimerPieOverlay(Graphics2D graphics, Point point, SporeItem groundItem) {
		drawTimerPieOverlay(graphics, point.getX(), point.getY(), groundItem);
	}
	private void drawTimerPieOverlay(Graphics2D graphics, LocalPoint groundPoint, SporeItem groundItem) {

		Point point = Perspective.localToCanvas(client,
			groundPoint, client.getPlane(), groundItem.getHeight() - OFFSET_Z);

		if (point != null) drawTimerPieOverlay(graphics, point, groundItem);
	}
}
