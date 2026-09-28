package main.reachhud.hud.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

final class ReachHudMeleeRenderer {

    private static final String INDICATOR = "● ";
    private static final int TARGET_Y_OFFSET = 11;
    private static final int REACH_Y_OFFSET = 23;

    private ReachHudMeleeRenderer() {
    }

    static void render(
            GuiGraphics graphics,
            Minecraft client,
            String targetName,
            String reachLabel,
            String distanceText,
            boolean withinReach,
            int targetNameColor,
            double alpha
    ) {
        if (alpha <= 0.001) {
            return;
        }

        int indicatorWidth = client.font.width(INDICATOR);
        int targetNameWidth = client.font.width(targetName);
        int reachLabelWidth = client.font.width(reachLabel);
        int distanceWidth = client.font.width(distanceText);

        int targetTotalWidth = indicatorWidth + targetNameWidth;
        int reachTotalWidth = reachLabelWidth + distanceWidth;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + TARGET_Y_OFFSET;
        int reachY = centerY + REACH_Y_OFFSET;

        int targetX = centerX - targetTotalWidth / 2;
        int reachX = centerX - reachTotalWidth / 2;

        int distanceColor = withinReach
                ? 0xFF55FF55
                : 0xFFFF5555;

        int fadedIndicatorColor =
                ReachHudRenderUtils.applyAlpha(
                        targetNameColor,
                        alpha
                );

        int fadedTargetColor =
                ReachHudRenderUtils.applyAlpha(
                        0xFFFFFFFF,
                        alpha
                );

        int fadedLabelColor =
                ReachHudRenderUtils.applyAlpha(
                        0xFFCCCCCC,
                        alpha
                );

        int fadedDistanceColor =
                ReachHudRenderUtils.applyAlpha(
                        distanceColor,
                        alpha
                );

        ReachHudRenderUtils.drawText(
                graphics,
                client,
                INDICATOR,
                targetX,
                targetY,
                fadedIndicatorColor,
                alpha
        );

        ReachHudRenderUtils.drawText(
                graphics,
                client,
                targetName,
                targetX + indicatorWidth,
                targetY,
                fadedTargetColor,
                alpha
        );

        ReachHudRenderUtils.drawText(
                graphics,
                client,
                reachLabel,
                reachX,
                reachY,
                fadedLabelColor,
                alpha
        );

        ReachHudRenderUtils.drawText(
                graphics,
                client,
                distanceText,
                reachX + reachLabelWidth,
                reachY,
                fadedDistanceColor,
                alpha
        );
    }
}