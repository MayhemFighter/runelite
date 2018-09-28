
package net.runelite.client.plugins.zulrah;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.zulrah.overlays.ZulrahCurrentPhaseOverlay;
import net.runelite.client.plugins.zulrah.overlays.ZulrahNextPhaseOverlay;
import net.runelite.client.plugins.zulrah.overlays.ZulrahOverlay;
import net.runelite.client.plugins.zulrah.overlays.ZulrahPrayerOverlay;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPattern;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPatternA;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPatternB;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPatternC;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPatternD;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.QueryRunner;

@PluginDescriptor(
		name = "Zulrah",
		description = "Overlays to assist with killing Zulrah",
		tags = {"zulrah", "boss", "helper", "loudpacks"}
)

@Slf4j
public class ZulrahPlugin extends Plugin
{
	@Getter
	private NPC zulrah;

	@Inject
	private QueryRunner queryRunner;

	@Inject
	private Client client;

	@Inject
	private ZulrahConfig config;

	@Inject
	private ZulrahOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ZulrahCurrentPhaseOverlay currentPhaseOverlay;

	@Inject
	private ZulrahNextPhaseOverlay nextPhaseOverlay;

	@Inject
	private ZulrahPrayerOverlay zulrahPrayerOverlay;

	@Inject
	private ZulrahOverlay zulrahOverlay;



	@Provides
	ZulrahConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZulrahConfig.class);
	}

	private final ZulrahPattern[] patterns = new ZulrahPattern[]
			{
					new ZulrahPatternA(),
					new ZulrahPatternB(),
					new ZulrahPatternC(),
					new ZulrahPatternD()
			};

	private ZulrahInstance instance;

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(currentPhaseOverlay);
		overlayManager.add(nextPhaseOverlay);
		overlayManager.add(zulrahPrayerOverlay);
		overlayManager.add(zulrahOverlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(currentPhaseOverlay);
		overlayManager.remove(nextPhaseOverlay);
		overlayManager.remove(zulrahPrayerOverlay);
		overlayManager.remove(zulrahOverlay);
		zulrah = null;
		instance = null;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!config.enabled() || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (zulrah == null)
		{
			if (instance != null)
			{
				log.debug("Zulrah encounter has ended.");
				instance = null;
			}
			return;
		}

		if (instance == null)
		{
			instance = new ZulrahInstance(zulrah);
			log.debug("Zulrah encounter has started.");
		}

		ZulrahPhase currentPhase = ZulrahPhase.valueOf(zulrah, instance.getStartLocation());
		if (instance.getPhase() == null)
		{
			instance.setPhase(currentPhase);
		}
		else if (!instance.getPhase().equals(currentPhase))
		{
			ZulrahPhase previousPhase = instance.getPhase();
			instance.setPhase(currentPhase);
			instance.nextStage();

			log.debug("Zulrah phase has moved from {} -> {}, stage: {}", previousPhase, currentPhase, instance.getStage());
		}

		ZulrahPattern pattern = instance.getPattern();
		if (pattern == null)
		{
			int potential = 0;
			ZulrahPattern potentialPattern = null;

			for (ZulrahPattern p : patterns)
			{
				if (p.stageMatches(instance.getStage(), instance.getPhase()))
				{
					potential++;
					potentialPattern = p;
				}
			}

			if (potential == 1)
			{
				log.debug("Zulrah pattern identified: {}", potentialPattern);

				instance.setPattern(potentialPattern);
			}
		}
		else if (pattern.canReset(instance.getStage()) && (instance.getPhase() == null || instance.getPhase().equals(pattern.get(0))))
		{
			log.debug("Zulrah pattern has reset.");

			instance.reset();
		}
	}

	@Subscribe
	public void onNPCSpawn(NpcSpawned event)
	{
		try {
			NPC npc = event.getNpc();
			if (npc != null && npc.getName().toLowerCase().contains("zulrah"))
				zulrah = npc;
		} catch (Exception e) {

		}
	}

	@Subscribe
	public void onNPCDespawn(NpcDespawned event)
	{
		try {
			NPC npc = event.getNpc();
			if (npc != null && npc.getName().toLowerCase().contains("zulrah"))
				zulrah = null;
		} catch (Exception e) {

		}
	}

	public ZulrahInstance getInstance()
	{
		return instance;
	}
}
