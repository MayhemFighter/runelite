
package net.runelite.client.plugins.zulrah.overlays;

import net.runelite.client.plugins.zulrah.ZulrahInstance;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.client.plugins.zulrah.ImagePanelComponent;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ZulrahNextPhaseOverlay extends Overlay
{
	private final ZulrahPlugin plugin;

	@Inject
	ZulrahNextPhaseOverlay(ZulrahPlugin plugin)
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

		ZulrahPhase nextPhase = instance.getNextPhase();
		if (nextPhase == null)
		{
			return null;
		}

		Color backgroundColor = nextPhase.getColor();
		BufferedImage zulrahImage = ZulrahImageManager.getSmallZulrahBufferedImage(nextPhase.getType());
		ImagePanelComponent imagePanelComponent = new ImagePanelComponent();
		imagePanelComponent.setTitle("Next");
		imagePanelComponent.setBackgroundColor(backgroundColor);
		imagePanelComponent.setImage(zulrahImage);
		return imagePanelComponent.render(graphics);
	}
}
