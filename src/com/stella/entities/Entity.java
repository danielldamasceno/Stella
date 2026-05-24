package com.stella.entities;

import java.awt.Rectangle;

/**
 * Classe base para todas as entidades do jogo (personagens, inimigos, objetos).
 * Define propriedades comuns como posição, direção e colisão.
 */
public class Entity {
    // Posição no mundo do jogo
    public int posY, posX, worldX, worldY;
    
    // Área de colisão (hitbox) da entidade
    public Rectangle solidArea;
    
    // Para qual direção a entidade está virada (top, bottom, left, right)
    public String direction = "top";
    
    // Indica se a entidade colidiu com algo neste frame
    public boolean collisonOn = false;
}
