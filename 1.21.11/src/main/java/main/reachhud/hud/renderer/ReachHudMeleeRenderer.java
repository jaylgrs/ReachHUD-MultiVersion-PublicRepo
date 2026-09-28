package main.reachhud.hud.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

final class ReachHudMeleeRenderer {

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

        int targetNameWidth = client.font.width(targetName);
        int reachLabelWidth = client.font.width(reachLabel);
        int distanceWidth = client.font.width(distanceText);

        int reachTotalWidth =
                reachLabelWidth + distanceWidth;

        int screenWidth =
                client.getWindow().getGuiScaledWidth();

        int screenHeight =
                client.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + 11;
        int reachY = centerY + 23;

        int targetX =
                centerX - targetNameWidth / 2;

        int reachX =
                centerX - reachTotalWidth / 2;

        int distanceColor =
                withinReach
                        ? 0xFF55FF55
                        : 0xFFFF5555;

        int fadedTargetColor =
                ReachHudRenderUtils.applyAlpha(
                        targetNameColor,
                        alpha
                );

        int fadedDistanceColor =
                ReachHudRenderUtils.applyAlpha(
                        distanceColor,
                        alpha
                );

        int fadedLabelColor =
                ReachHudRenderUtils.applyAlpha(
                        0xFFCCCCCC,
                        alpha
                );

        int paddingHorizontal = 6;

        int boxX = reachX - paddingHorizontal;
        int boxY = reachY - 2;

        int boxWidth =
                reachTotalWidth + paddingHorizontal * 2;

        int boxHeight = 12;

        int backgroundColor =
                ReachHudRenderUtils.applyAlpha(
                        0x99000000,
                        alpha
                );

        ReachHudRenderUtils.drawRoundedBox(
                graphics,
                boxX,
                boxY,
                boxWidth,
                boxHeight,
                backgroundColor
        );

        int accentColor =
                withinReach
                        ? 0xFF55FF55
                        : 0xFFFF5555;

        accentColor =
                ReachHudRenderUtils.applyAlpha(
                        accentColor,
                        alpha
                );

        int accentX = boxX + 2;
        int accentY = boxY + boxHeight - 1;
        int accentWidth = boxWidth - 4;

        graphics.fill(
                accentX,
                accentY,
                accentX + accentWidth,
                accentY + 1,
                accentColor
        );

        int indicatorColor =
                ReachHudRenderUtils.applyAlpha(
                        targetNameColor,
                        alpha
                );

        int indicatorSize = 3;

        int indicatorX = targetX - 6;
        int indicatorY = targetY + 4;

        graphics.fill(
                indicatorX,
                indicatorY,
                indicatorX + indicatorSize,
                indicatorY + indicatorSize,
                indicatorColor
        );

        ReachHudRenderUtils.drawText(
                graphics,
                client,
                targetName,
                targetX,
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