package com.stella.entities;

import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Representa um aliado no jogo.
 * Herda de superObject para ter as propriedades e desenho de objetos.
 */
public class Ally extends superObject{
    
    public Ally(){
        // Define o nome deste tipo de aliado
        name = "Teacher";
        
        // Carrega a imagem do aliado do arquivo
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/res/teacher.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
