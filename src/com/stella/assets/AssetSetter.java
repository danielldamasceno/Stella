package com.stella.assets;

import com.stella.core.GamePanel;
import com.stella.entities.Ally;
import com.stella.entities.Enemy;
import com.stella.entities.superObject;
import com.stella.util.RandomUtils;
import java.util.ArrayList;
import java.util.List;

public class AssetSetter {
    GamePanel gp;

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject(int FASE_STATE) {
        resetNPC();

        String spawnFile = switch (FASE_STATE) {
            case 1 -> null;
            case 2 -> "spawnMapSchool.txt";
            case 3 -> "spawnMap2.txt";
            default -> null;
        };
        if (spawnFile == null) return;

        gp.spawnM.loadSpawnMap(spawnFile);
        scanMapForSpawns();
    }

    private void resetNPC() {
        for (int i = 0; i < gp.obj.length; i++) {
            gp.obj[i] = null;
        }
    }

    private void scanMapForSpawns() {
        int enemyIndex = 1; 

        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int code = gp.spawnM.getSpawnCode(col, row);
                if (code == 0) continue;

                switch (code) {
                    case 11 -> { // aliado
                        Ally a = new Ally();
                        placeAt(a, col, row, 2, 3);
                        gp.obj[0] = a;
                    }
                    case 12 -> { // inimigo dinamico
                        if (enemyIndex < gp.obj.length) {
                            if(RandomUtils.randInt(1, 4) != 1) {
                                Enemy e = new Enemy(gp);
                                placeAt(e, col, row, 3, 3);
                                e.enemyType = "static";
                                gp.obj[enemyIndex++] = e;
                            }
                        
                        }
                    }
                    case 13 -> { // inimigo fixo 
                        if (enemyIndex < gp.obj.length) {
                            if (RandomUtils.randInt(1, 2) == 1){
                                Enemy e = new Enemy(gp);
                                placeAt(e, col, row, 3, 3);
                                e.enemyType = "static";
                                gp.obj[enemyIndex++] = e;
                            } else {
                                Enemy e = new Enemy(gp);
                                placeAt(e, col, row, 3, 3);
                                e.enemyType = "persuer";
                                gp.obj[enemyIndex++] = e;
                            }
                        }
                    }
                }
            }
        }

    }

    /** Posiciona qualquer superObject (Enemy, Ally, etc) num tile do mapa. */
    private void placeAt(superObject obj, int col, int row, int widthInTiles, int heightInTiles) {
        obj.WorldX = (col-1) * gp.tileSz + gp.tileSz / 2.0;
        obj.WorldY = (row-2) * gp.tileSz + gp.tileSz / 2.0;
        obj.width = widthInTiles * gp.tileSz;
        obj.height = heightInTiles * gp.tileSz;
        obj.enemy = (obj instanceof Enemy);
        obj.ally = (obj instanceof Ally);
    }
}