package main.reachhud.hud;

import main.reachhud.ReachHUD;
import main.reachhud.hud.renderer.ReachHudMeleeRenderer;
import main.reachhud.hud.renderer.ReachHudProjectileRenderer;
import main.reachhud.hud.renderer.ReachHudRenderState;
import main.reachhud.input.ReachHudKeybind;
import main.reachhud.projectile.ProjectileAimTracker;
import main.reachhud.reach.ReachCalculator;
import main.reachhud.target.TargetTracker;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public final class ReachHudRenderer {

    private static final double SMOOTH_SPEED = 0.25;
    private static final double FADE_SPEED = 0.18;
    private static final double MAX_DISTANCE_ADVANCE = 3.0;

    private static long lastFrameTime = System.nanoTime();

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
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker
    ) {
        Minecraft client =
                Minecraft.getInstance();

        double deltaSeconds =
                getDeltaSeconds();

        if (client.player == null) {
            ReachHudRenderState.resetSmoothing();
            return;
        }

        renderNotification(
                graphics,
                client
        );

        if (!ReachHudKeybind.isEnabled()) {
            updateHudAlpha(
                    false,
                    deltaSeconds
            );

            renderCachedHud(
                    graphics,
                    client
            );

            if (ReachHudRenderState.hudAlpha <= 0.001) {
                ReachHudRenderState.clearMeleeCache();
                ReachHudRenderState.clearProjectileCache();
            }

            ReachHudRenderState.resetSmoothingValuesOnly();
            return;
        }

        if (ProjectileAimTracker.hasTarget()) {
            renderProjectileHud(
                    graphics,
                    client,
                    deltaSeconds
            );

            return;
        }

        ReachHudRenderState.clearProjectileCache();

        renderMeleeHud(
                graphics,
                client,
                deltaSeconds
        );
    }

    private static void renderMeleeHud(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            double deltaSeconds
    ) {
        Entity target =
                TargetTracker.getCurrentTarget();

        if (target == null) {
            updateHudAlpha(
                    false,
                    deltaSeconds
            );

            renderCachedMeleeHud(
                    graphics,
                    client
            );

            ReachHudRenderState.resetMeleeSmoothingOnly();
            return;
        }

        double distance =
                ReachCalculator.getDistanceTo(target);

        double reach =
                ReachCalculator.getPlayerReach();

        if (distance < 0
                || reach < 0
                || distance > reach + MAX_DISTANCE_ADVANCE) {

            updateHudAlpha(
                    false,
                    deltaSeconds
            );

            renderCachedMeleeHud(
                    graphics,
                    client
            );

            ReachHudRenderState.resetMeleeSmoothingOnly();
            return;
        }

        boolean withinReach =
                distance <= reach;

        updateMeleeSmoothing(
                target,
                distance,
                deltaSeconds
        );

        ReachHudMeleeRenderer.updateCache(
                target,
                ReachHudRenderState.displayedDistance,
                withinReach
        );

        ReachHudRenderState.clearProjectileCache();

        updateHudAlpha(
                true,
                deltaSeconds
        );

        ReachHudMeleeRenderer.renderCached(
                graphics,
                client,
                ReachHudRenderState.hudAlpha
        );
    }

    private static void renderProjectileHud(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            double deltaSeconds
    ) {
        Entity target =
                ProjectileAimTracker.getCurrentTarget();

        if (target == null) {
            updateHudAlpha(
                    false,
                    deltaSeconds
            );

            renderCachedProjectileHud(
                    graphics,
                    client
            );

            ReachHudRenderState.resetProjectileSmoothing();
            return;
        }

        double distance =
                ProjectileAimTracker.getTargetDistance();

        if (distance < 0) {
            updateHudAlpha(
                    false,
                    deltaSeconds
            );

            renderCachedProjectileHud(
                    graphics,
                    client
            );

            ReachHudRenderState.resetProjectileSmoothing();
            return;
        }

        boolean willHit =
                ProjectileAimTracker.willHit();

        updateProjectileSmoothing(
                target,
                distance,
                deltaSeconds
        );

        ReachHudProjectileRenderer.updateCache(
                target,
                ReachHudRenderState.displayedProjectileDistance,
                willHit
        );

        ReachHudRenderState.clearMeleeCache();

        updateHudAlpha(
                true,
                deltaSeconds
        );

        ReachHudProjectileRenderer.renderCached(
                graphics,
                client,
                ReachHudRenderState.hudAlpha
        );
    }

    private static void updateMeleeSmoothing(
            Entity target,
            double distance,
            double deltaSeconds
    ) {
        if (ReachHudRenderState.lastTarget != target
                || ReachHudRenderState.displayedDistance < 0) {

            ReachHudRenderState.lastTarget = target;
            ReachHudRenderState.displayedDistance =
                    distance;

            return;
        }

        double smoothingFactor =
                getSmoothingFactor(
                        SMOOTH_SPEED,
                        deltaSeconds
                );

        ReachHudRenderState.displayedDistance +=
                (distance
                        - ReachHudRenderState.displayedDistance)
                        * smoothingFactor;
    }

    private static void updateProjectileSmoothing(
            Entity target,
            double distance,
            double deltaSeconds
    ) {
        if (ReachHudRenderState.lastProjectileTarget != target
                || ReachHudRenderState.displayedProjectileDistance < 0) {

            ReachHudRenderState.lastProjectileTarget =
                    target;

            ReachHudRenderState.displayedProjectileDistance =
                    distance;

            return;
        }

        double smoothingFactor =
                getSmoothingFactor(
                        SMOOTH_SPEED,
                        deltaSeconds
                );

        ReachHudRenderState.displayedProjectileDistance +=
                (distance
                        - ReachHudRenderState.displayedProjectileDistance)
                        * smoothingFactor;
    }

    private static void updateHudAlpha(
            boolean visible,
            double deltaSeconds
    ) {
        double targetAlpha =
                visible ? 1.0 : 0.0;

        double smoothingFactor =
                getSmoothingFactor(
                        FADE_SPEED,
                        deltaSeconds
                );

        ReachHudRenderState.hudAlpha +=
                (targetAlpha
                        - ReachHudRenderState.hudAlpha)
                        * smoothingFactor;

        if (Math.abs(
                targetAlpha
                        - ReachHudRenderState.hudAlpha
        ) < 0.001) {

            ReachHudRenderState.hudAlpha =
                    targetAlpha;
        }
    }

    private static double getSmoothingFactor(
            double speed,
            double deltaSeconds
    ) {
        return 1.0
                - Math.exp(
                -speed
                        * deltaSeconds
                        * 60.0
        );
    }

    private static double getDeltaSeconds() {
        long currentTime =
                System.nanoTime();

        double deltaSeconds =
                (currentTime - lastFrameTime)
                        / 1_000_000_000.0;

        lastFrameTime = currentTime;

        return Math.min(
                Math.max(deltaSeconds, 0.0),
                0.1
        );
    }

    private static void renderCachedHud(
            GuiGraphicsExtractor graphics,
            Minecraft client
    ) {
        if (!ReachHudRenderState.cachedMeleeTargetName.isEmpty()) {
            renderCachedMeleeHud(
                    graphics,
                    client
            );

            return;
        }

        if (!ReachHudRenderState.cachedProjectileTargetName.isEmpty()) {
            renderCachedProjectileHud(
                    graphics,
                    client
            );
        }
    }

    private static void renderCachedMeleeHud(
            GuiGraphicsExtractor graphics,
            Minecraft client
    ) {
        ReachHudMeleeRenderer.renderCached(
                graphics,
                client,
                ReachHudRenderState.hudAlpha
        );
    }

    private static void renderCachedProjectileHud(
            GuiGraphicsExtractor graphics,
            Minecraft client
    ) {
        ReachHudProjectileRenderer.renderCached(
                graphics,
                client,
                ReachHudRenderState.hudAlpha
        );
    }

    private static void renderNotification(
            GuiGraphicsExtractor graphics,
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

        Component component =
                Component.literal(text);

        graphics.text(
                client.font,
                component,
                x + 1,
                y + 1,
                0xAA000000,
                false
        );

        graphics.text(
                client.font,
                component,
                x,
                y,
                textColor,
                false
        );
    }
}