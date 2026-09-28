package main.reachhud.hud.renderer;

import net.minecraft.world.entity.Entity;

public final class ReachHudRenderState {

    static Entity lastTarget;
    static double displayedDistance = -1.0;

    static String cachedMeleeTargetName = "";
    static String cachedMeleeDistanceText = "";
    static boolean cachedMeleeWithinReach;
    static int cachedMeleeTargetColor = 0xFFFFFFFF;

    static Entity lastProjectileTarget;
    static double displayedProjectileDistance = -1.0;

    static String cachedProjectileTargetName = "";
    static String cachedProjectileDistanceText = "";
    static boolean cachedProjectileWillHit;
    static int cachedProjectileTargetColor = 0xFFFFFFFF;

    static double hudAlpha = 0.0;

    private ReachHudRenderState() {
    }

    static void resetSmoothing() {
        resetMeleeSmoothingOnly();
        resetProjectileSmoothing();

        clearMeleeCache();
        clearProjectileCache();

        hudAlpha = 0.0;
    }

    static void resetSmoothingValuesOnly() {
        resetMeleeSmoothingOnly();
        resetProjectileSmoothing();
    }

    static void resetMeleeSmoothingOnly() {
        lastTarget = null;
        displayedDistance = -1.0;
    }

    static void resetProjectileSmoothing() {
        lastProjectileTarget = null;
        displayedProjectileDistance = -1.0;
    }

    static void clearMeleeCache() {
        cachedMeleeTargetName = "";
        cachedMeleeDistanceText = "";
        cachedMeleeWithinReach = false;
        cachedMeleeTargetColor = 0xFFFFFFFF;
    }

    static void clearProjectileCache() {
        cachedProjectileTargetName = "";
        cachedProjectileDistanceText = "";
        cachedProjectileWillHit = false;
        cachedProjectileTargetColor = 0xFFFFFFFF;
    }
}