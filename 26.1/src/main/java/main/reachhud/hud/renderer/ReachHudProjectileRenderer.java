package main.reachhud.hud.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;

import java.util.Locale;

public final class ReachHudProjectileRenderer {

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

        ReachHudRenderState.cachedProjectileWillHit =
                willHit;

        ReachHudRenderState.cachedProjectileTargetColor =
                ReachHudRenderUtils.getTargetNameColor(
                        target
                );
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
        String aimLabel =
                "Aim: ";

        int targetNameWidth =
                client.font.width(targetName);

        int aimLabelWidth =
                client.font.width(aimLabel);

        int distanceWidth =
                client.font.width(distanceText);

        int aimTotalWidth =
                aimLabelWidth + distanceWidth;

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

        int aimY =
                centerY + 23;

        int targetX =
                centerX - targetNameWidth / 2;

        int aimX =
                centerX - aimTotalWidth / 2;

        int distanceColor =
                willHit
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
                aimX - paddingHorizontal;

        int boxY =
                aimY - 2;

        int boxWidth =
                aimTotalWidth
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
                willHit
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
                aimLabel,
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