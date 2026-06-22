    package com.stella.core;
    import java.awt.Color;
    import java.awt.Dimension;
    import java.awt.Font;
    import java.awt.Graphics;
    import java.awt.Graphics2D;
    import java.awt.Rectangle;
    import java.awt.TexturePaint;
    import java.awt.image.BufferedImage;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.io.IOException;
    import javax.imageio.ImageIO;
    import javax.swing.JButton;
    import javax.swing.JPanel;   
    import com.stella.player.Fear;
    import com.stella.player.Player;
    import com.stella.player.KeyHandler;
    import com.stella.world.SpawnManager;
    import com.stella.world.TileManager;
    import com.stella.entities.Enemy;
    import com.stella.entities.superObject;
    import com.stella.physics.CollisionChecker;
    import com.stella.assets.AssetSetter;

        /**
         * Painel principal do jogo.
         * Gerencia toda a lógica de jogo, renderização e loop do jogo.
         */
    public class GamePanel extends JPanel implements Runnable{
            // Configurações de câmera e mundo
        final int ogTileSz = 24;           // Tamanho original dos sprites
        final int scale = 3;               // Escala para ampliação
        public final int tileSz = ogTileSz*scale; // Tamanho final dos tiles (72px)
        final int maxScreenCol = 20;       // Quantas colunas de tiles cabem na tela
        final int maxScreenRow = 11;       // Quantas linhas de tiles cabem na tela

            // Tamanho do mundo
        public final int maxWorldCol = 50;     // Quantas colunas de tiles no mundo
        public final int maxWorldRow = 50;     // Quantas linhas de tiles no mundo
        public final int worldWidth = maxWorldCol * tileSz;
        public final int worldHeight = maxWorldRow * tileSz;

            // Tamanho da tela em pixels
        public final int screenWidth = tileSz * maxScreenCol;  // 1440px
        public final int screenHeight = tileSz * maxScreenRow; // 864px

        // Estados do jogo
        public static final int TITLE_STATE = 0;  // Tela de título
        public static final int PLAY_STATE = 1;   // Jogo em andamento
        public static final int DIALOG_STATE = 5;
        public static final int TRANS_STATE = 6;
        public static final int FADE_STATE = 4;
        public static final int FADE_IN_STATE = 7;
        public static final int FADE_OUT_STATE = 8;
        public static final int LOADING_STATE = 9;

        //Condições win/lose
        public static final int GAME_OVER_STATE = 2;  // Jogo acabou (derrota)
        public static final int VICTORY_STATE = 3;    // Jogo acabou (vitória)

            

        public int FASE_STATE = 2;
        private float fadeAlpha = 0f;      // 0 = transparente, 1 = preto total
        private long fadeStartTime = -1;   // quando o fade começou
        private String[] dialogue = new String[6];
        private int currentDialogIndex = 0;
        private long loadingStartTime = -1;
        private static final int LOADING_MIN_DURATION = 750; // ms, dentro do range 0.5-1s que você pediu
        private boolean levelLoaded = false;
        private static final int FADE_DURATION = 600; // 1 segundo em ms

        public int gameState = TITLE_STATE;

        // Thread do jogo (para rodar o loop em paralelo)
        private volatile boolean running;

        Thread GameThread;
        JButton startButton;
        JButton optionsButton;
        JButton easyButton;
        JButton normalButton;
        JButton hardButton;
        JButton backButton;
        JButton restartButton;
        JButton nextFaseButton;
        BufferedImage buttonTexture;
        BufferedImage backgroundImage;
        
        // Componentes principais do jogo
        public KeyHandler key = new KeyHandler();
        public TileManager tileManager = new TileManager(this);
        public SpawnManager spawnM = new SpawnManager(this);
        public CollisionChecker cChecker = new CollisionChecker(this);
        public Player player = new Player(this, key);
        public HUD HUD = new HUD(this);
        public AssetSetter aSetter = new AssetSetter(this);

        // Dificuldade do jogo, 0 = easy por padrão
        public static int dificulty = 0;

        // Array de objetos do mundo (inimigos, itens, etc)
        public superObject obj[] = new superObject[50]; //Limite de objetos

        // Posição da câmera (coordenada do mundo que aparece no canto superior-esquerdo)
        public int cameraX = 0;
        public int cameraY = 0;

        public GamePanel(){
            this.setPreferredSize(new Dimension(screenWidth, screenHeight));
            this.setBackground(Color.BLACK);
            this.setDoubleBuffered(true); // Evita piscar com buffer duplo
            this.setLayout(null);
            this.addKeyListener(key);     // Adiciona o detector de teclado
            this.setFocusable(true);      // Permite receber eventos de teclado
            this.requestFocusInWindow();  // Pede foco para capturar teclas
            
            // Tenta carregar a imagem de fundo do menu e a textura dos botões
            try {
                backgroundImage = ImageIO.read(getClass().getResourceAsStream("/res/menu.png"));
                buttonTexture = ImageIO.read(getClass().getResourceAsStream("/res/tile/wall2.png"));
            } catch (IOException e) {
                System.out.println("Erro ao carregar background ou textura de botões: " + e.getMessage());
            }
            
            // Cria e posiciona o botão de iniciar e de opções
            startButton = createTexturedButton("Começar o jogo", screenWidth/2 - 100, screenHeight/2 + 50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    startGameWithDialogue();
                }
            });
            optionsButton = createTexturedButton("Opções", screenWidth/2 - 100, screenHeight/2 + 100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showOptionButtons(true);
                }
            });

            //Botões de dificuldade 
            easyButton = createTexturedButton("Fácil", screenWidth/2 - 100, screenHeight/2 + 50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dificulty = 0;
                    Fear.setFearDificult(dificulty);
                    showOptionButtons(false);
                }
                });

                normalButton = createTexturedButton("Normal", screenWidth/2 - 100, screenHeight/2 + 100, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dificulty = 1;
                        Fear.setFearDificult(dificulty);
                        showOptionButtons(false);
                    }
                });

                hardButton = createTexturedButton("Difícil", screenWidth/2 - 100, screenHeight/2 + 150, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dificulty = 2;
                        Fear.setFearDificult(dificulty);
                        showOptionButtons(false);
                    }
                });

                backButton = createTexturedButton("Voltar", screenWidth/2 - 100, screenHeight/2 + 200, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showOptionButtons(false);
                    }
                });

                //Usados somente no final de fases
                /** Voltar a tela inicial */
                restartButton = createTexturedButton("Voltar a tela inicial", screenWidth/2 - 100, screenHeight/2 + 300, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        restartGame();
                }
                });
                /** Chama "nextLevel" */
                nextFaseButton = createTexturedButton("Ir para próxima fase", screenWidth/2 - 100, screenHeight/2 + 300, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nextLevel();
                    }        
                });
                
                this.add(startButton);
                this.add(optionsButton);
                this.add(easyButton);
                this.add(normalButton);
                this.add(hardButton);
                this.add(backButton);
                this.add(restartButton);
                this.add(nextFaseButton);

                // Configura as springs de diálogo
                setDialog();

                //garante que todos os botoes exceto iniciar e opções estejam invisíveis no começo
                showOptionButtons(false);
                
            }

            private void showOptionButtons(boolean visible) {
                startButton.setVisible(!visible);
                optionsButton.setVisible(!visible);
                easyButton.setVisible(visible);
                normalButton.setVisible(visible);
                hardButton.setVisible(visible);
                backButton.setVisible(visible);
                restartButton.setVisible(visible);
                nextFaseButton.setVisible(visible);
            }

            private JButton createTexturedButton(String text, int x, int y, ActionListener listener) {
                JButton button = new TextureButton(text, buttonTexture);
                button.setBounds(x, y, 200, 40);
                button.setFont(new Font("Arial", Font.BOLD, 16));
                button.setForeground(Color.WHITE);
                button.setBorderPainted(true);
                button.setFocusPainted(true);
                button.setContentAreaFilled(false);
                button.addActionListener(listener);
                return button;
            }

            private static class TextureButton extends JButton {
                private final BufferedImage texture;

                public TextureButton(String text, BufferedImage texture) {
                    super(text);
                    this.texture = texture;
                    setHorizontalTextPosition(CENTER);
                    setVerticalTextPosition(CENTER);
                }

                @Override
                protected void paintComponent(Graphics g) {
                    if (texture != null) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        TexturePaint paint = new TexturePaint(texture, new Rectangle(0, 0, texture.getWidth(), texture.getHeight()));
                        g2.setPaint(paint);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            }

            /**
             * Cria e inicia a thread do jogo.
             */
            public void startGameThread(){
                running = true;
                GameThread = null;
                GameThread = new Thread(this);
                GameThread.start();
            }

            public void stopGameThread() {
                running = false; // sinaliza para o loop parar
            try {
                if (GameThread != null) {
                    GameThread.join(); // espera a thread terminar
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

            /**
             * Inicializa todos os objetos do jogo.
             */
            public void setupGame() {
                if(FASE_STATE == 1) {
                        tileManager.map = "corredor.txt";
                    } else if (FASE_STATE == 2) {
                        tileManager.map = "mapSchool.txt";
                    } else if (FASE_STATE == 3) {
                        tileManager.map = "map2.txt";
                    }

                tileManager.LoadMap();
                player.setStartPosition(FASE_STATE);
                aSetter.setObject(FASE_STATE);
            }
            public void setDialog() {
                dialogue[0] = "Ola Stella!";
                dialogue[1] = "Por que voce esta aqui?";
                dialogue[2] = "Parece tão assustada. \nO que aconteceu?";
                dialogue[3] = "...";
                dialogue[4] = "Venha, vou te levar para um lugar calmo.";
                dialogue[5] = "Para conversarmos, Ok?";
            }

            /**
             * Inicia o jogo removendo a tela de título.
             */
            public void startGame() {
                // Aplica a dificuldade selecionada e inicia o jogo
                Fear.setFearDificult(dificulty);
                this.remove(startButton);
                this.remove(optionsButton);
                this.revalidate();
                this.repaint();
                
                // Muda para o estado de jogo
                gameState = PLAY_STATE;
                this.requestFocusInWindow(); // Garante foco no painel após iniciar
                
                // Inicia a thread se não estiver rodando
                if (GameThread == null || !GameThread.isAlive()) {
                    startGameThread();
                }
            }   
            
            public void startGameWithDialogue() {

                this.remove(startButton);
                this.remove(optionsButton);
                this.revalidate();
                this.repaint();

                gameState = DIALOG_STATE;
                currentDialogIndex = 0;

                if (GameThread == null || !GameThread.isAlive()) {
                    startGameThread();
                }
                this.requestFocusInWindow();
            }

            public void nextLevel() {
                nextFaseButton.setVisible(false);
                stopGameThread();
                FASE_STATE++;
                setupGame();
                player.autoWalk = false;
                player.isMoving = false;
                updateCam();
                fadeAlpha = 1f;
                fadeStartTime = -1;
                gameState = FADE_OUT_STATE;
                if (GameThread == null || !GameThread.isAlive()) {
                    startGameThread();
                }
                requestFocusInWindow();
            }
            private void loadingState() {
                long now = System.currentTimeMillis();
                if (loadingStartTime < 0) loadingStartTime = now;

                // Carrega a fase só uma vez, no primeiro frame deste estado
                if (!levelLoaded) {
                    FASE_STATE++;
                    setupGame();
                    player.autoWalk = false;
                    player.isMoving = false;
                    updateCam();
                    levelLoaded = true;
                }
            
                long elapsed = now - loadingStartTime;
                if (elapsed >= LOADING_MIN_DURATION) {
                    loadingStartTime = -1;
                    fadeAlpha = 1f;
                    fadeStartTime = -1;
                    gameState = FADE_OUT_STATE;
                }
            }           
            public void restartGame() {
                Fear.situation = 0;
                Fear.distIn = 0;
                FASE_STATE = 1;

                // Recria o jogador do zero
                player = new Player(this, key);

                // Recoloca os objetos no mundo
                for (int i = 0; i < obj.length; i++) {
                obj[i] = null;
                }
                setupGame();

                // Esconde o botão e volta para o título
                restartButton.setVisible(false);
                nextFaseButton.setVisible(false);
                gameState = TITLE_STATE;

                // Readiciona os botões do menu (foram removidos no startGame)
                if (startButton.getParent() == null) this.add(startButton);
                if (optionsButton.getParent() == null) this.add(optionsButton);
                showOptionButtons(false);

                this.revalidate();
                this.repaint();
                this.requestFocusInWindow();
            }


            public void updateCam() {
                // Ajusta a câmera para não sair dos limites do mapa.
                    // cameraX/Y = coordenada do mundo que estará no canto superior-esquerdo da tela
                    int maxCameraX = worldWidth - screenWidth + 72;
                    int maxCameraY = worldHeight - screenHeight + 72;
                    if (maxCameraX < 0) maxCameraX = 0;
                    if (maxCameraY < 0) maxCameraY = 0;
                    int cameraX = player.worldX - player.screenX;
                    int cameraY = player.worldY - player.screenY;
                    // Limita a câmera entre 0 e o máximo do mundo
                    cameraX = Math.max(0, Math.min(cameraX, maxCameraX));
                    cameraY = Math.max(0, Math.min(cameraY, maxCameraY));
                    // Calcula onde o jogador ficará na tela com a câmera limitada
                    player.screenX = player.worldX - cameraX;
                    player.screenY = player.worldY - cameraY;
                    // Guarda a posição da câmera no GamePanel para que o mundo use-a ao desenhar
                    this.cameraX = cameraX;
                    this.cameraY = cameraY;
            }
            public void updateCamToSafezone() {
                // Move a câmera para o centro da safezone
                int safeX = 25 * tileSz; // coordenada X da safezone
                int safeY = 25 * tileSz; // coordenada Y da safezone
                int cameraX = safeX - screenWidth / 2;
                int cameraY = safeY - screenHeight / 2;

                // Limita a câmera entre 0 e o máximo do mundo
                int maxCameraX = worldWidth - screenWidth + 72;
                int maxCameraY = worldHeight - screenHeight + 72;
                if (maxCameraX < 0) maxCameraX = 0;
                if (maxCameraY < 0) maxCameraY = 0;
                cameraX = Math.max(0, Math.min(cameraX, maxCameraX));
                cameraY = Math.max(0, Math.min(cameraY, maxCameraY));

                this.cameraX = cameraX;
                this.cameraY = cameraY;
            }
            public void teleportPlayer() {
                if (FASE_STATE != 1) return;
                int tilePortalSaida = 30;  // ← tile onde ela teleporta (ajuste aqui)
                int tilePortalEntrada = 2; // ← tile onde ela reaparece (ajuste aqui)

                int fim = tilePortalSaida * tileSz;
                int inicio = tilePortalEntrada * tileSz;

                if (player.worldX >= fim) {
                    int offset = player.worldX - fim; // quanto passou do portal
                    player.worldX = inicio + offset;  // mantém o excesso para não travar
                        
                    for (superObject o : obj) {
                        if (o != null && o.ally) {
                            o.WorldX = o.WorldX - fim + inicio;
                        }
                    }
                }
            }
            
            private void playState() {
                // Atualiza o estado do jogador (colisão, medo)
                player.update();
                player.autoWalk = false; // Reseta o autoWalk

                if (FASE_STATE == 1) {
                    /*int tilePortalSaida = 30;  // ← tile onde ela teleporta (ajuste aqui)
                    int tilePortalEntrada = 2; // ← tile onde ela reaparece (ajuste aqui)
                    int fim = tilePortalSaida * tileSz;
                    int inicio = tilePortalEntrada * tileSz;
                    if (player.worldX >= fim) {
                        int offset = player.worldX - fim; // quanto passou do portal
                        player.worldX = inicio + offset;  // mantém o excesso para não travar
                    
                        for (superObject o : obj) {
                            if (o != null && o.ally) {
                                o.WorldX = o.WorldX - fim + inicio;
                            }
                        }
                    }*/
                }

                if (FASE_STATE == 2) {
                    if (player.checkSafezone()) {
                        gameState = FADE_IN_STATE;
                        currentDialogIndex = 0;
                    }
                    for (int i = 0; i < obj.length; i++) {
                        if (obj[i] instanceof Enemy) {
                            ((Enemy) obj[i]).update(player);
                        }
                    }
                }

                if (FASE_STATE == 3) {
                    if (player.checkSafezone()) {
                        gameState = DIALOG_STATE;
                        currentDialogIndex = 0;
                    }
                    for (int i = 0; i < obj.length; i++) {
                        if (obj[i] instanceof Enemy) {
                            ((Enemy) obj[i]).update(player);
                        }
                    }
                }
                
                player.andar(false);
                updateCam(); 
                
            }
            private void dialogState() {

                player.update();
                player.andar(true);
                updateCam();
                teleportPlayer();

                if (FASE_STATE == 1) {
                    player.autoWalk = true;
                    player.andar(false);
                    for (superObject o : obj) {
                        if (o != null && o.ally) {
                            o.WorldX += 2;
                        }
                    }
                } else if (FASE_STATE == 2) {
                    player.autoWalkDirection = "left";
                    player.autoWalk = true;
                    player.andar(false);
                } else if (FASE_STATE == 3) {
                    player.andar(true);
                }

                if (key.enterPressed) {
                    key.enterPressed = false;
                    currentDialogIndex++;
                    if (currentDialogIndex >= dialogue.length) {
                        currentDialogIndex = 0;
                        if (FASE_STATE == 1) {  
                            gameState = FADE_IN_STATE;
                        } else if (FASE_STATE == 2) {
                            gameState = FADE_IN_STATE;
                        } else if (FASE_STATE == 3) {
                            gameState = VICTORY_STATE;
                        }
                    }
                }
            }
            private void fadeState() {
                long now = System.currentTimeMillis();
                if (fadeStartTime < 0) fadeStartTime = now;
                float progress = (float)(now - fadeStartTime) / FADE_DURATION;
                fadeAlpha = Math.min(progress, 1f);
                if (progress >= 1f) {
                        fadeAlpha = 0f;
                        fadeStartTime = -1;
                        gameState = GAME_OVER_STATE;
                }
            }
            private void fadeInState() {
                if (FASE_STATE == 2) {
                    player.autoWalkDirection = "left";
                    player.autoWalk = true;
                    player.andar(false);
                } else {
                player.autoWalk = false;
                player.isMoving = false;
                }

                long now = System.currentTimeMillis();
                if (fadeStartTime < 0) fadeStartTime = now;
                float progress = (float)(now - fadeStartTime) / FADE_DURATION;
                fadeAlpha = Math.min(progress, 1f);
                if (progress >= 1f) {
                    fadeAlpha = 1f;
                    fadeStartTime = -1;
                    loadingStartTime = -1;
                    levelLoaded = false;
                    gameState = LOADING_STATE;
                }
            }           
            private void fadeOutState() {
                long now = System.currentTimeMillis();
                if (fadeStartTime < 0) fadeStartTime = now;
                float progress = (float)(now - fadeStartTime) / FADE_DURATION;
                fadeAlpha = 1f - Math.min(progress, 1f);
                if (progress >= 1f) {
                    fadeAlpha = 0f;
                    fadeStartTime = -1;
                    gameState = PLAY_STATE;
                }
            }

            private void drawTitle(Graphics2D g2) {
                // Desenha a tela de título
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 48));
                    
                    // Desenha o subtítulo
                    g2.setFont(new Font("Arial", Font.PLAIN, 24));
                    String subtitle = "Aperte o botão para começar";
                    int subtitleWidth = g2.getFontMetrics().stringWidth(subtitle);
                    g2.drawString(subtitle, screenWidth/2 - subtitleWidth/2, screenHeight/2);
                    
                    // Desenha o background da tela de título
                    if (backgroundImage != null) {
                        g2.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight, this);
                    }
            }
            private void drawObjects(Graphics2D g2) {
                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] == null) continue;
                    if (obj[i].ally && FASE_STATE == 2) continue; // não desenha a professora na fase 2
                    obj[i].draw(g2, this);
                }
            }
            private void drawPlay(Graphics2D g2) {
                tileManager.Draw(g2);
                drawObjects(g2);
                player.Draw(g2);
                HUD.Draw(g2);
            }
            private void drawDialog(Graphics2D g2) {
                tileManager.Draw(g2);
                drawObjects(g2);
                player.Draw(g2);
                HUD.Draw(g2);

                // Caixa de diálogo
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRect(50, screenHeight - 150, screenWidth - 100, 100);

                // Texto de diálogo
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.PLAIN, 18));

                String dialogText = dialogue[currentDialogIndex];
                String[] lines = dialogText.split("\\n");
                int lineHeight = 22;
                int startY = screenHeight - 110;
                for (int i = 0; i < lines.length; i++) {
                    g2.drawString(lines[i], 70, startY + i * lineHeight);
                }
            }
            private void drawFade(Graphics2D g2) {
                tileManager.Draw(g2);
                drawObjects(g2);
                player.Draw(g2);
                HUD.Draw(g2);
                int alpha = (int)(fadeAlpha * 255);
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRect(0, 0, screenWidth, screenHeight);
            }
            private void drawFadeIn(Graphics2D g2) {
                tileManager.Draw(g2);
                drawObjects(g2);
                player.Draw(g2);
                HUD.Draw(g2);
                int alpha = (int)(fadeAlpha * 255);
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRect(0, 0, screenWidth, screenHeight);
            }
            private void drawFadeOut(Graphics2D g2) {
                tileManager.Draw(g2);
                drawObjects(g2);
                player.Draw(g2);
                HUD.Draw(g2);
                int alpha = (int)(fadeAlpha * 255);
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRect(0, 0, screenWidth, screenHeight);
            }
            private void drawTrans(Graphics2D g2) {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, screenWidth, screenHeight);
                g2.setColor(Color.YELLOW);
                g2.setFont(new Font("Arial", Font.BOLD, 56));
                switch (FASE_STATE) {
                    case 1:
                        
                        break;
                    case 2:
                        
                        break;
                    case 3:
                        
                        break;
                
                
                }
                String ending = "Você encontrou a Professora!";
                int ew = g2.getFontMetrics().stringWidth(ending);
                g2.drawString(ending, screenWidth/2 - ew/2, screenHeight/2 - 50);
                nextFaseButton.setVisible(true);
            }
            private void drawLoading(Graphics2D g2) {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, screenWidth, screenHeight);
                        
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.PLAIN, 28));
                String loadingText = "Carregando...";
                int lw = g2.getFontMetrics().stringWidth(loadingText);
                g2.drawString(loadingText, screenWidth/2 - lw/2, screenHeight/2);
            }
            private void drawGameOver(Graphics2D g2) {
                // Fundo escuro
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0, 0, screenWidth, screenHeight);

                    // Título "Game Over"
                    g2.setColor(Color.RED);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 72));
                    String gameOver = "GAME OVER";
                    int gw = g2.getFontMetrics().stringWidth(gameOver);
                    g2.drawString(gameOver, screenWidth/2 - gw/2, screenHeight/2 - 50);
                    
                    //Subtítulo
                    g2.setColor(Color.RED);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                    String subTextGameOver = "Barra de medo chegou ao maximo.";
                    int sw = g2.getFontMetrics().stringWidth(subTextGameOver);
                    g2.drawString(subTextGameOver, screenWidth/2 - sw/2, screenHeight/2 + 2);

                    restartButton.setVisible(true);
            }
            private void drawVictory(Graphics2D g2) {
                g2.setColor(new Color(0, 0, 0, 200));
                    g2.fillRect(0, 0, screenWidth, screenHeight);

                    g2.setColor(Color.GREEN);
                    g2.setFont(new Font("Arial", Font.BOLD, 56));
                    String ending = "Você reencontrou a Professora!";
                    int ew = g2.getFontMetrics().stringWidth(ending);
                    g2.drawString(ending, screenWidth/2 - ew/2, screenHeight/2 - 50);
                    restartButton.setVisible(true);
            }

            /**
             * Atualiza a lógica do jogo a cada frame.
             */
            public void update() {
                switch (gameState) {
                    case PLAY_STATE:      playState();     break;
                    case DIALOG_STATE:    dialogState();   break;
                    case FADE_IN_STATE:   fadeInState();   break;
                    case FADE_OUT_STATE:  fadeOutState();  break;
                    case LOADING_STATE:   loadingState();  break;
                    case FADE_STATE:      fadeState();     break; 
                }
            }

            /**
             * Desenha na tela.
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;

                switch (gameState) {
                    case TITLE_STATE:     drawTitle(g2);        break;
                    case PLAY_STATE:      drawPlay(g2);         break;
                    case DIALOG_STATE:    drawDialog(g2);       break;
                    case FADE_STATE:      drawFade(g2);         break;
                    case FADE_IN_STATE:   drawFadeIn(g2);       break;
                    case FADE_OUT_STATE:  drawFadeOut(g2);      break;
                    case LOADING_STATE:   drawLoading(g2);      break;
                    case TRANS_STATE:     drawTrans(g2);        break;
                    case GAME_OVER_STATE: drawGameOver(g2);     break;
                    case VICTORY_STATE:   drawVictory(g2);      break;
                }
            }
            
            /**
             * Loop principal do jogo (roda 60 vezes por segundo).
             */
            @Override
            public void run() {
                while (running) {
                    // Atualiza a lógica do jogo
                    update();
                    // Redesenha a tela
                    repaint();

                    try {
                        // Aguarda ~16ms para manter 60 FPS
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
