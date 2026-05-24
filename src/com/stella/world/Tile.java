package com.stella.world;

import java.awt.image.BufferedImage;

/**
 * Representa um tipo de tile (bloco) do mapa.
 * Cada tile tem uma imagem e pode ter colisão ou não.
 */
public class Tile {
    // Imagem que será desenhada na tela
    public BufferedImage image;
    
    // Se verdadeiro, o jogador não consegue passar através deste tile
    public boolean collision = false;
}
