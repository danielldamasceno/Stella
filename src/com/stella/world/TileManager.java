package com.stella.world;

import com.stella.core.GamePanel;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

/**
 * Gerencia todos os tiles (blocos) do mapa.
 * Carrega as imagens dos tiles e o arquivo de mapa.
 */
public class TileManager {
    GamePanel gp;
    
    // Array com todos os tipos de tiles disponíveis
    public Tile[] tile;
    
    // Mapa com os índices de tiles em cada posição
    public int mapTileNum[][];

    public TileManager(GamePanel gp) {
        this.gp = gp;
        
        // Cria array para 71 tipos diferentes de tiles
        tile = new Tile[71];
        
        // Cria o mapa com o tamanho do mundo
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];
        
        // Carrega as imagens de todos os tiles
        getTileImage();
        
        // Carrega o mapa do arquivo
        LoadMap();
    }

    /**
     * Carrega as imagens de todos os tiles de 0 a 70.
     * As imagens estão em /res/tile/tile000.png, tile001.png, etc.
     */
    public void getTileImage(){
        for (int i = 0; i < tile.length; i++) {
            tile[i] = new Tile();
        }

        // Carrega apenas os tiles usados pelo mapa atual.
        // O map.txt usa 1 para piso e 2 para parede.
        loadTile(1, "/res/tile/floor1.png", false);
        loadTile(2, "/res/tile/wall1.png", true);
        loadTile(3, "/res/tile/floor2.png", false);
        loadTile(4, "/res/tile/floor3.png", false);
        loadTile(5, "/res/tile/wall2.png", true);
        loadTile(6, "/res/tile/wall3.png", true);
        loadTile(7, "/res/tile/armario.png", true);
        loadTile(8, "/res/tile/armariodown.png", true);
    }

    private void loadTile(int index, String path, boolean collision) {
        if (index < 0 || index >= tile.length) return;
        tile[index].collision = collision;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                tile[index].image = ImageIO.read(is);
            } else {
                System.err.println("Imagem do tile não encontrada: " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carrega o mapa do arquivo /res/map.txt.
     * Cada linha tem números separados por espaço, cada número é um índice de tile.
     */
    public void LoadMap() {
        try (InputStream is = getClass().getResourceAsStream("/res/map.txt")) {

            if (is == null) {
                System.err.println("Arquivo de mapa não encontrado");
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int row = 0;

            while (row < gp.maxWorldRow) {
                String line = br.readLine();

                if (line == null) {
                    break;
                }

                // Pula linhas vazias
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Separa os números da linha
                String[] numbers = line.trim().split("\\s+");

                // Preenche o mapa com os índices de tiles
                for (int col = 0; col < gp.maxWorldCol; col++) {
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                }

                row++;
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Desenha os tiles visíveis na câmera.
     * Otimiza para não desenhar tiles que não estão sendo vistos.
     */
    public void Draw(Graphics2D g2){
        // Percorre todas as linhas e colunas do mapa
        for(int i = 0; i < gp.maxWorldRow; i++){
            for(int j = 0; j < gp.maxWorldCol; j++){
                // Posição do tile no mundo
                int WorldX = j * gp.tileSz;
                int WorldY = i * gp.tileSz;

                // Converte para posição na tela (relativa ao jogador)
                // Usa as coordenadas da câmera (posição do mundo no canto superior-esquerdo)
                int ScreenX = WorldX - gp.cameraX;
                int ScreenY = WorldY - gp.cameraY;
                
                // Índice do tile nesta posição
                int tilenum = mapTileNum[j][i];
                
                if (tilenum < 0 || tilenum >= tile.length) continue;
                if (tile[tilenum].image == null) continue;
                
                // Só desenha se o tile estiver dentro dos limites da tela (usando câmera)
                if (ScreenX + gp.tileSz > 0 && ScreenX - gp.tileSz < gp.screenWidth &&
                    ScreenY + gp.tileSz > 0 && ScreenY - gp.tileSz < gp.screenHeight) {
                    g2.drawImage(tile[tilenum].image, ScreenX, ScreenY, gp.tileSz, gp.tileSz, null);
                }
            }
        }
    }
}
