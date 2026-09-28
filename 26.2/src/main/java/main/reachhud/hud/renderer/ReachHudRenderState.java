package main.reachhud.hud.renderer;

import net.minecraft.world.entity.Entity;

public final class ReachHudRenderState {

    public static Entity lastTarget;
    public static double displayedDistance = -1.0;

    public static String cachedMeleeTargetName = "";
    public static String cachedMeleeDistanceText = "";
    public static boolean cachedMeleeWithinReach;
    public static int cachedMeleeTargetColor = 0xFFFFFFFF;

    public static Entity lastProjectileTarget;
    public static double displayedProjectileDistance = -1.0;

    public static String cachedProjectileTargetName = "";
    public static String cachedProjectileDistanceText = "";
    public static boolean cachedProjectileWillHit;
    public static int cachedProjectileTargetColor = 0xFFFFFFFF;

    public static double hudAlpha = 0.0;

    private ReachHudRenderState() {
    }

    public static void resetSmoothing() {
        resetMeleeSmoothingOnly();
        resetProjectileSmoothing();

        clearMeleeCache();
        clearProjectileCache();

        hudAlpha = 0.0;
    }

    public static void resetSmoothingValuesOnly() {
        resetMeleeSmoothingOnly();
        resetProjectileSmoothing();
    }

    public static void resetMeleeSmoothingOnly() {
        lastTarget = null;
        displayedDistance = -1.0;
    }

    public static void resetProjectileSmoothing() {
        lastProjectileTarget = null;
        displayedProjectileDistance = -1.0;
    }

    public static void clearMeleeCache() {
        cachedMeleeTargetName = "";
        cachedMeleeDistanceText = "";
        cachedMeleeWithinReach = false;
        cachedMeleeTargetColor = 0xFFFFFFFF;
    }

    public static void clearProjectileCache() {
        cachedProjectileTargetName = "";
        cachedProjectileDistanceText = "";
        cachedProjectileWillHit = false;
        cachedProjectileTargetColor = 0xFFFFFFFF;
    }
}