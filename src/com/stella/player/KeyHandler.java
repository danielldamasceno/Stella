package com.stella.player;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Gerencia as entradas do teclado do jogador.
 * Detecta quando as teclas WASD são pressionadas ou soltas.
 */
public class KeyHandler implements KeyListener{
    // Estados de cada direção (pressionado = true, solto = false)
    public boolean leftPressed, rightPressed, upPressed, downPressed, enterPressed, interactPressed;
    
    /**
     * Detecta quando uma tecla é pressionada.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
    
        // Controles: A = esquerda, D = direita, W = cima, S = baixo
        if(code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT){
            this.leftPressed = true;
        }
        if(code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT){
            this.rightPressed = true;
        }
        if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN){
            this.downPressed = true;
        }
        if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP){
            this.upPressed = true;
        }
        if(code == KeyEvent.VK_ENTER){
            this.enterPressed = true;
        }
        if(code == KeyEvent.VK_E){
            this.interactPressed = true;
        }
    }

    /**
     * Detecta quando uma tecla é solta.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        
        if(code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT){
            this.leftPressed = false;
        }
        if(code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT){
            this.rightPressed = false;
        }
        if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN){
            this.downPressed = false;
        }
        if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP){
            this.upPressed = false;
        }
        if(code == KeyEvent.VK_ENTER){
            this.enterPressed = false;
        }
        if(code == KeyEvent.VK_E){
            this.interactPressed = false;
        }
    }

    /**
     * Não é usado, mas é obrigatório implementar (parte da interface KeyListener).
     */
    @Override
    public void keyTyped(KeyEvent arg0) {
       
    }
}
