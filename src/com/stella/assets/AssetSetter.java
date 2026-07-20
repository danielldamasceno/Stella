package com.stella.assets;

import com.stella.core.GamePanel;
import com.stella.entities.Ally;
import com.stella.entities.Enemy;
import com.stella.entities.InteractionBlock;
import com.stella.entities.superObject;
import com.stella.util.RandomUtils;


public class AssetSetter {
    GamePanel gp;
    int enemyIndex = 1;
    int item, invasorSpawnCol, invasorSpawnRow;
    
    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * Procura o tile do tipo targetTile mais próximo da posição (col,row).
     * Retorna int[]{col,row} ou null se não encontrar dentro do raio.
     */
    /*private int[] findNearestTileOfType(int col, int row, int targetTile, int maxRadius) {
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
    } */

    public void setObject(int FASE_STATE, String mapFile) {
        String spawnFile = null;
        resetNPC();
        if (mapFile == null) {
            spawnFile = getSpawnMapFile(FASE_STATE);
        } else {
            spawnFile = mapFile;
        }

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
        if (FASE_STATE != 1) scanMapForEnemySpawns(FASE_STATE);
        scanMapForObjSpawns(FASE_STATE);

    }

    public String getTileMapFile(int faseState) {
        return switch (faseState) {
            case 1 -> "fase1/maptile.txt";
            case 2 -> "fase2/spawnMapSchool.txt";
            case 3 -> "fase3/spawnSala.txt";
            default -> "fase1/maptile.txt";
        };
    }

    public String getSpawnMapFile(int faseState) {
        return switch (faseState) {
            case 1 -> "fase1/mapspawn.txt";
            case 2 -> "fase2/spawnMapSchool.txt";
             case 3 -> getFase3SpawnFile(gp.player.currentRoom);
            default -> null;
        };
    }

    public String getFase3SpawnFile(String room) {
    return switch (room) {
        case "mapSala" -> "fase3/spawnMapSala.txt";
        case "mapQuarto" -> "fase3/spawnMapQuarto.txt";
        case "mapCozinha" -> "fase3/spawnMapCozinha.txt";
        default -> "fase3/spawnMapSala.txt";
    };
}

    public void resetNPC() {
        for (int i = 0; i < gp.obj.length; i++) {
            gp.obj[i] = null;
        }
    }

    public void scanMapForEnemySpawns(int faseState) {
        enemyIndex = 1;

        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int code = gp.spawnM.getSpawnCode(col, row);
                if (code == 0) continue;
                if (faseState == 2) {
                    switch (code) {
                        case 16 -> { // aliado
                            if("estojo".equals(gp.player.inventario[0])) {
                                Ally a = new Ally();
                                placeAt(a, col, row, 2, 3);
                                gp.obj[0] = a;
                            }
                        }
                        case 17 -> { // inimigo dinamico
                            if (enemyIndex < gp.obj.length) {
                                if(RandomUtils.randInt(1, 4) != 1) {
                                    Enemy e = new Enemy(gp);
                                    placeAt(e, col, row, 3, 3);
                                    e.enemyType = "static";
                                    gp.obj[enemyIndex++] = e;
                                }
                            }      
                        }
                        case 18 -> { // inimigo fixo
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
                }   else if (faseState == 3) {
                    switch (code) {
                        /*case 39 -> { // inimigo fixo
                            if (enemyIndex < gp.obj.length) {
                                Enemy e = new Enemy(gp);
                                placeAt(e, col, row, 3, 3);
                                e.enemyType = "static";
                                gp.obj[enemyIndex++] = e;
                            }
                        }*/
                        case 40 -> { //porta de saida
                            spawnObjIntereactionBlock(col, row, "Sair da casa (aperte E)", null);
                        }
                    }

                }
            }
        }

    }

