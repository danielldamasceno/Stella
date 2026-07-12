package com.stella.entities;

import com.stella.core.GamePanel;

public class StepDialogueObject extends superObject {
    public String[] dialogueLines = {"Você passou por aqui."};
    public boolean triggered = false;

    public StepDialogueObject(GamePanel gp) {
        this.width = 8;
        this.height = 8;
        this.collsion = false;
        this.image = null;
    }

    public boolean isPlayerOnTop(com.stella.player.Player player) {
        int tileSize = player.getGamePanel().tileSz;
        double playerLeft = player.worldX;
        double playerRight = player.worldX + tileSize;
        double playerTop = player.worldY;
        double playerBottom = player.worldY + tileSize;

        double objectLeft = WorldX;
        double objectRight = WorldX + width;
        double objectTop = WorldY;
        double objectBottom = WorldY + height;

        return playerRight > objectLeft && playerLeft < objectRight && playerBottom > objectTop && playerTop < objectBottom;
    }
}
