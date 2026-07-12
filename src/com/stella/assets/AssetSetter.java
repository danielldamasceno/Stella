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

    public void setObject(int FASE_STATE) {
        resetNPC();

        String spawnFile = getSpawnMapFile(FASE_STATE);
        if (spawnFile == null) return;

        gp.spawnM.loadSpawnMap(spawnFile);
        scanMapForSpawns();
    }

    public String getTileMapFile(int faseState) {
        return switch (faseState) {
            case 1 -> "fase1/maptile.txt";
            case 2 -> "mapSchool.txt";
            case 3 -> "map2.txt";
            default -> "fase1/maptile.txt";
        };
    }

    public String getSpawnMapFile(int faseState) {
        return switch (faseState) {
            case 1 -> "fase1/mapspawn.txt";
            case 2 -> "spawnMapSchool.txt";
            case 3 -> "spawnMap2.txt";
            default -> null;
        };
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
                if (enemyIndex >= gp.obj.length) {
                    return;
                }

                int code = gp.spawnM.getSpawnCode(col, row);
                if (code == 0) continue;

                if (gp.FASE_STATE == 1) {
                    if (code == 1 && enemyIndex < gp.obj.length) {
                        InteractionBlock block = new InteractionBlock(gp);
                        placeAt(block, col, row, 1, 1);
                        block.dialogueOptions = getDoorDialogueOptions();
                        gp.obj[enemyIndex++] = block;
                    }
                    if (code == 30 && enemyIndex < gp.obj.length) {
                        InteractionBlock block = new InteractionBlock(gp);
                        placeAt(block, col, row, 1, 1);
                        block.dialogueLines = new String[] {"Com muito custo, achei a sala."};
                        block.promptText = "Acessar sala";
                        gp.obj[enemyIndex++] = block;
                    }
                    continue;
                }

                switch (code) {
                    case 1 -> { // porta/interação da fase 1
                        if (enemyIndex < gp.obj.length) {
                            InteractionBlock block = new InteractionBlock(gp);
                            placeAt(block, col, row, 1, 1);
                            block.dialogueOptions = getDoorDialogueOptions();
                            gp.obj[enemyIndex++] = block;
                        }
                    }
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
                    case 14 -> { // bloco de interação
                        InteractionBlock block = new InteractionBlock(gp);
                        placeAt(block, col, row, 1, 1);
                        block.dialogueLines = new String[] {
                            "Você encontrou um bloco de interação.",
                            "Aperte E para ouvir."
                        };
                        gp.obj[enemyIndex++] = block;
                    }
                    case 15 -> { // objeto que mostra diálogo ao passar por cima
                        StepDialogueObject step = new StepDialogueObject(gp);
                        placeAt(step, col, row, 1, 1);
                        step.dialogueLines = new String[] {
                            "Você passou por esta área.",
                            "Este é um aviso automático."
                        };
                        gp.obj[enemyIndex++] = step;
                    }
                    case 30 -> { // safezone
                        InteractionBlock block = new InteractionBlock(gp);
                        placeAt(block, col, row, 1, 1);
                        block.dialogueLines = new String[] {"Com muito custo, achei a sala."};
                        block.promptText = "Acessar sala";
                        gp.obj[enemyIndex++] = block;
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
        obj.WorldX = (col - 1) * gp.tileSz + (gp.tileSz / 2.0) - (obj.width / 2.0);
        obj.WorldY = (row - 2) * gp.tileSz + (gp.tileSz / 2.0) - (obj.height / 2.0);
        obj.width = Math.max(8, widthInTiles * gp.tileSz / 2);
        obj.height = Math.max(8, heightInTiles * gp.tileSz / 2);
        obj.enemy = (obj instanceof Enemy);
        obj.ally = (obj instanceof Ally);
    }
}