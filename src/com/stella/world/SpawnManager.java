package com.stella.world;

import com.stella.core.GamePanel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SpawnManager {
    GamePanel gp;
    public String spawnMap;
    public int spawnTileNum[][];

    public SpawnManager(GamePanel gp) {
        this.gp = gp;
        spawnTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];
    }

    public void loadSpawnMap(String fileName) {
        this.spawnMap = fileName;
        for (int col = 0; col < gp.maxWorldCol; col++)
            for (int row = 0; row < gp.maxWorldRow; row++)
                spawnTileNum[col][row] = 0;

        String[] candidates = new String[] {
            "/res/levels/" + fileName,
            "/res/levels/fase2/" + fileName,
            "/res/levels/fase1/" + fileName
        };

        InputStream is = null;
        for (String candidate : candidates) {
            is = getClass().getResourceAsStream(candidate);
            if (is != null) {
                break;
            }
        }

        try (InputStream stream = is) {
            if (stream == null) {
                System.err.println("Arquivo de spawn não encontrado: " + fileName);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            int row = 0;
            while (row < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) break;
                if (line.trim().isEmpty()) continue;
                String[] numbers = line.trim().split("\\s+");
                for (int col = 0; col < gp.maxWorldCol; col++) {
                    spawnTileNum[col][row] = Integer.parseInt(numbers[col]);
                }
                row++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getSpawnCode(int col, int row) {
        if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) return 0;
        return spawnTileNum[col][row];
    }
}