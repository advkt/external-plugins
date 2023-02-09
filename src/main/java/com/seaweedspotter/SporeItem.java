package com.seaweedspotter;

import java.time.Instant;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@Builder
class SporeItem
{
	private int quantity;
	private WorldPoint location;
	private int height;

	@Nullable
	private Instant spawnTime;

}
