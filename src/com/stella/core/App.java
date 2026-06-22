package com.stella.core;

import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;



/**
 * Classe principal que inicia o jogo.
 * Cria a janela e o painel de jogo.
 */
public class App {
    static int fase;
    public static void main(String[] args) throws Exception {
        // Cria a janela principal
        JFrame window = new JFrame("Stella");
        
        
        // Configura para fechar ao clicar no X
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Não permite redimensionar
        window.setResizable(false);
        
        // Cria o painel do jogo
        GamePanel panel = new GamePanel();
        
        // Adiciona o painel à janela
        window.add(panel);
        
        // Inicializa os objetos do jogo
        panel.setupGame();
        
        // Ajusta o tamanho da janela ao conteúdo
        window.pack();
        
        // Coloca a janela no centro da tela
        window.setLocationRelativeTo(null);
        
        // Muda o ícone da janela para o sprite do personagem
        try {
        BufferedImage icon = ImageIO.read(App.class.getResourceAsStream("/res/MC/frontidle.png"));
        window.setIconImage(icon);
        } catch (IOException e) {
        e.printStackTrace();
        }
        
        // Mostra a janela
        window.setVisible(true);
    }
}
