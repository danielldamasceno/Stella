package com.stella.assets;

import com.stella.core.GamePanel;
import com.stella.entities.Enemy;

/**
 * Gerencia a criação e colocação de objetos no mundo (inimigos, itens, etc).
 * É responsável por inicializar todos os elementos que não são jogador ou mapa.
 */
public class AssetSetter {
    GamePanel gp;

    public AssetSetter(GamePanel gp){
        this.gp = gp;
    }

    /**
     * Coloca os objetos do jogo no mundo.
     * Cria inimigos e define suas posições iniciais.
     */
    public void setObject(){
        // Cria um novo inimigo e coloca no primeiro slot de objetos
        gp.obj[0] = new Enemy();
        
        // Define a posição inicial do inimigo (em tiles do mundo)
        gp.obj[0].WorldX = 32 * gp.tileSz + gp.tileSz/2;
        gp.obj[0].WorldY = 3 * gp.tileSz + gp.tileSz/2;
        
        // Define o tamanho do inimigo
        gp.obj[0].height = 3 * gp.tileSz;
        gp.obj[0].width = 3 * gp.tileSz;
        
        // Marca como inimigo (para o sistema de medo do jogador)
        gp.obj[0].enemy = true;
    }
}
