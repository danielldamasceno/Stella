package com.stella.entities;

import java.io.IOException;
import javax.imageio.ImageIO;

import com.stella.player.Player;
import com.stella.core.GamePanel;

/**
 * Representa um inimigo (Boys) no jogo.
 * Herda de superObject para ter as propriedades e desenho de objetos.
 */
public class Enemy extends superObject {
    private GamePanel gp;
    private int animTimer = 0;
    private int randomDir = 0;

    public Enemy(GamePanel gp) {
        this.gp = gp;
        // Define o nome deste tipo de inimigo
        name = "Boys";
        
        // Carrega a imagem do inimigo do arquivo
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/res/enemy.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     public void update(Player player) {
        double exactX = 0;
        double exactY = 0;
        boolean initialized = false;

        if (!initialized && width > 0) {
        exactX = WorldX;
        exactY = WorldY;
        initialized = true;
        }

        double playerCenterX = player.worldX + gp.tileSz / 2.0;
        double playerCenterY = player.worldY + gp.tileSz / 2.0;

        double objCenterX = exactX + width / 2.0;
        double objCenterY = exactY + height / 2.0;

        double dx = playerCenterX - objCenterX;
        double dy = playerCenterY - objCenterY;
        double dist = Math.hypot(dx, dy);

        if (enemyType.equals("persuer") && dist < 220) {
        // Normaliza para mover sempre na mesma velocidade
        // independente da direção
        double speed = 0.6;
        WorldX += (dx / dist) * speed;
        WorldY += (dy / dist) * speed;
        } else if (enemyType.equals("randomWalker")) {
         // Muda de direção só a cada 60 frames para não tremer
            if (animTimer <= 0) {
                randomDir = (int)(Math.random() * 4);
                animTimer = 60;
            } else {
                animTimer--;
                double speed = 0.5;
                switch (randomDir) {
                    case 0: WorldX += speed; break;
                    case 1: WorldX -= speed; break;
                    case 2: WorldY += speed; break;
                    case 3: WorldY -= speed; break;
                }
            }
     
        }    
    }
}
