package main.reachhud.hud.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;

import java.util.Locale;

public final class ReachHudMeleeRenderer {

    private ReachHudMeleeRenderer() {
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            Entity target,
            double distance,
            boolean withinReach,
            double alpha
    ) {
        if (target == null || alpha <= 0.001) {
            return;
        }

        String targetName =
                target.getName().getString();

        String distanceText =
                String.format(
                        Locale.ROOT,
                        "%.2f",
                        distance
                );

        int targetColor =
                ReachHudRenderUtils.getTargetNameColor(
                        target
                );

        renderHud(
                graphics,
                client,
                targetName,
                distanceText,
                withinReach,
                targetColor,
                alpha
        );
    }

    public static void updateCache(
            Entity target,
            double distance,
            boolean withinReach
    ) {
        if (target == null) {
            return;
        }

        ReachHudRenderState.cachedMeleeTargetName =
                target.getName().getString();

        ReachHudRenderState.cachedMeleeDistanceText =
                String.format(
                        Locale.ROOT,
                        "%.2f",
                        distance
                );

        ReachHudRenderState.cachedMeleeWithinReach =
                withinReach;

        ReachHudRenderState.cachedMeleeTargetColor =
                ReachHudRenderUtils.getTargetNameColor(
                        target
                );
    }

    public static void renderCached(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            double alpha
    ) {
        if (ReachHudRenderState.cachedMeleeTargetName.isEmpty()
                || ReachHudRenderState.cachedMeleeDistanceText.isEmpty()) {
            return;
        }

        renderHud(
                graphics,
                client,
                ReachHudRenderState.cachedMeleeTargetName,
                ReachHudRenderState.cachedMeleeDistanceText,
                ReachHudRenderState.cachedMeleeWithinReach,
                ReachHudRenderState.cachedMeleeTargetColor,
                alpha
        );
    }

    private static void renderHud(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            String targetName,
            String distanceText,
            boolean withinReach,
            int targetNameColor,
            double alpha
    ) {
        String reachLabel =
                "Reach: ";

        int targetNameWidth =
                client.font.width(targetName);

        int reachLabelWidth =
                client.font.width(reachLabel);

        int distanceWidth =
                client.font.width(distanceText);

        int reachTotalWidth =
                reachLabelWidth + distanceWidth;

        int screenWidth =
                client.getWindow().getGuiScaledWidth();

        int screenHeight =
                client.getWindow().getGuiScaledHeight();

        int centerX =
                screenWidth / 2;

        int centerY =
                screenHeight / 2;

        int targetY =
                centerY + 11;

        int reachY =
                centerY + 23;

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

        int boxX =
                reachX - paddingHorizontal;

        int boxY =
                reachY - 2;

        int boxWidth =
                reachTotalWidth
                        + paddingHorizontal * 2;

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

        graphics.fill(
                boxX + 2,
                boxY + boxHeight - 1,
                boxX + boxWidth - 2,
                boxY + boxHeight,
                accentColor
        );

        int indicatorColor =
                ReachHudRenderUtils.applyAlpha(
                        targetNameColor,
                        alpha
                );

        graphics.fill(
                targetX - 6,
                targetY + 4,
                targetX - 3,
                targetY + 7,
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