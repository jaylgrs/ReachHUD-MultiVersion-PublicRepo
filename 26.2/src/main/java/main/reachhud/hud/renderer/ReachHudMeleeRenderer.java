package main.reachhud.hud.renderer;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;

public final class ReachHudMeleeRenderer {

    private static final String INDICATOR = "● ";
    private static final String REACH_LABEL = "Reach: ";

    private static final int TARGET_Y_OFFSET = 11;
    private static final int REACH_Y_OFFSET = 23;

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

        String targetName = target.getName().getString();

        String distanceText = String.format(
                Locale.ROOT,
                "%.2f",
                distance
        );

        int targetColor =
                ReachHudRenderUtils.getTargetNameColor(target);

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
                ReachHudRenderUtils.getTargetNameColor(target);
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
        int indicatorWidth = client.font.width(INDICATOR);
        int targetNameWidth = client.font.width(targetName);
        int reachLabelWidth = client.font.width(REACH_LABEL);
        int distanceWidth = client.font.width(distanceText);

        int targetTotalWidth =
                indicatorWidth + targetNameWidth;

        int reachTotalWidth =
                reachLabelWidth + distanceWidth;

        int screenWidth =
                client.getWindow().getGuiScaledWidth();

        int screenHeight =
                client.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + TARGET_Y_OFFSET;
        int reachY = centerY + REACH_Y_OFFSET;

        int targetX =
                centerX - targetTotalWidth / 2;

        int reachX =
                centerX - reachTotalWidth / 2;

        int distanceColor =
                withinReach
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
                REACH_LABEL,
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