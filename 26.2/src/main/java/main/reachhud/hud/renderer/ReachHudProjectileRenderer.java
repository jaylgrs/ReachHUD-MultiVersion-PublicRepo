package main.reachhud.hud.renderer;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;

public final class ReachHudProjectileRenderer {

    private static final String INDICATOR = "● ";
    private static final String AIM_LABEL = "Aim: ";

    private static final int TARGET_Y_OFFSET = 11;
    private static final int AIM_Y_OFFSET = 23;

    private ReachHudProjectileRenderer() {
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            Entity target,
            double distance,
            boolean willHit,
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
                willHit,
                targetColor,
                alpha
        );
    }

    public static void updateCache(
            Entity target,
            double distance,
            boolean willHit
    ) {
        if (target == null) {
            return;
        }

        ReachHudRenderState.cachedProjectileTargetName =
                target.getName().getString();

        ReachHudRenderState.cachedProjectileDistanceText =
                String.format(
                        Locale.ROOT,
                        "%.2f",
                        distance
                );

        ReachHudRenderState.cachedProjectileWillHit = willHit;

        ReachHudRenderState.cachedProjectileTargetColor =
                ReachHudRenderUtils.getTargetNameColor(target);
    }

    public static void renderCached(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            double alpha
    ) {
        if (ReachHudRenderState.cachedProjectileTargetName.isEmpty()
                || ReachHudRenderState.cachedProjectileDistanceText.isEmpty()) {
            return;
        }

        renderHud(
                graphics,
                client,
                ReachHudRenderState.cachedProjectileTargetName,
                ReachHudRenderState.cachedProjectileDistanceText,
                ReachHudRenderState.cachedProjectileWillHit,
                ReachHudRenderState.cachedProjectileTargetColor,
                alpha
        );
    }

    private static void renderHud(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            String targetName,
            String distanceText,
            boolean willHit,
            int targetNameColor,
            double alpha
    ) {
        int indicatorWidth = client.font.width(INDICATOR);
        int targetNameWidth = client.font.width(targetName);
        int aimLabelWidth = client.font.width(AIM_LABEL);
        int distanceWidth = client.font.width(distanceText);

        int targetTotalWidth =
                indicatorWidth + targetNameWidth;

        int aimTotalWidth =
                aimLabelWidth + distanceWidth;

        int screenWidth =
                client.getWindow().getGuiScaledWidth();

        int screenHeight =
                client.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + TARGET_Y_OFFSET;
        int aimY = centerY + AIM_Y_OFFSET;

        int targetX =
                centerX - targetTotalWidth / 2;

        int aimX =
                centerX - aimTotalWidth / 2;

        int distanceColor =
                willHit
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
                AIM_LABEL,
                aimX,
                aimY,
                fadedLabelColor,
                alpha
        );

        ReachHudRenderUtils.drawText(
                graphics,
                client,
                distanceText,
                aimX + aimLabelWidth,
                aimY,
                fadedDistanceColor,
                alpha
        );
    }
}