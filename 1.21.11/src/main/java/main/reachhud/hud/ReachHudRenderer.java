package main.reachhud.hud;

import main.reachhud.ReachHUD;
import main.reachhud.input.ReachHudKeybind;
import main.reachhud.projectile.ProjectileAimTracker;
import main.reachhud.reach.ReachCalculator;
import main.reachhud.target.TargetTracker;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public final class ReachHudRenderer {

    private static final double SMOOTH_SPEED = 0.25;
    private static final double FADE_SPEED = 0.18;

    private static Entity lastTarget;
    private static double displayedDistance = -1.0;

    private static String cachedMeleeTargetName = "";
    private static String cachedMeleeDistanceText = "";
    private static boolean cachedMeleeWithinReach;
    private static int cachedMeleeTargetColor = 0xFFFFFFFF;

    private static Entity lastProjectileTarget;
    private static double displayedProjectileDistance = -1.0;

    private static String cachedProjectileTargetName = "";
    private static String cachedProjectileDistanceText = "";
    private static boolean cachedProjectileWillHit;
    private static int cachedProjectileTargetColor = 0xFFFFFFFF;

    private static double hudAlpha = 0.0;

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

        renderNotification(graphics, client);

        if (!ReachHudKeybind.isEnabled()) {
            updateHudAlpha(false);
            renderCachedHud(graphics, client);
            resetSmoothingValuesOnly();
            return;
        }

        if (ProjectileAimTracker.hasTarget()) {
            renderProjectileHud(graphics, client);
            return;
        }

        resetProjectileSmoothing();

        Entity target = TargetTracker.getCurrentTarget();

        if (target == null) {
            updateHudAlpha(false);
            renderCachedHud(graphics, client);
            resetMeleeSmoothingOnly();
            return;
        }

        double distance = ReachCalculator.getDistanceTo(target);
        double reach = ReachCalculator.getPlayerReach();

        if (distance < 0 || reach < 0) {
            updateHudAlpha(false);
            renderCachedHud(graphics, client);
            resetMeleeSmoothingOnly();
            return;
        }

        if (distance > reach + 3.0) {
            updateHudAlpha(false);
            renderCachedHud(graphics, client);
            resetMeleeSmoothingOnly();
            return;
        }

        boolean withinReach = distance <= reach;

        updateSmoothedDistance(target, distance);

        String targetName = target.getName().getString();
        String reachLabel = "Reach: ";
        String distanceText = String.format("%.2f", displayedDistance);

        int targetNameColor = getTargetNameColor(target);

        cachedMeleeTargetName = targetName;
        cachedMeleeDistanceText = distanceText;
        cachedMeleeWithinReach = withinReach;
        cachedMeleeTargetColor = targetNameColor;

        updateHudAlpha(true);

        renderMeleeHud(
                graphics,
                client,
                targetName,
                reachLabel,
                distanceText,
                withinReach,
                targetNameColor,
                hudAlpha
        );
    }

    private static void renderMeleeHud(
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

        int reachTotalWidth = reachLabelWidth + distanceWidth;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + 11;
        int reachY = centerY + 23;

        int targetX = centerX - targetNameWidth / 2;
        int reachX = centerX - reachTotalWidth / 2;

        int distanceColor =
                withinReach
                        ? 0xFF55FF55
                        : 0xFFFF5555;

        int fadedTargetColor =
                applyAlpha(targetNameColor, alpha);

        int fadedDistanceColor =
                applyAlpha(distanceColor, alpha);

        int fadedLabelColor =
                applyAlpha(0xFFCCCCCC, alpha);

        int paddingHorizontal = 6;

        int boxX = reachX - paddingHorizontal;
        int boxY = reachY - 2;

        int boxWidth =
                reachTotalWidth + paddingHorizontal * 2;

        int boxHeight = 12;

        int backgroundColor =
                applyAlpha(0x99000000, alpha);

        drawRoundedBox(
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

        accentColor = applyAlpha(accentColor, alpha);

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
                applyAlpha(targetNameColor, alpha);

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

        drawText(
                graphics,
                client,
                targetName,
                targetX,
                targetY,
                fadedTargetColor,
                alpha
        );

        drawText(
                graphics,
                client,
                reachLabel,
                reachX,
                reachY,
                fadedLabelColor,
                alpha
        );

        drawText(
                graphics,
                client,
                distanceText,
                reachX + reachLabelWidth,
                reachY,
                fadedDistanceColor,
                alpha
        );
    }

    private static void renderProjectileHud(
            GuiGraphics graphics,
            Minecraft client
    ) {
        Entity target =
                ProjectileAimTracker.getCurrentTarget();

        if (target == null) {
            updateHudAlpha(false);
            renderCachedHud(graphics, client);
            resetProjectileSmoothing();
            return;
        }

        double distance =
                ProjectileAimTracker.getTargetDistance();

        if (distance < 0) {
            updateHudAlpha(false);
            renderCachedHud(graphics, client);
            resetProjectileSmoothing();
            return;
        }

        boolean willHit =
                ProjectileAimTracker.willHit();

        updateSmoothedProjectileDistance(
                target,
                distance
        );

        String targetName =
                target.getName().getString();

        String aimLabel = "Aim: ";

        String distanceText =
                String.format(
                        "%.2f",
                        displayedProjectileDistance
                );

        int targetNameColor =
                getTargetNameColor(target);

        cachedProjectileTargetName = targetName;
        cachedProjectileDistanceText = distanceText;
        cachedProjectileWillHit = willHit;
        cachedProjectileTargetColor = targetNameColor;

        updateHudAlpha(true);

        renderProjectileHudContent(
                graphics,
                client,
                targetName,
                aimLabel,
                distanceText,
                willHit,
                targetNameColor,
                hudAlpha
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
                applyAlpha(targetNameColor, alpha);

        int fadedDistanceColor =
                applyAlpha(distanceColor, alpha);

        int fadedLabelColor =
                applyAlpha(0xFFCCCCCC, alpha);

        int paddingHorizontal = 6;

        int boxX = aimX - paddingHorizontal;
        int boxY = aimY - 2;

        int boxWidth =
                aimTotalWidth + paddingHorizontal * 2;

        int boxHeight = 12;

        int backgroundColor =
                applyAlpha(0x99000000, alpha);

        drawRoundedBox(
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

        accentColor = applyAlpha(accentColor, alpha);

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
                applyAlpha(targetNameColor, alpha);

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

        drawText(
                graphics,
                client,
                targetName,
                targetX,
                targetY,
                fadedTargetColor,
                alpha
        );

        drawText(
                graphics,
                client,
                aimLabel,
                aimX,
                aimY,
                fadedLabelColor,
                alpha
        );

        drawText(
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
        if (hudAlpha <= 0.001) {
            return;
        }

        if (!cachedProjectileTargetName.isEmpty()) {
            renderProjectileHudContent(
                    graphics,
                    client,
                    cachedProjectileTargetName,
                    "Aim: ",
                    cachedProjectileDistanceText,
                    cachedProjectileWillHit,
                    cachedProjectileTargetColor,
                    hudAlpha
            );

            return;
        }

        if (!cachedMeleeTargetName.isEmpty()) {
            renderMeleeHud(
                    graphics,
                    client,
                    cachedMeleeTargetName,
                    "Reach: ",
                    cachedMeleeDistanceText,
                    cachedMeleeWithinReach,
                    cachedMeleeTargetColor,
                    hudAlpha
            );
        }
    }

    private static void drawRoundedBox(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        if (width <= 2 || height <= 2) {
            graphics.fill(
                    x,
                    y,
                    x + width,
                    y + height,
                    color
            );

            return;
        }

        graphics.fill(
                x + 1,
                y,
                x + width - 1,
                y + height,
                color
        );

        graphics.fill(
                x,
                y + 1,
                x + width,
                y + height - 1,
                color
        );
    }

    private static void drawText(
            GuiGraphics graphics,
            Minecraft client,
            String text,
            int x,
            int y,
            int color,
            double alpha
    ) {
        Component component =
                Component.literal(text);

        int shadowColor =
                applyAlpha(0xAA000000, alpha);

        graphics.drawString(
                client.font,
                component,
                x + 1,
                y + 1,
                shadowColor
        );

        graphics.drawString(
                client.font,
                component,
                x,
                y,
                color
        );
    }

    private static int getTargetNameColor(Entity target) {
        if (target instanceof Player
                || target instanceof Enemy) {

            return 0xFFFF5555;
        }

        return 0xFF55FF55;
    }

    private static int applyAlpha(
            int color,
            double alpha
    ) {
        alpha = Math.max(
                0.0,
                Math.min(1.0, alpha)
        );

        int originalAlpha =
                (color >>> 24) & 0xFF;

        int finalAlpha =
                (int) (originalAlpha * alpha);

        return (color & 0x00FFFFFF)
                | (finalAlpha << 24);
    }

    private static void updateHudAlpha(
            boolean visible
    ) {
        double targetAlpha =
                visible ? 1.0 : 0.0;

        double difference =
                targetAlpha - hudAlpha;

        hudAlpha +=
                difference * FADE_SPEED;

        if (Math.abs(difference) < 0.005) {
            hudAlpha = targetAlpha;
        }
    }

    private static void updateSmoothedDistance(
            Entity target,
            double actualDistance
    ) {
        if (lastTarget != target
                || displayedDistance < 0) {

            lastTarget = target;
            displayedDistance = actualDistance;
            return;
        }

        displayedDistance +=
                (actualDistance - displayedDistance)
                        * SMOOTH_SPEED;

        if (Math.abs(
                actualDistance - displayedDistance
        ) < 0.005) {

            displayedDistance = actualDistance;
        }
    }

    private static void updateSmoothedProjectileDistance(
            Entity target,
            double actualDistance
    ) {
        if (lastProjectileTarget != target
                || displayedProjectileDistance < 0) {

            lastProjectileTarget = target;
            displayedProjectileDistance = actualDistance;
            return;
        }

        displayedProjectileDistance +=
                (actualDistance - displayedProjectileDistance)
                        * SMOOTH_SPEED;

        if (Math.abs(
                actualDistance - displayedProjectileDistance
        ) < 0.005) {

            displayedProjectileDistance = actualDistance;
        }
    }

    private static void resetSmoothing() {
        lastTarget = null;
        displayedDistance = -1.0;

        resetProjectileSmoothing();

        cachedMeleeTargetName = "";
        cachedMeleeDistanceText = "";

        cachedProjectileTargetName = "";
        cachedProjectileDistanceText = "";

        hudAlpha = 0.0;
    }

    private static void resetSmoothingValuesOnly() {
        resetMeleeSmoothingOnly();
        resetProjectileSmoothing();
    }

    private static void resetMeleeSmoothingOnly() {
        lastTarget = null;
        displayedDistance = -1.0;
    }

    private static void resetProjectileSmoothing() {
        lastProjectileTarget = null;
        displayedProjectileDistance = -1.0;
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

        drawText(
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