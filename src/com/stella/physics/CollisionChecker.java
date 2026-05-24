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
    public void checkTile(Entity entity){
        // Reseta a colisão a cada frame (começa sem colidir)
        entity.collisonOn = false;

        // Calcula as coordenadas da área de colisão da entidade
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        // Converte as coordenadas para índices de tiles no mapa
        int entityLeftCol = entityLeftWorldX / gp.tileSz;
        int entityRightCol = entityRightWorldX / gp.tileSz;
        int entityTopRow = entityTopWorldY / gp.tileSz;
        int entityBottomRow = entityBottomWorldY / gp.tileSz;

        int tileNum1, tileNum2;

        // Checa colisão baseado na direção que a entidade está virada
        switch (entity.direction) {
            case "top":
                // Verifica tiles acima da entidade
                entityTopRow = (entityTopWorldY - 3) / gp.tileSz;
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityTopRow];
                if(gp.tileManager.tile[tileNum1].collision || gp.tileManager.tile[tileNum2].collision){
                    entity.collisonOn = true;
                }
                break;
                
            case "right":
                // Verifica tiles à direita da entidade
                entityRightCol = (entityRightWorldX + 3) / gp.tileSz;
                tileNum1 = gp.tileManager.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityBottomRow];
                if(gp.tileManager.tile[tileNum1].collision || gp.tileManager.tile[tileNum2].collision){
                    entity.collisonOn = true;
                }
                break;
                
            case "bottom":
                // Verifica tiles abaixo da entidade
                entityBottomRow = (entityBottomWorldY + 3) / gp.tileSz;
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityBottomRow];
                if(gp.tileManager.tile[tileNum1].collision || gp.tileManager.tile[tileNum2].collision){
                    entity.collisonOn = true;
                }
                break;
                
            case "left":
                // Verifica tiles à esquerda da entidade
                entityLeftCol = (entityLeftWorldX - 3) / gp.tileSz;
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityLeftCol][entityBottomRow];
                if(gp.tileManager.tile[tileNum1].collision || gp.tileManager.tile[tileNum2].collision){
                    entity.collisonOn = true;
                }
                break;
        }
    }
}
