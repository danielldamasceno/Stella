package com.stella.core;

import com.stella.player.Fear;
import java.awt.Color;
import java.awt.Graphics2D;

public class HUD {

    GamePanel gp;

    public HUD(GamePanel gp) {
        this.gp = gp;
    }

    public void Draw(Graphics2D g2) {
        double fear = Math.max(0.0, Math.min(1.0, Fear.situation));

        int barWidth = gp.tileSz * 6;
        int barHeight = 24;
        int barX = gp.screenWidth / 2 - barWidth / 2;
        int barY = gp.screenHeight - barHeight - 20;

        // Fundo escuro da barra
        g2.setColor(new Color(10, 10, 20));
        g2.fillRect(barX, barY, barWidth, barHeight);

        if (fear > 0) {
            int halfWidth = barWidth / 2;
            int fill = (int)(halfWidth * fear);
            int centerX = barX + halfWidth;

            int red = 40 + (int)(50 * fear);
            g2.setColor(new Color(red, 0, 160));

            g2.fillRect(centerX - fill, barY, fill, barHeight);
            g2.fillRect(centerX, barY, fill, barHeight);
        }

        g2.setColor(new Color(0, 0, 0));
        g2.drawRect(barX, barY, barWidth, barHeight);

        if (fear <= 0) {
            return;
        }

        if (fear > 0) {
            int alpha = (int)(140 * fear);
            g2.setColor(new Color(8, 0, 12, alpha));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

            float[] fractions = {0.15f, 0.65f, 1.0f};
            Color[] colors = {
                new Color(0, 0, 0, 0),
                new Color(18, 0, 22, (int)(90 * fear)),
                new Color(80, 0, 16, (int)(180 * fear))
            };

            java.awt.RadialGradientPaint vignette = new java.awt.RadialGradientPaint(
                gp.screenWidth / 2f,
                gp.screenHeight / 2f,
                Math.max(gp.screenWidth, gp.screenHeight) * 0.75f,
                fractions,
                colors
            );

            g2.setPaint(vignette);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
            g2.setPaint(null);

            if ((System.currentTimeMillis() / 120) % 2 == 0) {
                g2.setColor(new Color(255, 20, 30, (int)(18 * fear)));
                g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
            }
        }
    }
}