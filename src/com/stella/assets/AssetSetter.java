package com.stella.assets;

import com.stella.core.GamePanel;
import com.stella.entities.Ally;
import com.stella.entities.Enemy;
import com.stella.entities.InteractionBlock;
import com.stella.entities.StepDialogueObject;
import com.stella.entities.superObject;
import com.stella.util.RandomUtils;

public class AssetSetter {
    GamePanel gp;

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * Procura o tile do tipo targetTile mais próximo da posição (col,row).
     * Retorna int[]{col,row} ou null se não encontrar dentro do raio.
     */
    private int[] findNearestTileOfType(int col, int row, int targetTile, int maxRadius) {
        for (int r = 1; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (Math.abs(dx) != r && Math.abs(dy) != r) continue; // borda do quadrado
                    int nc = col + dx;
                    int nr = row + dy;
                    if (nc < 0 || nr < 0 || nc >= gp.maxWorldCol || nr >= gp.maxWorldRow) continue;
                    if (gp.tileManager.mapTileNum[nc][nr] == targetTile) {
                        return new int[] {nc, nr};
                    }
                }
            }
        }
        return null;
    }

    public void setObject(int FASE_STATE) {
        resetNPC();

        String spawnFile = getSpawnMapFile(FASE_STATE);
        if (spawnFile == null) return;

        gp.spawnM.loadSpawnMap(spawnFile);
        // Aumenta spawn automaticamente: onde o mapa tiver tile 4 (piso) e
        // o spawn map estiver zerado, há uma chance baixa de marcar como 12.
        for (int c = 0; c < gp.maxWorldCol; c++) {
            for (int r = 0; r < gp.maxWorldRow; r++) {
                try {
                    if (gp.tileManager.mapTileNum[c][r] == 4 && gp.spawnM.spawnTileNum[c][r] == 0) {
                        // chance aumentada: 1 em 3 (~33%)
                        if (com.stella.util.RandomUtils.randInt(1, 3) == 1) {
                            gp.spawnM.spawnTileNum[c][r] = 12;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        System.out.println(FASE_STATE);
        scanMapForSpawns(FASE_STATE);

        // Garante que a professora (ally) exista na fase da escola
        /*if (gp.FASE_STATE == 2) {
            try {
                Ally teacher = new Ally();
                // Define tamanho próximo ao usado para aliados nos spawns
                teacher.width = Math.max(8, 3 * gp.tileSz / 2);
                teacher.height = Math.max(8, 3 * gp.tileSz / 2);
                // Posicionamento em coordenadas do mundo conforme pedido
                teacher.WorldX = 500.0;
                teacher.WorldY = 3104.0;
                teacher.enemy = false;
                teacher.ally = true;
                // Coloca na posição reservada para aliado (índice 0)
                gp.obj[0] = teacher;
            } catch (Exception ignored) {}
        }*/
    }

    public String getTileMapFile(int faseState) {
        return switch (faseState) {
            case 1 -> "fase1/maptile.txt";
            case 2 -> "fase2/spawnMapSchool.txt";
            case 3 -> "fase2/mapSchool.txt";
            default -> "fase1/maptile.txt";
        };
    }

    public String getSpawnMapFile(int faseState) {
        return switch (faseState) {
            case 1 -> "fase1/mapspawn.txt";
            case 2 -> "fase2/spawnMapSchool.txt";
            case 3 -> "fase2/spawnMap2.txt";
            case 4 -> "fase2/spawnMapSchool.txt";
            default -> null;
        };
    }

     private void resetNPC() {
        for (int i = 0; i < gp.obj.length; i++) {
            gp.obj[i] = null;
        }
    }

    private void scanMapForSpawns(int faseState) {
        int enemyIndex = 1; 

        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int code = gp.spawnM.getSpawnCode(col, row);
                if (code == 0) continue;

                switch (code) {
                    case 1 -> { // porta/interação da fase 1
                        if(faseState == 1) {
                            if (enemyIndex < gp.obj.length) {
                            InteractionBlock block = new InteractionBlock(gp);
                            placeAt(block, col, row, 1, 1);
                            block.dialogueOptions = getDoorDialogueOptions();
                            gp.obj[enemyIndex++] = block;
                            }
                        }
                    }

                    case 16 -> { // aliado
                        if (faseState == 2) {
                            Ally a = new Ally();
                            placeAt(a, col, row, 2, 3);
                            gp.obj[0] = a;
                        }
                    }
                    case 17 -> { // inimigo dinamico
                        if (faseState == 2) {
                            if (enemyIndex < gp.obj.length) {
                                if(RandomUtils.randInt(1, 4) != 1) {
                                    Enemy e = new Enemy(gp);
                                    placeAt(e, col, row, 3, 3);
                                    e.enemyType = "static";
                                    gp.obj[enemyIndex++] = e;
                                }
                            
                            }
                        }
                    }
                    case 18 -> { // inimigo fixo
                        if (faseState == 2) {
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
                    case 19 -> { // safezone
                        if (faseState == 1) {
                        InteractionBlock block = new InteractionBlock(gp);
                        placeAt(block, col, row, 1, 1);
                        block.dialogueLines = new String[] {"Com muito custo, achei a sala."};
                        block.promptText = "Acessar sala (aperte E)";
                        gp.obj[enemyIndex++] = block;
                        }
                    }
                }
            }
        }

    }

    private String[][] getDoorDialogueOptions() {
        return new String[][] {
            new String[] {
                "Homem 1: \"Ela ainda tá lá fora esperando os pais.\"",
                "Homem 2: \"Espera mais um pouco. Quando a rua esvaziar, a gente vê.\""
            },
            new String[] {
                "Homem 1: \"Aquela menina entrou sozinha faz um tempo.\"",
                "Homem 2: \"Fica de olho. Se ela se separar dos outros, me avisa.\""
            },
            new String[] {
                "Homem 1: \"Ela parece confiar em qualquer adulto.\"",
                "Homem 2: \"É... isso pode facilitar as coisas.\""
            },
            new String[] {
                "Homem 1: \"Os pais dela parecem distraídos.\"",
                "Homem 2: \"Melhor esperar eles irem pro caixa.\""
            },
            new String[] {
                "Homem 1: \"Você viu aquela criança andando pelos corredores?\"",
                "Homem 2: \"Vi. Melhor ninguém perceber que estamos observando.\""
            },
            new String[] {
                "Homem 1: \"Ela passou por aqui duas vezes.\"",
                "Homem 2: \"Continua olhando. Não faz nada enquanto tiver muita gente.\""
            }
        };
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