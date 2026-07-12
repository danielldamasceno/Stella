package com.stella.physics;

import com.stella.core.GamePanel;
import com.stella.entities.Entity;

/**
 * Verifica colisões entre entidades e tiles do mapa.
 * Detecta se o jogador pode se mover em uma direção ou se há obstáculo.
 */
public class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp){
        this.gp = gp;
    }

    /**
     * Verifica se uma entidade colidiu com alguma parede/tile.
     * Baseado na direção que a entidade está tentando se mover.
     */
    public void checkTile(Entity entity) {
        checkTile(entity, entity.direction);
    }

    public void checkTile(Entity entity, String direction) {
        entity.collisonOn = false;

        int tileSize = gp.tileSz;
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol = entityLeftWorldX / tileSize;
        int entityRightCol = entityRightWorldX / tileSize;
        int entityTopRow = entityTopWorldY / tileSize;
        int entityBottomRow = entityBottomWorldY / tileSize;

        int rowCheck;
        int colCheck;

        switch (direction) {
            case "top":
                rowCheck = Math.max(0, (entityTopWorldY - 3) / tileSize);
                entity.collisonOn = isTileCollidable(entityLeftCol, rowCheck) || isTileCollidable(entityRightCol, rowCheck);
                break;
            case "right":
                colCheck = Math.min(gp.maxWorldCol - 1, (entityRightWorldX + 3) / tileSize);
                entity.collisonOn = isTileCollidable(colCheck, entityTopRow) || isTileCollidable(colCheck, entityBottomRow);
                break;
            case "bottom":
                rowCheck = Math.min(gp.maxWorldRow - 1, (entityBottomWorldY + 3) / tileSize);
                entity.collisonOn = isTileCollidable(entityLeftCol, rowCheck) || isTileCollidable(entityRightCol, rowCheck);
                break;
            case "left":
                colCheck = Math.max(0, (entityLeftWorldX - 3) / tileSize);
                entity.collisonOn = isTileCollidable(colCheck, entityTopRow) || isTileCollidable(colCheck, entityBottomRow);
                break;
        }
    }

    private boolean isTileCollidable(int col, int row) {
        if (col < 0 || row < 0 || col >= gp.maxWorldCol || row >= gp.maxWorldRow) {
            return true;
        }

        int tileNum = gp.tileManager.mapTileNum[col][row];
        return tileNum >= 0 && tileNum < gp.tileManager.tile.length && gp.tileManager.tile[tileNum].collision;
    }
}
