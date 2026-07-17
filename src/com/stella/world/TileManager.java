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
    public String map;
    // Array com todos os tipos de tiles disponíveis
    public Tile[] tile;
    
    // Mapa com os índices de tiles em cada posição
    public int mapTileNum[][];

    public TileManager(GamePanel gp) {
        this.gp = gp;
        
        // Cria array para os tiles da fase 1
        tile = new Tile[18];
        
        // Cria o mapa com o tamanho do mundo
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];
        
        getTileImage();
        LoadMap(gp.mapFile);
    }

    /**
     * Carrega as imagens de todos os tiles de 0 a 15.
     * As imagens estão em /res/tile/tile000.png, tile001.png, etc.
     */
    public void getTileImage(){
        for (int i = 0; i < tile.length; i++) {
            tile[i] = new Tile();
        }

        String normalizedMap = map == null ? "" : map.toLowerCase();
        if (normalizedMap.contains("school") || normalizedMap.contains("hallway") || normalizedMap.contains("fase2") || normalizedMap.contains("fase2")) {
            loadSchoolTileSet();
        } else {
            loadFfpTileSet();
        }
    }

    private void loadFfpTileSet() {
        loadTile(0, "/res/tile/FFP/00.png", false);
        loadTile(1, "/res/tile/FFP/ffp_door1.png", true);
        loadTile(2, "/res/tile/FFP/ffp_door2.png", true);
        loadTile(3, "/res/tile/FFP/ffp_dooropen (1).png", true);
        loadTile(4, "/res/tile/FFP/ffp_doorwh (1).png", true);
        loadTile(5, "/res/tile/FFP/ffp_floor.png", false);
        loadTile(6, "/res/tile/FFP/ffp_lwall.png", true);
        loadTile(7, "/res/tile/FFP/ffp_rwall.png", true);
        loadTile(8, "/res/tile/FFP/ffp_stage1.png", true);
        loadTile(9, "/res/tile/FFP/ffp_stage2.png", true);
        loadTile(10, "/res/tile/FFP/ffp_table1.png", true);
        loadTile(11, "/res/tile/FFP/ffp_table2.png", true);
        loadTile(12, "/res/tile/FFP/ffp_wall1.png", true);
        loadTile(13, "/res/tile/FFP/ffp_wall2 _poster03.png", true);
        loadTile(14, "/res/tile/FFP/ffp_wall2.png", true);
        loadTile(15, "/res/tile/FFP/ffp_wall2_poster02 (1).png", true);
        loadTile(16, "/res/tile/FFP/ffp_wall2_posters01.png", true);
        loadTile(17, "/res/tile/FFP/ffp_wall3.png", true);
    }

    private void loadSchoolTileSet() {
        // Ordem numérica dos tiles da escola para os mapas da escola
        loadTile(0, "/res/tile/escolassprite/00.png", false);
        loadTile(1, "/res/tile/escolassprite/border.png", true);
        loadTile(2, "/res/tile/escolassprite/door1.png", true);
        loadTile(3, "/res/tile/escolassprite/door2.png", true);
        loadTile(4, "/res/tile/escolassprite/floor.png", false);
        loadTile(5, "/res/tile/escolassprite/lockerd_1.png", true);
        loadTile(6, "/res/tile/escolassprite/lockerd_2.png", true);
        loadTile(7, "/res/tile/escolassprite/lockerd_3.png", true);
        loadTile(8, "/res/tile/escolassprite/lockeru_1.png", true);
        loadTile(9, "/res/tile/escolassprite/lockeru_2.png", true);
        loadTile(10, "/res/tile/escolassprite/lockeru_3.png", true);
        loadTile(11, "/res/tile/escolassprite/l_borderwall.png", true);
        loadTile(12, "/res/tile/escolassprite/r_borderwall.png", true);
        loadTile(13, "/res/tile/escolassprite/wall1.png", true);
        loadTile(14, "/res/tile/escolassprite/wall2.png", true);
        loadTile(15, "/res/tile/escolassprite/wall3.png", true);
        loadTile(16, "/res/tile/escolassprite/floor.png", false);
        loadTile(17, "/res/tile/escolassprite/floor.png", false);
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

    private InputStream openMapStream(String resourcePath) {
        String[] candidates = new String[] {
            resourcePath,
            "/res/levels/fase2/" + resourcePath.replace("/res/levels/", ""),
            "/res/levels/fase1/" + resourcePath.replace("/res/levels/", "")
        };

        for (String candidate : candidates) {
            InputStream is = getClass().getResourceAsStream(candidate);
            if (is != null) {
                return is;
            }
        }
        return null;
    }

    /**
     * Carrega o mapa do arquivo /res/map.txt.
     * Cada linha tem números separados por espaço, cada número é um índice de tile.
     */
    public void LoadMap(String mapFile) {
        
        getTileImage();
        String mapPath = "/res/levels/" + mapFile;
        try (InputStream is = openMapStream(mapPath)) {
            if (is == null) {
                System.err.println("Arquivo de mapa não encontrado: " + mapPath);
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
                if (line.trim().isEmpty()) continue;

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
                
                // Indice do tile nesta posicao
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