    public void scanMapForObjSpawns(int faseState) {
        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int code = gp.spawnM.getSpawnCode(col, row);
                if (code == 0) continue;
                
                if (faseState == 1 || faseState == 2) {
                    switch (code) {
                        case 1 -> { // porta/interação da fase 1
                            if(faseState == 1) {
                                InteractionBlock block = new InteractionBlock(gp);
                                placeAt(block, col, row, 1, 1);
                                block.dialogueOptions = getDoorDialogueOptions();
                                gp.obj[enemyIndex++] = block;
                            };
                        }

                        case 2 -> { // porta/interação da fase 2
                            if (faseState == 2) {
                                spawnObjIntereactionBlock(col, row, "Sair da sala (aperte E)", null);
                            }
                        }
                        case 19 -> { // safezone
                            if (faseState == 1) {
                            spawnObjIntereactionBlock(col, row, "Acessar sala (aperte E)", new String[] {"Com muito custo, achei a sala."});
                            }
                        }
                        case 20 -> {
                            if (faseState == 2) {
                                spawnObjIntereactionBlock(col, row, "Sala de Ciências (aperte E)", null);
                            }
                        }
                        case 21 -> {
                            if (faseState == 2) {
                                spawnObjIntereactionBlock(col, row, "Sala de Matemática (aperte E)", null);
                            }
                        }
                        case 22 -> {
                            if (faseState == 2) {
                                spawnObjIntereactionBlock(col, row, "Sala de Português (aperte E)", null);
                            }
                        }
                        case 23 -> {
                            if (faseState == 2) {
                                spawnObjIntereactionBlock(col, row, "Laboratório de Informática (aperte E)", null);
                            }
                        }
                        case 24 -> {
                            if (faseState == 2) {
                                spawnObjIntereactionBlock(col, row, "Laboratório de Física (aperte E)", null);
                            }
                        }
                        case 25 -> {
                            if (faseState == 2) {
                                spawnObjIntereactionBlock(col, row, "Sala (aperte E)", new String[] {"Essa porta está trancada!"});
                            }
                        }
                        case 26 -> {
                            if (faseState == 1) continue;
                            spawnItemInteractionBlock(col, row, "Pegar o Estojo (aperte E)", new String[] {"Ufa! Enfim achei meu estojo. Preciso encontrar a professora e contar o que aconteceu."}, "/res/levelsimage/level2/estojo.png");
                        }
                    }
                } else if (faseState == 3) {
                    switch (code) {
                        case 41 -> {
                        invasorSpawnCol = col;
                        invasorSpawnRow = row;
                        }
                        case 42 -> { // porta/interação da fase 3
                            spawnObjIntereactionBlock(col, row, "Entrar na cozinha (aperte E)", null);
                        }
                        case 43 -> { // porta/interação da fase 3
                            spawnObjIntereactionBlock(col, row, "Entrar no quarto (aperte E)", null);
                        }
                        case 44 -> { //voltar pra sala
                            spawnObjIntereactionBlock(col, row, "Voltar para a sala (aperte E)", null);
                        }
                        case 45 -> { //item
                                if (!gp.isInvasorTriggered()) break; // itens só liberam depois da primeira aparição
                                if (gp.player.currentRoom == "mapSala" && !"wallet".equals(gp.player.inventario[0]))
                                    spawnObjIntereactionBlock(col, row, "Pegar a carteira (aperte E)", new String[] {"Achei a carteira."});
                                if (gp.player.currentRoom == "mapQuarto" && !"phone".equals(gp.player.inventario[1]))
                                    spawnObjIntereactionBlock(col, row, "Pegar o celular (aperte E)", new String[] {"No meu quarto, como sempre."});
                                if (gp.player.currentRoom == "mapCozinha" && !"key".equals(gp.player.inventario[2]))
                                    spawnObjIntereactionBlock(col, row, "Pegar as chaves (aperte E)", new String[] {"Minhas chaves! Ainda bem que lembrei onde estavam."});
                        }
                    }
                }
            }
        }
    }

    public Enemy spawnInvasor() {
        if (enemyIndex >= gp.obj.length) return null;

        int col = invasorSpawnCol;
        int row = invasorSpawnRow;

        if (col < 0 || row < 0) {
            col = (int) (gp.player.worldX / gp.tileSz) - 3;
            row = (int) (gp.player.worldY / gp.tileSz);
        }

        Enemy e = new Enemy(gp);
        placeAt(e, col, row, 3, 3);
        e.enemyType = "persuer";
        gp.obj[enemyIndex++] = e;
        return e;
    }

    private void spawnObjIntereactionBlock(int col, int row, String promptText, String[] dialogueLines) {
        if (enemyIndex >= gp.obj.length) return;
        InteractionBlock block = new InteractionBlock(gp);
        placeAt(block, col, row, 1, 1);
        block.promptText = promptText;
        if(dialogueLines != null) block.dialogueLines = dialogueLines;
        gp.obj[enemyIndex++] = block;
    }

    private void spawnItemInteractionBlock(int col, int row, String promptText, String[] dialogueLines, String imagepath) {
        if (enemyIndex >= gp.obj.length) return;
        InteractionBlock item = new InteractionBlock(gp);
        item = new InteractionBlock(gp, imagepath);
        placeAt(item, col, row, 1, 1);
        item.promptText = promptText;
        if(dialogueLines != null) item.dialogueLines = dialogueLines;
        gp.obj[enemyIndex++] = item;
        this.item = enemyIndex - 1;
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