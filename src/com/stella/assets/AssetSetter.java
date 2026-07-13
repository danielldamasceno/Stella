package com.stella.assets;

import com.stella.core.GamePanel;
import com.stella.entities.Ally;
import com.stella.entities.Enemy;
import com.stella.entities.InteractionBlock;
import com.stella.entities.StepDialogueObject;
import com.stella.entities.superObject;

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
        scanMapForSpawns();

        // Garante que a professora (ally) exista na fase da escola
        if (gp.FASE_STATE == 2) {
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
        }
    }

    public String getTileMapFile(int faseState) {
        return switch (faseState) {
            case 1 -> "fase1/maptile.txt";
            case 2 -> "mapSchool.txt";
            case 3 -> "mapSchool.txt";
            default -> "fase1/maptile.txt";
        };
    }

    public String getSpawnMapFile(int faseState) {
        return switch (faseState) {
            case 1 -> "fase1/mapspawn.txt";
            case 2 -> "spawnMapSchool.txt";
            case 3 -> "spawnMap2.txt";
            case 4 -> "spawnMapSchool.txt";
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
        // Cria lista de tiles do tipo 4 (piso) para uso em spawns aleatórios
        java.util.List<int[]> floorTiles = new java.util.ArrayList<>();
        for (int c = 0; c < gp.maxWorldCol; c++) {
            for (int r = 0; r < gp.maxWorldRow; r++) {
                try {
                    if (gp.tileManager.mapTileNum[c][r] == 4) {
                        floorTiles.add(new int[] {c, r});
                    }
                } catch (Exception ignored) {}
            }
        }

        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                if (enemyIndex >= gp.obj.length) {
                    return;
                }

                int code = gp.spawnM.getSpawnCode(col, row);
                if (code == 0) continue;

                // Para a fase "mapSchool" (FASE_STATE == 2), só spawnar inimigos nos tiles de piso (código 4)
                int spawnCol = col;
                int spawnRow = row;
                if (gp.FASE_STATE == 2 && (code == 12 || code == 13)) {
                    int mapTileCode = gp.tileManager.mapTileNum[col][row];
                    if (mapTileCode != 4) {
                        // procura o tile 4 mais próximo (raio até 3)
                        int[] found = findNearestTileOfType(col, row, 4, 6);
                        if (found == null) continue; // nada encontrado -> pula spawn
                        spawnCol = found[0];
                        spawnRow = found[1];
                    }
                }

                if (gp.FASE_STATE == 1) {
                    if (code == 1 && enemyIndex < gp.obj.length) {
                            InteractionBlock block = new InteractionBlock(gp);
                            placeAt(block, spawnCol, spawnRow, 1, 1);
                        block.dialogueOptions = getDoorDialogueOptions();
                        gp.obj[enemyIndex++] = block;
                    }
                    if (code == 30 && enemyIndex < gp.obj.length) {
                        InteractionBlock block = new InteractionBlock(gp);
                            placeAt(block, spawnCol, spawnRow, 1, 1);
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
                            placeAt(block, spawnCol, spawnRow, 1, 1);
                            block.dialogueOptions = getDoorDialogueOptions();
                            gp.obj[enemyIndex++] = block;
                        }
                    }
                    case 11 -> { // aliado
                        Ally a = new Ally();
                        placeAt(a, spawnCol, spawnRow, 2, 3);
                        gp.obj[0] = a;
                    }
                    case 56 -> { // aliado (marcador 56 no spawnMap)
                        Ally a = new Ally();
                        placeAt(a, spawnCol, spawnRow, 2, 3);
                        gp.obj[0] = a;
                    }
                    case 4 -> { // inimigo dinamico (distribuído aleatoriamente sobre tiles do tipo 4)
                        if (enemyIndex < gp.obj.length && !floorTiles.isEmpty()) {
                            int spawnCount = 2;
                            for (int s = 0; s < spawnCount && enemyIndex < gp.obj.length && !floorTiles.isEmpty(); s++) {
                                int idx = com.stella.util.RandomUtils.randInt(0, floorTiles.size() - 1);
                                int[] chosen = floorTiles.remove(idx);
                                Enemy e = new Enemy(gp);
                                placeAt(e, chosen[0], chosen[1], 3, 3);
                                e.enemyType = "static";
                                gp.obj[enemyIndex++] = e;
                            }
                        }
                    }
                    case 13 -> { // inimigo fixo (distribuído aleatoriamente sobre tiles do tipo 4)
                        if (enemyIndex < gp.obj.length && !floorTiles.isEmpty()) {
                            int spawnCount = 2;
                            for (int s = 0; s < spawnCount && enemyIndex < gp.obj.length && !floorTiles.isEmpty(); s++) {
                                int idx = com.stella.util.RandomUtils.randInt(0, floorTiles.size() - 1);
                                int[] chosen = floorTiles.remove(idx);
                                Enemy e = new Enemy(gp);
                                placeAt(e, chosen[0], chosen[1], 3, 3);
                                e.enemyType = (s == 0) ? "static" : "persuer";
                                gp.obj[enemyIndex++] = e;
                            }
                        }
                    }
                    case 15 -> { // objeto que mostra diálogo ao passar por cima
                        StepDialogueObject step = new StepDialogueObject(gp);
                        placeAt(step, spawnCol, spawnRow, 1, 1);
                        step.dialogueLines = new String[] {
                            "Você passou por esta área.",
                            "Este é um aviso automático."
                        };
                        gp.obj[enemyIndex++] = step;
                    }
                    case 30 -> { // safezone
                        InteractionBlock block = new InteractionBlock(gp);
                        placeAt(block, spawnCol, spawnRow, 1, 1);
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