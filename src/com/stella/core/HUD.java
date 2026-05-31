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
          
        int barWidth = gp.tileSz * 6;
        int barHeight = 24; // altura da barra em pixels na tela
        int barX = gp.screenWidth / 2 - barWidth / 2;
        int barY = gp.screenHeight - barHeight - 20;

        // Fundo escuro da barra
        g2.setColor(new Color(10, 10, 20));
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Fill azul proporcional ao medo — cor do barInterior (47, 55, 153)
        if (Fear.situation > 0) {
            int halfWidth = barWidth / 2;
            int fill = (int)(halfWidth * Fear.situation);
            int centerX = barX + halfWidth;

            int red = 40 + (int)(50 * Fear.situation);
            g2.setColor(new Color(red, 0, 160));
            
            g2.fillRect(centerX - fill, barY, fill, barHeight);
            g2.fillRect(centerX, barY, fill, barHeight);
        }

        // Borda
        g2.setColor(new Color(0, 0, 0));
        g2.drawRect(barX, barY, barWidth, barHeight);

        if (Fear.situation > 0) {
            float intensity = (float) Fear.situation;

            // Cria um gradiente radial do centro para as bordas
            int cx = gp.screenWidth / 2;
            int cy = gp.screenHeight / 2;

            // Raio do gradiente — cobre toda a tela
            float[] fractions = { 0.3f, 1.0f };
            Color[] colors = {
                new Color(0, 0, 0, 0),                          // centro transparente
                new Color(0, 0, 0, (int)(255 * intensity))      // borda escura
            };
        
            java.awt.RadialGradientPaint vignette = new java.awt.RadialGradientPaint(
                cx, cy,                          // centro
                Math.max(gp.screenWidth, gp.screenHeight) * 0.75f, // raio
                fractions,
                colors
            );
        
            g2.setPaint(vignette);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
            g2.setPaint(null); // reseta o paint
        }
    }
}