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
    private static final double FADE_SPEED = 0.18;

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
            updateHudAlpha(false, deltaTicks);
            renderCachedHud(graphics, client);
            resetMeleeSmoothingOnly();
            return;
        }

        double distance = ReachCalculator.getDistanceTo(target);
        double reach = ReachCalculator.getPlayerReach();

        if (distance < 0 || reach < 0) {
            updateHudAlpha(false, deltaTicks);
            renderCachedHud(graphics, client);
            resetMeleeSmoothingOnly();
            return;
        }

        if (distance > reach + 3.0) {
            updateHudAlpha(false, deltaTicks);
            renderCachedHud(graphics, client);
            resetMeleeSmoothingOnly();
            return;
        }

        boolean withinReach = distance <= reach;

        updateSmoothedDistance(target, distance, deltaTicks);

        String targetName = target.getName().getString();
        String reachLabel = "Reach: ";
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
                reachLabel,
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
        Entity target =
                ProjectileAimTracker.getCurrentTarget();

        if (target == null) {
            updateHudAlpha(false, deltaTicks);
            renderCachedHud(graphics, client);
            resetProjectileSmoothing();
            return;
        }

        double distance =
                ProjectileAimTracker.getTargetDistance();

        if (distance < 0) {
            updateHudAlpha(false, deltaTicks);
            renderCachedHud(graphics, client);
            resetProjectileSmoothing();
            return;
        }

        boolean willHit =
                ProjectileAimTracker.willHit();

        updateSmoothedProjectileDistance(
                target,
                distance,
                deltaTicks
        );

        String targetName =
                target.getName().getString();

        String aimLabel = "Aim: ";

        String distanceText =
                formatDistance(
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
                aimLabel,
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

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + 11;
        int aimY = centerY + 23;

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

        int boxX = aimX - paddingHorizontal;
        int boxY = aimY - 2;

        int boxWidth =
                aimTotalWidth + paddingHorizontal * 2;

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
        if (ReachHudRenderState.hudAlpha <= 0.001) {
            return;
        }

        if (!ReachHudRenderState.cachedProjectileTargetName.isEmpty()) {
            renderProjectileHudContent(
                    graphics,
                    client,
                    ReachHudRenderState.cachedProjectileTargetName,
                    "Aim: ",
                    ReachHudRenderState.cachedProjectileDistanceText,
                    ReachHudRenderState.cachedProjectileWillHit,
                    ReachHudRenderState.cachedProjectileTargetColor,
                    ReachHudRenderState.hudAlpha
            );

            return;
        }

        if (!ReachHudRenderState.cachedMeleeTargetName.isEmpty()) {
            ReachHudMeleeRenderer.render(
                    graphics,
                    client,
                    ReachHudRenderState.cachedMeleeTargetName,
                    "Reach: ",
                    ReachHudRenderState.cachedMeleeDistanceText,
                    ReachHudRenderState.cachedMeleeWithinReach,
                    ReachHudRenderState.cachedMeleeTargetColor,
                    ReachHudRenderState.hudAlpha
            );
        }
    }

    private static void updateHudAlpha(
            boolean visible,
            double deltaTicks
    ) {
        double targetAlpha =
                visible ? 1.0 : 0.0;

        double difference =
                targetAlpha - ReachHudRenderState.hudAlpha;

        double factor =
                getSmoothingFactor(FADE_SPEED, deltaTicks);

        ReachHudRenderState.hudAlpha +=
                difference * factor;

        if (Math.abs(difference) < 0.005) {
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

        double factor =
                getSmoothingFactor(SMOOTH_SPEED, deltaTicks);

        ReachHudRenderState.displayedDistance +=
                (actualDistance
                        - ReachHudRenderState.displayedDistance)
                        * factor;

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

        double factor =
                getSmoothingFactor(SMOOTH_SPEED, deltaTicks);

        ReachHudRenderState.displayedProjectileDistance +=
                (actualDistance
                        - ReachHudRenderState.displayedProjectileDistance)
                        * factor;

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

        String text =
                ReachHudNotification.isEnabled()
                        ? "ReachHUD: Active"
                        : "ReachHUD: Inactive";

        int screenWidth =
                client.getWindow().getGuiScaledWidth();

        int screenHeight =
                client.getWindow().getGuiScaledHeight();

        int textWidth =
                client.font.width(text);

        int x =
                (screenWidth - textWidth) / 2;

        int y =
                screenHeight - 58;

        int textColor =
                ReachHudNotification.isEnabled()
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