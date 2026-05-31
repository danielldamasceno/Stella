package com.stella.assets;

import com.stella.core.GamePanel;
import com.stella.entities.Ally;
import com.stella.entities.Enemy;
import com.stella.util.RandomUtils;

/**
 * Gerencia a criação e colocação de objetos no mundo (inimigos, itens, etc).
 * É responsável por inicializar todos os elementos que não são jogador ou mapa.
 */
public class AssetSetter {
    GamePanel gp;
    public int qtdEnemy = RandomUtils.randInt(8,10); // Define a quantidade de inimigos a serem criados

    public AssetSetter(GamePanel gp){
        this.gp = gp;
    }

    /**
     * Coloca os objetos do jogo no mundo.
     * Cria inimigos e define suas posições iniciais.
     */
    public void setObject(){
        
        // Ally:
        gp.obj[0] = new Ally();
        // Define a posição inicial do aliado (em tiles do mundo)
        gp.obj[0].WorldX = 3 * gp.tileSz + gp.tileSz/2;
        gp.obj[0].WorldY = 45 * gp.tileSz + gp.tileSz/2;
        // Marca como aliado
        gp.obj[0].ally = true;
        // Assegura que o aliado não seja marcado como inimigo
        if (gp.obj[0] != null) gp.obj[0].enemy = false;
        // Define o tamanho do aliado
        gp.obj[0].height = 3 * gp.tileSz;
        gp.obj[0].width = 2 * gp.tileSz;

        // Enemys:
        // Cria novos inimigos
         for(int i = 1; i < qtdEnemy; i++) {
             gp.obj[i] = new Enemy(gp);
         }
       
        // Define a posição inicial do inimigo (em tiles do mundo) 

        //fixos:
        gp.obj[1].WorldX = (29 - 2)* gp.tileSz + gp.tileSz/2;
        gp.obj[1].WorldY = (21 - 2) * gp.tileSz + gp.tileSz/2;
        gp.obj[1].enemyType = "static";

        gp.obj[2].WorldX = (28 - 2)* gp.tileSz + gp.tileSz/2;
        gp.obj[2].WorldY = (22 - 2) * gp.tileSz + gp.tileSz/2;
        gp.obj[2].enemyType = "static";

        gp.obj[3].WorldX = (30  - 2)* gp.tileSz + gp.tileSz/2;
        gp.obj[3].WorldY = (23 - 2) * gp.tileSz + gp.tileSz/2;
        gp.obj[3].enemyType = "persuer";

        //dinamicos:
        /*  
            Não em estagio final
            Forma a aleatorizar os inimigos deve ser baseada em tiles spawnaveis
        */
        for (int i = 4; i < qtdEnemy; i++) { 
            int numInimigo = RandomUtils.randInt(1, 3);
            switch (numInimigo) {
                case 1:
                    if (gp.obj[i] == null) continue;
                    gp.obj[i].WorldX = RandomUtils.randInt(5-2, 8-2) * gp.tileSz + gp.tileSz/2;
                    gp.obj[i].WorldY = RandomUtils.randInt(8-2, 19-2) * gp.tileSz + gp.tileSz/2;
                    break;
                case 2:
                    if (gp.obj[i] == null) continue;
                    gp.obj[i].WorldX = RandomUtils.randInt(8-2, 46-2) * gp.tileSz + gp.tileSz/2;
                    gp.obj[i].WorldY = RandomUtils.randInt(8-2, 11-2) * gp.tileSz + gp.tileSz/2;
                    break;
                case 3:
                    if (gp.obj[i] == null) continue;
                    gp.obj[i].WorldX = RandomUtils.randInt(32-2, 35-2) * gp.tileSz + gp.tileSz/2;
                    gp.obj[i].WorldY = RandomUtils.randInt(35-2, 43-2) * gp.tileSz + gp.tileSz/2;
                    break;
            }
        }
        
        
        // Define o tamanho dos inimigos (3x3 tiles) e marca-os como inimigos
        for (int i = 1; i < qtdEnemy; i++) {
            if (gp.obj[i] == null) continue;
            gp.obj[i].height = 3 * gp.tileSz;
            gp.obj[i].width = 3 * gp.tileSz;
            gp.obj[i].enemy = true;
        }

        // Define o tipo de inimigo
        for (int i = 4; i < qtdEnemy; i++) {
            if (gp.obj[i] == null) continue;
            int typeEnemy = RandomUtils.randInt(1, 2);
            if (typeEnemy == 1) {
                gp.obj[i].enemyType = "persuer";
            } else {
                gp.obj[i].enemyType = "static";
            }
        }
    }
}
