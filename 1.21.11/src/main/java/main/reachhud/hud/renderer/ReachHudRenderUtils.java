package main.reachhud.hud.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

final class ReachHudRenderUtils {

    private ReachHudRenderUtils() {
    }

    static void drawRoundedBox(
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

    static void drawText(
            GuiGraphics graphics,
            Minecraft client,
            String text,
            int x,
            int y,
            int color,
            double alpha
    ) {
        Component component = Component.literal(text);

        int shadowColor = applyAlpha(
                0xAA000000,
                alpha
        );

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

    static int getTargetNameColor(Entity target) {
        if (target instanceof Player
                || target instanceof Enemy) {
            return 0xFFFF5555;
        }

        return 0xFF55FF55;
    }

    static int applyAlpha(
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
}