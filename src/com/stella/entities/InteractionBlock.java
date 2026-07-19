package com.stella.entities;

import com.stella.core.GamePanel;
import java.io.IOException;
import javax.imageio.ImageIO;



public class InteractionBlock extends superObject {
    public String promptText = "Ouvir (aperte E)";
    public String[] dialogueLines = {"Interação iniciada."};
    public String[][] dialogueOptions = null;
    public boolean used = false;

    public InteractionBlock(GamePanel gp) {
        this.width = 8;
        this.height = 8;
        this.collsion = false;
        this.image = null;
    }
    public InteractionBlock(GamePanel gp, String imagePath) {
        this.width = 16;
        this.height = 16;
        this.collsion = false;
        try {
            this.image = ImageIO.read(getClass().getResourceAsStream(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public String[] getDialogueLines() {
        if (dialogueOptions != null && dialogueOptions.length > 0) {
            int index = (int) (Math.random() * dialogueOptions.length);
            return dialogueOptions[index];
        }
        return dialogueLines;
    }

    public boolean isNear(com.stella.player.Player player) {
        int tileSize = player.getGamePanel().tileSz;
        double dx = (WorldX + width / 2.0) - (player.worldX + tileSize / 2.0);
        double dy = (WorldY + height / 2.0) - (player.worldY + tileSize / 2.0);
        double dist = Math.hypot(dx, dy);
        return dist < 180;
    }
}
