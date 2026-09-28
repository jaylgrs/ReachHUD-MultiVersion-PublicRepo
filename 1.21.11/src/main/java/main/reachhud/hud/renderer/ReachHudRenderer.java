package main.reachhud.hud.renderer;

import java.util.Locale;

import main.reachhud.ReachHUD;
import main.reachhud.hud.ReachHudNotification;
import main.reachhud.input.ReachHudKeybind;
import main.reachhud.projectile.ProjectileAimTracker;
import main.reachhud.reach.ReachCalculator;
import main.reachhud.target.TargetTracker;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;

public final class ReachHudRenderer {

    private static final double SMOOTH_SPEED = 0.25;
    private static final double FADE_SPEED = 0.75;

    private static final String REACH_LABEL = "Reach: ";
    private static final String AIM_LABEL = "Aim: ";

    private ReachHudRenderer() {
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                ReachHUD.id("reach_hud"),
                ReachHudRenderer::render
        );
    }

    private static void render(
            GuiGraphics graphics,
            DeltaTracker deltaTracker
    ) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.options.hideGui) {
            resetSmoothing();
            return;
        }

        if (client.screen != null) {
            resetSmoothing();
            return;
        }

        double deltaTicks = deltaTracker.getRealtimeDeltaTicks();

        renderNotification(graphics, client);

        if (!ReachHudKeybind.isEnabled()) {
            updateHudAlpha(false, deltaTicks);
            renderCachedHud(graphics, client);
            resetSmoothingValuesOnly();
            return;
        }

        if (ProjectileAimTracker.hasTarget()) {
            renderProjectileHud(graphics, client, deltaTicks);
            return;
        }

        resetProjectileSmoothing();

        Entity target = TargetTracker.getCurrentTarget();

        if (target == null) {
            hideMeleeHud(graphics, client, deltaTicks);
            return;
        }

        double distance = ReachCalculator.getDistanceTo(target);
        double reach = ReachCalculator.getPlayerReach();

        if (distance < 0 || reach < 0) {
            hideMeleeHud(graphics, client, deltaTicks);
            return;
        }

        if (distance > reach + 3.0) {
            hideMeleeHud(graphics, client, deltaTicks);
            return;
        }

        boolean withinReach = distance <= reach;

        updateSmoothedDistance(target, distance, deltaTicks);

        String targetName = target.getName().getString();
        String distanceText = formatDistance(
                ReachHudRenderState.displayedDistance
        );

        int targetNameColor =
                ReachHudRenderUtils.getTargetNameColor(target);

        ReachHudRenderState.cachedMeleeTargetName = targetName;
        ReachHudRenderState.cachedMeleeDistanceText = distanceText;
        ReachHudRenderState.cachedMeleeWithinReach = withinReach;
        ReachHudRenderState.cachedMeleeTargetColor = targetNameColor;

        updateHudAlpha(true, deltaTicks);

        ReachHudMeleeRenderer.render(
                graphics,
                client,
                targetName,
                REACH_LABEL,
                distanceText,
                withinReach,
                targetNameColor,
                ReachHudRenderState.hudAlpha
        );
    }

    private static void renderProjectileHud(
            GuiGraphics graphics,
            Minecraft client,
            double deltaTicks
    ) {
        Entity target = ProjectileAimTracker.getCurrentTarget();

        if (target == null) {
            hideProjectileHud(graphics, client, deltaTicks);
            return;
        }

        double distance = ProjectileAimTracker.getTargetDistance();

        if (distance < 0) {
            hideProjectileHud(graphics, client, deltaTicks);
            return;
        }

        boolean willHit = ProjectileAimTracker.willHit();

        updateSmoothedProjectileDistance(
                target,
                distance,
                deltaTicks
        );

        String targetName = target.getName().getString();
        String distanceText = formatDistance(
                ReachHudRenderState.displayedProjectileDistance
        );

        int targetNameColor =
                ReachHudRenderUtils.getTargetNameColor(target);

        ReachHudRenderState.cachedProjectileTargetName = targetName;
        ReachHudRenderState.cachedProjectileDistanceText = distanceText;
        ReachHudRenderState.cachedProjectileWillHit = willHit;
        ReachHudRenderState.cachedProjectileTargetColor = targetNameColor;

        updateHudAlpha(true, deltaTicks);

        renderProjectileHudContent(
                graphics,
                client,
                targetName,
                AIM_LABEL,
                distanceText,
                willHit,
                targetNameColor,
                ReachHudRenderState.hudAlpha
        );
    }

    private static void renderProjectileHudContent(
            GuiGraphics graphics,
            Minecraft client,
            String targetName,
            String aimLabel,
            String distanceText,
            boolean willHit,
            int targetNameColor,
            double alpha
    ) {
        if (alpha <= 0.001) {
            return;
        }

        int indicatorWidth = client.font.width("● ");
        int targetNameWidth = client.font.width(targetName);
        int aimLabelWidth = client.font.width(aimLabel);
        int distanceWidth = client.font.width(distanceText);

        int targetTotalWidth = indicatorWidth + targetNameWidth;
        int aimTotalWidth = aimLabelWidth + distanceWidth;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + 11;
        int aimY = centerY + 23;

        int targetX = centerX - targetTotalWidth / 2;
        int aimX = centerX - aimTotalWidth / 2;

        int distanceColor = willHit
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
                "● ",
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

    private static void renderCachedHud(
            GuiGraphics graphics,
            Minecraft client
    ) {
        double alpha = ReachHudRenderState.hudAlpha;

        if (alpha <= 0.001) {
            return;
        }

        if (!ReachHudRenderState.cachedProjectileTargetName.isEmpty()) {
            renderProjectileHudContent(
                    graphics,
                    client,
                    ReachHudRenderState.cachedProjectileTargetName,
                    AIM_LABEL,
                    ReachHudRenderState.cachedProjectileDistanceText,
                    ReachHudRenderState.cachedProjectileWillHit,
                    ReachHudRenderState.cachedProjectileTargetColor,
                    alpha
            );
            return;
        }

        if (!ReachHudRenderState.cachedMeleeTargetName.isEmpty()) {
            ReachHudMeleeRenderer.render(
                    graphics,
                    client,
                    ReachHudRenderState.cachedMeleeTargetName,
                    REACH_LABEL,
                    ReachHudRenderState.cachedMeleeDistanceText,
                    ReachHudRenderState.cachedMeleeWithinReach,
                    ReachHudRenderState.cachedMeleeTargetColor,
                    alpha
            );
        }
    }

    private static void hideMeleeHud(
            GuiGraphics graphics,
            Minecraft client,
            double deltaTicks
    ) {
        updateHudAlpha(false, deltaTicks);
        renderCachedHud(graphics, client);
        resetMeleeSmoothingOnly();
    }

    private static void hideProjectileHud(
            GuiGraphics graphics,
            Minecraft client,
            double deltaTicks
    ) {
        updateHudAlpha(false, deltaTicks);
        renderCachedHud(graphics, client);
        resetProjectileSmoothing();
    }

    private static void updateHudAlpha(
            boolean visible,
            double deltaTicks
    ) {
        double targetAlpha = visible ? 1.0 : 0.0;
        double difference =
                targetAlpha - ReachHudRenderState.hudAlpha;

        if (Math.abs(difference) < 0.005) {
            ReachHudRenderState.hudAlpha = targetAlpha;
            return;
        }

        double factor = getSmoothingFactor(
                FADE_SPEED,
                deltaTicks
        );

        ReachHudRenderState.hudAlpha += difference * factor;

        if (Math.abs(
                targetAlpha - ReachHudRenderState.hudAlpha
        ) < 0.005) {
            ReachHudRenderState.hudAlpha = targetAlpha;
        }
    }

    private static void updateSmoothedDistance(
            Entity target,
            double actualDistance,
            double deltaTicks
    ) {
        if (ReachHudRenderState.lastTarget != target
                || ReachHudRenderState.displayedDistance < 0) {

            ReachHudRenderState.lastTarget = target;
            ReachHudRenderState.displayedDistance = actualDistance;
            return;
        }

        double factor = getSmoothingFactor(
                SMOOTH_SPEED,
                deltaTicks
        );

        ReachHudRenderState.displayedDistance +=
                (
                        actualDistance
                                - ReachHudRenderState.displayedDistance
                ) * factor;

        if (Math.abs(
                actualDistance
                        - ReachHudRenderState.displayedDistance
        ) < 0.005) {
            ReachHudRenderState.displayedDistance = actualDistance;
        }
    }

    private static void updateSmoothedProjectileDistance(
            Entity target,
            double actualDistance,
            double deltaTicks
    ) {
        if (ReachHudRenderState.lastProjectileTarget != target
                || ReachHudRenderState.displayedProjectileDistance < 0) {

            ReachHudRenderState.lastProjectileTarget = target;
            ReachHudRenderState.displayedProjectileDistance =
                    actualDistance;
            return;
        }

        double factor = getSmoothingFactor(
                SMOOTH_SPEED,
                deltaTicks
        );

        ReachHudRenderState.displayedProjectileDistance +=
                (
                        actualDistance
                                - ReachHudRenderState.displayedProjectileDistance
                ) * factor;

        if (Math.abs(
                actualDistance
                        - ReachHudRenderState.displayedProjectileDistance
        ) < 0.005) {
            ReachHudRenderState.displayedProjectileDistance =
                    actualDistance;
        }
    }

    private static double getSmoothingFactor(
            double speed,
            double deltaTicks
    ) {
        if (deltaTicks <= 0.0) {
            return 0.0;
        }

        return 1.0 - Math.pow(
                1.0 - speed,
                deltaTicks
        );
    }

    private static String formatDistance(double distance) {
        return String.format(
                Locale.ROOT,
                "%.2f",
                distance
        );
    }

    private static void resetSmoothing() {
        ReachHudRenderState.resetSmoothing();
    }

    private static void resetSmoothingValuesOnly() {
        ReachHudRenderState.resetSmoothingValuesOnly();
    }

    private static void resetMeleeSmoothingOnly() {
        ReachHudRenderState.resetMeleeSmoothingOnly();
    }

    private static void resetProjectileSmoothing() {
        ReachHudRenderState.resetProjectileSmoothing();
    }

    private static void renderNotification(
            GuiGraphics graphics,
            Minecraft client
    ) {
        if (!ReachHudNotification.isVisible()) {
            return;
        }

        String text = ReachHudNotification.isEnabled()
                ? "ReachHUD: Active"
                : "ReachHUD: Inactive";

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int textWidth = client.font.width(text);

        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - 58;

        int textColor = ReachHudNotification.isEnabled()
                ? 0xFF55FF55
                : 0xFFFF5555;

        ReachHudRenderUtils.drawText(
                graphics,
                client,
                text,
                x,
                y,
                textColor,
                1.0
        );
    }
}