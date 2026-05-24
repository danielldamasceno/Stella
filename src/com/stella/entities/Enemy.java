package com.stella.entities;

import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Representa um inimigo (Boys) no jogo.
 * Herda de superObject para ter as propriedades e desenho de objetos.
 */
public class Enemy extends superObject{
    
    public Enemy(){
        // Define o nome deste tipo de inimigo
        name = "Boys";
        
        // Carrega a imagem do inimigo do arquivo
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/res/enemy.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
