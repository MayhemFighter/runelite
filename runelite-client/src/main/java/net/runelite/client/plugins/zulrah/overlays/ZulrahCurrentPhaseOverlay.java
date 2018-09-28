
package net.runelite.client.plugins.zulrah.overlays;

import net.runelite.client.plugins.zulrah.ZulrahInstance;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.client.plugins.zulrah.ImagePanelComponent;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ZulrahCurrentPhaseOverlay extends Overlay
{
	private final ZulrahPlugin plugin;
	private final PanelComponent imagePanelComponent = new PanelComponent();

	@Inject
	ZulrahCurrentPhaseOverlay(ZulrahPlugin plugin)
	{
		setPosition(OverlayPosition.BOTTOM_RIGHT);
		setPriority(OverlayPriority.HIGH);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		ZulrahInstance instance = plugin.getInstance();

		if (instance == null)
		{
			return null;
		}

		ZulrahPhase currentPhase = instance.getPhase();
		if (currentPhase == null)
		{
			return null;
		}

		String pattern = instance.getPattern() != null ? instance.getPattern().toString() : "Unknown";
		String title = currentPhase.isJad() ? "JAD PHASE" : pattern;
		Color backgroundColor = currentPhase.getColor();
		BufferedImage zulrahImage = ZulrahImageManager.getZulrahBufferedImage(currentPhase.getType());
		ImagePanelComponent imagePanelComponent = new ImagePanelComponent();
		imagePanelComponent.setTitle(title);
		imagePanelComponent.setBackgroundColor(backgroundColor);
		imagePanelComponent.setImage(zulrahImage);
		return imagePanelComponent.render(graphics);
	}
}
