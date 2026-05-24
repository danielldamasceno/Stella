package com.stella.entities;

import com.stella.core.GamePanel;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Classe base para todos os objetos do mundo (inimigos, itens, obstacles).
 * Define como os objetos aparecem na tela e suas propriedades básicas.
 */
public class superObject {
    // A imagem que será desenhada
    public BufferedImage image;
    
    // Nome do objeto (para identificação)
    public String name;
    
    // Se verdadeiro, o objeto bloqueia movimento
    public boolean collsion = false;
    
    // Posição no mundo
    public int WorldX, WorldY;
    
    // Tamanho do objeto em pixels
    public int width, height;
    
    // Se verdadeiro, este objeto é um inimigo
    public boolean enemy = false;

    /**
     * Desenha o objeto na tela se estiver dentro do campo de visão do jogador.
     * Usa câmera centralizada no jogador.
     */
    public void draw(Graphics2D g2, GamePanel gp){
        // Calcula a posição na tela baseado na posição da câmera
        int ScreenX = WorldX - gp.cameraX;
        int ScreenY = WorldY - gp.cameraY;
        
        // Só desenha se o objeto estiver visível na tela (usando coordenadas da câmera)
        if (ScreenX + gp.tileSz > 0 && ScreenX - gp.tileSz < gp.screenWidth &&
            ScreenY + gp.tileSz > 0 && ScreenY - gp.tileSz < gp.screenHeight) {
            g2.drawImage(image, ScreenX, ScreenY, width, height, null);
        }
    }
}
