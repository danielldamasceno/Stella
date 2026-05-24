package com.stella.core;

import javax.swing.JFrame;

/**
 * Classe principal que inicia o jogo.
 * Cria a janela e o painel de jogo.
 */
public class App {
    public static void main(String[] args) throws Exception {
        // Cria a janela principal
        JFrame window = new JFrame("Stella");
        
        // Configura para fechar ao clicar no X
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Não permite redimensionar
        window.setResizable(true);
        
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
        
        // Mostra a janela
        window.setVisible(true);
    }
}
