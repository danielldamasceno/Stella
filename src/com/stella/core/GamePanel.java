    package com.stella.core;
    import java.awt.Color;
    import com.stella.assets.SomJogo;
    import java.awt.Dimension;
    import java.awt.Font;
    import java.awt.FontMetrics;
    import java.awt.Graphics;
    import java.awt.Graphics2D;
    import java.awt.Rectangle;
    import java.awt.TexturePaint;
    import java.awt.image.BufferedImage;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.io.IOException;
    import java.io.InputStream;
    import javax.imageio.ImageIO;
    import javax.swing.JButton;
    import javax.swing.JPanel;   
    import java.util.ArrayList;
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
        SomJogo som = new SomJogo();
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
        public static final int CUTSCENE_1_STATE = 10;
        public static final int CUTSCENE_2_STATE = 11;
        public static final int HIDE_SEQUENCE_STATE = 12;
        public static final int FADE_IN_STATE = 7;
        public static final int FADE_OUT_STATE = 8;
        public static final int LOADING_STATE = 9;
        public static final int FINAL_IMAGE_STATE = 13;

        //Condições win/lose
        public static final int GAME_OVER_STATE = 2;  // Jogo acabou (derrota)
        public static final int VICTORY_STATE = 3;    // Jogo acabou (vitória)

            

        // Começa na fase 1 por padrão para abrir na tela de título
        public int FASE_STATE = 1;
        public int roomNumber;
        public String mapFile = "fase1/maptile.txt"; // Arquivo de mapa inicial
        private float fadeAlpha = 0f;      // 0 = transparente, 1 = preto total
        private long fadeStartTime = -1;   // quando o fade começou
        private String[] dialogue = new String[20];
        private int dialogueLength = 0;
        private int currentDialogIndex = 0;
        private int nextGameStateAfterDialog = PLAY_STATE;
        private String interactionPrompt = "";
        private BufferedImage cutsceneBackground;
        private BufferedImage cutsceneIntroBackground;
        private BufferedImage cutscenePsychologistBackground;
        private BufferedImage cutsceneCharacterBackground;
        private BufferedImage cutsceneFinalBackground;
        private String[] cutsceneLines = new String[0];
        private String[] cutsceneSpeakers = new String[0];
        private boolean phase1IntroActive = false;
        private boolean pendingHideSequence = false;
        private boolean hideSequenceActive = false;
        private boolean playerHidden = false;
        private boolean fromSafezonePsychDialog = false;
        private boolean schoolCompleted = false;
        private boolean homeCompleted = false;
        private long hideSequenceStartedAt = -1;
        private static final int HIDE_SEQUENCE_DURATION_MS = 5000;
        private int hideCountdownSeconds = 5;
        private long loadingStartTime = -1;
        private static final int LOADING_MIN_DURATION = 750; // ms, dentro do range 0.5-1s que você pediu
        private boolean levelLoaded = false;
        private static final int FADE_DURATION = 600; // 1 segundo em ms
        private long phase3StartTime = -1;
        private boolean invasorTriggered = false;
        private static final long INVASOR_TRIGGER_DELAY_MS = 5000; // 30s
        private Enemy invasorEnemy = null;
        private boolean pendingInvasorReturn = false;
        private boolean invasorAlreadyCame = false;

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
        JButton returnTitleButton;
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
        public superObject obj[] = new superObject[100]; //Limite de objetos

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

            som.tocarTrilha("audio/leticia-STELLA-abertura-do-jogo-tela-inicial-2026-07-13-06_53.wav");
            // Tenta carregar a imagem de fundo do menu e a textura dos botões
            try {
                backgroundImage = loadImage("/res/menu.png");
                buttonTexture = loadImage("/res/tile/escolassprite/wall2.png", "/res/tile/FFP/ffp_wall2.png", "/res/tile/wall2.png");
                cutsceneIntroBackground = loadImage("/res/levelsimage/cutscene/quartoinit.png");
                cutscenePsychologistBackground = loadImage("/res/levelsimage/psicologa/psifala.png");
                cutsceneCharacterBackground = loadImage("/res/levelsimage/psicologa/mcfala.png");
                cutsceneFinalBackground = loadImage("/res/levelsimage/psicologa/imagemFinal.png");
                cutsceneBackground = cutsceneIntroBackground;
            } catch (Exception e) {
                System.out.println("Erro ao carregar background ou textura de botões: " + e.getMessage());
            }
            
            // Cria e posiciona o botão de iniciar e de opções
            startButton = createTexturedButton("Começar o jogo", screenWidth/2 - 100, screenHeight/2 + 50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    startGame();
                }
            });
            /*optionsButton = createTexturedButton("Opções", screenWidth/2 - 100, screenHeight/2 + 100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showOptionButtons(true);
                }
            });*/

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
                restartButton = createTexturedButton("Tentar de Novo", screenWidth/2 - 100, screenHeight/2 + 300, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        restartGame();
                }
                });
                returnTitleButton = createTexturedButton("Voltar ao Título", screenWidth/2 - 100, screenHeight/2 + 200, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        resetGame();
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
                //this.add(optionsButton);
                this.add(easyButton);
                this.add(normalButton);
                this.add(hardButton);
                this.add(backButton);
                this.add(restartButton);
                this.add(returnTitleButton);
                this.add(nextFaseButton);

                // Configura as springs de diálogo
                setDialog();

                //garante que todos os botoes exceto iniciar e opções estejam invisíveis no começo
                showOptionButtons(false);
                
            }

            private void showOptionButtons(boolean visible) {
                startButton.setVisible(!visible);
                //optionsButton.setVisible(!visible);
                easyButton.setVisible(visible);
                normalButton.setVisible(visible);
                hardButton.setVisible(visible);
                backButton.setVisible(visible);
                restartButton.setVisible(visible);
                nextFaseButton.setVisible(visible);
            }

            private BufferedImage loadImage(String... paths) throws IOException {
                for (String path : paths) {
                    try (InputStream is = getClass().getResourceAsStream(path)) {
                        if (is != null) {
                            BufferedImage img = ImageIO.read(is);
                            if (img != null) {
                                return img;
                            }
                        }
                    }
                    System.err.println("Recurso não encontrado: " + path);
                }
                return null;
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
                tileManager.map = aSetter.getTileMapFile(FASE_STATE);
                switch (FASE_STATE) {
                    case 1 -> mapFile = "fase1/maptile.txt";
                    case 2 -> mapFile = "fase2/mapSchool.txt";
                    case 3 -> {
                        player.currentRoom = "mapSala";
                        mapFile = "fase3/mapSala.txt";
                    }
                    default -> mapFile = "fase1/maptile.txt";
                }
                tileManager.LoadMap(mapFile);
                player.setStartPosition(FASE_STATE);
                aSetter.setObject(FASE_STATE, null);
                        // Ao iniciar a fase da escola (FASE_STATE == 2) mostrar diálogo inicial
                                if (FASE_STATE == 2) {
                                    startDialogue(PLAY_STATE, "os meninos faziam bullying comigo, tive que tentar encontrar a professora para conversar com ela");
                                    // não retorna aqui: mantém a inicialização da fase e apenas inicia o diálogo
                                }
                                if (FASE_STATE == 3) {
                                    phase3StartTime = System.currentTimeMillis();
                                    invasorTriggered = false;
                                    invasorEnemy = null;
                                }                               
                som.tocarTrilha("audio/leticia-trilha-ambiente-2026-07-13-06_54.wav");

            }
            public void setDialog() {
                setDialog(new String[] {
                    "Ola Stella!",
                    "Por que voce esta aqui?",
                    "Parece tão assustada. \nO que aconteceu?",
                    "...",
                    "Venha, vou te levar para um lugar calmo.",
                    "Para conversarmos, Ok?"
                });
            }

            public void setDialog(String... lines) {
                for (int i = 0; i < dialogue.length; i++) {
                    dialogue[i] = "";
                }

                dialogueLength = Math.min(lines.length, dialogue.length);
                for (int i = 0; i < dialogueLength; i++) {
                    dialogue[i] = lines[i];
                }
                currentDialogIndex = 0;
            }

            private void startDialogue(int nextState, String... lines) {
                setDialog(lines);
                nextGameStateAfterDialog = nextState;
                currentDialogIndex = 0;
                player.autoWalk = false;
                player.autoWalkDirection = "right";
                player.isMoving = false;
                gameState = DIALOG_STATE;
                key.interactPressed = false;
            }

            private void startHideSequence() {
                hideSequenceActive = true;
                playerHidden = false;
                hideSequenceStartedAt = System.currentTimeMillis();
                hideCountdownSeconds = 5;
                gameState = HIDE_SEQUENCE_STATE;
                key.interactPressed = false;
                key.enterPressed = false;
                player.autoWalk = false;
                player.isMoving = false;
            }

            private boolean isTableTile(int tileCode) {
                if (FASE_STATE == 3) return tileCode == 23 || tileCode == 24;
                return tileCode == 10 || tileCode == 11;
            }

            private boolean canHideBehindTable() {
                int centerCol = (int) ((player.worldX + tileSz / 2.0) / tileSz);
                int centerRow = (int) ((player.worldY + tileSz / 2.0) / tileSz);

                int[][] checks = {
                    {centerCol, centerRow - 1},
                    {centerCol + 1, centerRow},
                    {centerCol, centerRow + 1},
                    {centerCol - 1, centerRow}
                };

                for (int[] check : checks) {
                    if (check[0] < 0 || check[0] >= maxWorldCol || check[1] < 0 || check[1] >= maxWorldRow) {
                        continue;
                    }
                    int tileCode = tileManager.mapTileNum[check[0]][check[1]];
                    if (isTableTile(tileCode)) {
                        return true;
                    }
                }
                return false;
            }

            /**
             * Inicia o jogo removendo a tela de título.
             */
            public void startGame() {
                // Aplica a dificuldade selecionada e inicia o jogo
                Fear.setFearDificult(dificulty);
                this.remove(startButton);
                //this.remove(optionsButton);
                this.revalidate();
                this.repaint();

                startCutscene1();
                this.requestFocusInWindow(); // Garante foco no painel após iniciar

                // Inicia a thread se não estiver rodando
                if (GameThread == null || !GameThread.isAlive()) {
                    startGameThread();
                }
            }

            private void startCutscene1() {
                cutsceneLines = new String[] {
                    "a primeira vez que vi algo assim, foi no meu quarto",
                    "*xingamentos*",
                    "meus pais estavam brigando bem feio, eu era criança e não entendia direito... até que ...",
                    "*som de tapa*"
                };
                cutsceneSpeakers = new String[0];
                cutsceneBackground = cutsceneIntroBackground;
                gameState = CUTSCENE_1_STATE;
                currentDialogIndex = 0;
                setDialog(cutsceneLines);
                nextGameStateAfterDialog = CUTSCENE_2_STATE;
                key.interactPressed = false;
                key.enterPressed = false;
                player.autoWalk = false;
                player.isMoving = false;
                player.direction = "bottom";
                player.worldX = screenWidth / 2 - tileSz / 2;
                player.worldY = screenHeight / 2 - tileSz / 2;
                player.screenX = screenWidth / 2 - tileSz / 2;
                player.screenY = screenHeight / 2 - tileSz / 2;
                cameraX = 0;
                cameraY = 0;
            }

            private void startCutscene2() {
                cutsceneLines = new String[] {
                    "Entendo. Pelo que você contou, isso pode indicar uma situação de violência física dentro de casa.",
                    "Eu ficava no meu quarto esperando a briga acabar.",
                    "Ninguém deveria passar por isso. Você não tem culpa pelo que aconteceu.",
                    "Em situações assim, o mais seguro é procurar um adulto de confiança, como um familiar, professor, orientador ou outro responsável que possa ajudar.\"",
                    "Também existem serviços de proteção para crianças e famílias quando há violência.",
                    "Agora, se você se sentir confortável, pode continuar contando a história. O que aconteceu depois?"
                };
                cutsceneSpeakers = new String[] {
                    "psicologa",
                    "Stella",
                    "psicologa",
                    "psicologa",
                    "psicologa",
                    "psicologa"
                };
                cutsceneBackground = cutsceneIntroBackground;
                phase1IntroActive = false;
                FASE_STATE = 1;
                setupGame();
                gameState = CUTSCENE_2_STATE;
                currentDialogIndex = 0;
                setDialog(cutsceneLines);
                nextGameStateAfterDialog = PLAY_STATE;
                key.interactPressed = false;
                key.enterPressed = false;
                player.autoWalk = false;
                player.isMoving = false;
                player.direction = "bottom";
                player.worldX = screenWidth / 2 - tileSz / 2;
                player.worldY = screenHeight / 2 - tileSz / 2;
                player.screenX = screenWidth / 2 - tileSz / 2;
                player.screenY = screenHeight / 2 - tileSz / 2;
                cameraX = 0;
                cameraY = 0;
            }

            private void startPsychologistConversation() {
                cutsceneLines = new String[] {
                    "O que você viveu é uma situação de violência contra a criança. Pessoas que observam ou tentam se aproximar de crianças de forma suspeita representam um risco e nunca devem ser ignoradas.",
                    "Nesses momentos, o mais importante é não se aproximar, procurar um adulto de confiança e contar imediatamente o que aconteceu. Mesmo que você não tenha certeza, é sempre melhor pedir ajuda.",
                    "Você fez a escolha certa ao não entrar naquela sala.",
                    "Isso aconteceu outras vezes? Você consegue se lembrar de outra situação que tenha feito você se sentir em perigo?",
                    "Foi na escola, os alunos me perseguiam por eu ser menina... tive que ir atras do meu estojo que eles econderam."
                };
                cutsceneSpeakers = new String[] {"psicologa", "psicologa", "psicologa", "psicologa", "Stella"};
                cutsceneBackground = cutscenePsychologistBackground;
                currentDialogIndex = 0;
                setDialog(cutsceneLines);
                fromSafezonePsychDialog = true;
                // Só vai para a vitória se já tiver concluído a fase da escola
                if (homeCompleted) {
                    nextGameStateAfterDialog = VICTORY_STATE;
                } else {
                    nextGameStateAfterDialog = PLAY_STATE;
                }
                key.interactPressed = false;
                key.enterPressed = false;
                player.autoWalk = false;
                player.isMoving = false;
                gameState = CUTSCENE_2_STATE;
            }
            
            public void startGameWithDialogue() {
                startGame();
            }

            public void nextLevel() {
                nextFaseButton.setVisible(false);
                stopGameThread();
                // Se estamos saindo da fase 2, marcamos a escola como concluída
                if (FASE_STATE == 2) schoolCompleted = true;
                if (FASE_STATE == 3) homeCompleted = true;
                
                FASE_STATE++;
                setupGame();
                player.autoWalk = false;
                player.isMoving = false;
                updateCam();
                fadeAlpha = 1f;
                fadeStartTime = -1;
                // Se o setup disparou um diálogo, não sobrescreve o estado de diálogo
                if (gameState != DIALOG_STATE) {
                    gameState = FADE_OUT_STATE;
                }
                if (GameThread == null || !GameThread.isAlive()) {
                    startGameThread();
                }
                requestFocusInWindow();
            }
            
            private void enterRoom() {
                player.lastWorldX = player.worldX;
                player.lastWorldY = player.worldY;
                mapFile = "fase2/randomRoom" + roomNumber + ".txt";

                tileManager.LoadMap(mapFile);
                if (roomNumber == 2) mapFile = "spawnrandomRoom" + roomNumber + ".txt";
                aSetter.setObject(FASE_STATE, mapFile);

                updateCam();
            }

            private void backFromRoom() {
                player.worldX = player.lastWorldX;
                player.worldY = player.lastWorldY;
                player.currentRoom = null;
                
                tileManager.LoadMap("fase2/mapSchool.txt");
                aSetter.setObject(FASE_STATE, null);
                mapFile = null;

                updateCam();
            }

            private void changeHomeRoom(String targetRoom, int spawnCol, int spawnRow) {
                player.currentRoom = targetRoom;
                mapFile = switch (targetRoom) {
                    case "mapSala" -> "fase3/mapSala.txt";
                    case "mapQuarto" -> "fase3/mapQuarto.txt";
                    case "mapCozinha" -> "fase3/mapCozinha.txt";
                    default -> "fase3/mapSala.txt";
                };
            
                tileManager.LoadMap(mapFile);
                aSetter.setObject(FASE_STATE, null);
            
                player.worldX = spawnCol * tileSz;
                player.worldY = spawnRow * tileSz;
            
                updateCam();
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
                    // Preserva diálogo caso tenha sido iniciado durante o carregamento
                    if (gameState != DIALOG_STATE) {
                        gameState = FADE_OUT_STATE;
                    }
                }
            }           
            public void restartGame() {
                Fear.situation = 0;
                Fear.distIn = 0;
                

                // Recria o jogador do zero
                player = new Player(this, key);

                // Recoloca os objetos no mundo
                for (int i = 0; i < obj.length; i++) {
                obj[i] = null;
                }
                setupGame();

                // Esconde o botão e volta para o título
                returnTitleButton.setVisible(false);
                restartButton.setVisible(false);
                nextFaseButton.setVisible(false);
                gameState = PLAY_STATE;

                // Readiciona os botões do menu (foram removidos no startGame)
                // if (startButton.getParent() == null) this.add(startButton);
                //if (optionsButton.getParent() == null) this.add(optionsButton);
                // showOptionButtons(false);

                this.revalidate();
                this.repaint();
                this.requestFocusInWindow();
            }
            public void resetGame() {
                Fear.situation = 0;
                Fear.distIn = 0;
                FASE_STATE=1;

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
                //if (optionsButton.getParent() == null) this.add(optionsButton);
                // showOptionButtons(false);

                this.revalidate();
                this.repaint();
                this.requestFocusInWindow();
            }

            public void updateCam() {
                // Ajusta a câmera para centralizar o jogador sempre que possível.
                int centerScreenX = screenWidth / 2 - tileSz / 2;
                int centerScreenY = screenHeight / 2 - tileSz / 2;

                int maxCameraX = worldWidth - screenWidth;
                int maxCameraY = worldHeight - screenHeight;
                if (maxCameraX < 0) maxCameraX = 0;
                if (maxCameraY < 0) maxCameraY = 0;

                int cameraX = player.worldX - centerScreenX;
                int cameraY = player.worldY - centerScreenY;

                cameraX = Math.max(0, Math.min(cameraX, maxCameraX));
                cameraY = Math.max(0, Math.min(cameraY, maxCameraY));

                player.screenX = player.worldX - cameraX;
                player.screenY = player.worldY - cameraY;

                this.cameraX = cameraX;
                this.cameraY = cameraY;
            }
            public void updateCamToSafezone() {
                // Move a câmera para o centro da safezone, respeitando os limites do mundo.
                int safeX = 25 * tileSz; // coordenada X da safezone
                int safeY = 25 * tileSz; // coordenada Y da safezone
                int centerScreenX = screenWidth / 2 - tileSz / 2;
                int centerScreenY = screenHeight / 2 - tileSz / 2;
                int cameraX = safeX - centerScreenX;
                int cameraY = safeY - centerScreenY;

                int maxCameraX = worldWidth - screenWidth;
                int maxCameraY = worldHeight - screenHeight;
                if (maxCameraX < 0) maxCameraX = 0;
                if (maxCameraY < 0) maxCameraY = 0;
                cameraX = Math.max(0, Math.min(cameraX, maxCameraX));
                cameraY = Math.max(0, Math.min(cameraY, maxCameraY));

                this.cameraX = cameraX;
                this.cameraY = cameraY;
            }
            public void teleportPlayer() {
                // Mantido sem efeito para evitar qualquer reposicionamento inesperado do jogador.
            }
            
            private void playState() {
                // Atualiza o estado do jogador (colisão, medo)
                player.update();
                player.autoWalk = false; // Reseta o autoWalk

                if (shouldTriggerGameOver()) {
                    fadeStartTime = -1;
                    gameState = FADE_STATE;
                    return;
                }

                updateInteractionPrompts();

                if (key.interactPressed) {
                    key.interactPressed = false;
                    for (int i = 0; i < obj.length; i++) {
                        if (obj[i] == null) continue;
                        if (obj[i] instanceof com.stella.entities.InteractionBlock block) {
                            if (block.isNear(player) && !block.used) {
                                boolean isRoomPortal = block.promptText != null && (
                                    block.promptText.contains("Sala de Ciências") ||
                                    block.promptText.contains("Sala de Matemática") ||
                                    block.promptText.contains("Sala de Português") ||
                                    block.promptText.contains("Laboratório de Informática") ||
                                    block.promptText.contains("Laboratório de Física") ||
                                    block.promptText.contains("Sair da sala (aperte E)") ||
                                    block.promptText.contains("Sala (aperte E)") ||
                                    block.promptText.contains("Sair da casa (aperte E)") ||
                                    block.promptText.contains("Voltar para a sala (aperte E)")
                                );

                                if (!isRoomPortal) {
                                    block.used = true;
                                }

                                switch(block.promptText) {
                                    case "Acessar sala (aperte E)" -> {
                                        if (FASE_STATE == 1) {
                                            startPsychologistConversation();
                                        }
                                    }
                                    case "Sala de Ciências (aperte E)" -> { //sala aleatoria 1
                                        if (FASE_STATE == 2) {
                                            roomNumber = 1;
                                            enterRoom();

                                            player.currentRoom = "room1";
                                            player.worldX = 17 * tileSz;
                                            player.worldY = 15 * tileSz;
                                        }
                                    }
                                    case "Sala de Matemática (aperte E)" -> { //sala aleatoria 2
                                        if (FASE_STATE == 2) {
                                            roomNumber = 2;
                                            enterRoom();

                                            player.currentRoom = "room2";
                                            player.worldX = 17 * tileSz;
                                            player.worldY = 16 * tileSz;
                                        }
                                    }
                                    case "Sala de Português (aperte E)" -> { //sala aleatoria 3
                                        if (FASE_STATE == 2) {
                                            roomNumber = 3;
                                            enterRoom();

                                            player.currentRoom = "room3";
                                            player.worldX = 17 * tileSz;
                                            player.worldY = 16 * tileSz;
                                        }
                                    }
                                    case "Laboratório de Informática (aperte E)" -> { //sala aleatoria 4
                                        if (FASE_STATE == 2) {
                                            roomNumber = 4;
                                            enterRoom();

                                            player.currentRoom = "room4";
                                            player.worldX = 8 * tileSz;
                                            player.worldY = 16 * tileSz;
                                        }
                                    }
                                    case "Laboratório de Física (aperte E)" -> { //sala aleatoria 5
                                        if (FASE_STATE == 2) {
                                            roomNumber = 5;
                                            enterRoom();

                                            player.currentRoom = "room5";
                                            player.worldX = 7 * tileSz;
                                            player.worldY = 16 * tileSz;
                                        }
                                    }
                                    case "Pegar o Estojo (aperte E)" -> {
                                        startDialogue(PLAY_STATE, block.getDialogueLines());
                                        obj[i] = null; // Remove o item do jogo
                                        player.inventario[0] = "estojo";
                                    }
                                    case "Sala (aperte E)" -> {
                                        startDialogue(PLAY_STATE, block.getDialogueLines());
                                    }
                                    case "Sair da sala (aperte E)" -> {
                                        backFromRoom();
                                    }
                                    //fase 3:
                                    case "Sair da casa (aperte E)" -> {
                                        if (!hasAllHomeItems()) {
                                            startDialogue(PLAY_STATE, "Estou esquecendo algo.");
                                        } else {
                                            triggerFase3Ending();
                                        }
                                    }
                                    case "Voltar para a sala (aperte E)" -> {
                                        changeHomeRoom("mapSala", 14, 4);
                                    }
                                    case "Entrar no quarto (aperte E)" -> {
                                        player.lastWorldX = player.worldX;
                                        player.lastWorldY= player.worldY;
                                        changeHomeRoom("mapQuarto", 5, 8);
                                    }
                                    case "Entrar na cozinha (aperte E)" -> {
                                        player.lastWorldX = player.worldX;
                                        player.lastWorldY= player.worldY;
                                        changeHomeRoom("mapCozinha",16, 11);
                                    }
                                    case "Pegar a carteira (aperte E)" -> {
                                        startDialogue(PLAY_STATE, block.getDialogueLines());
                                        obj[i] = null;
                                        player.inventario[0] = "wallet";
                                        checkLastItem();
                                    }
                                    case "Pegar as chaves (aperte E)" -> {
                                        startDialogue(PLAY_STATE, block.getDialogueLines());
                                        obj[i] = null;
                                        player.inventario[2] = "key";
                                        checkLastItem();
                                    }
                                    case "Pegar o celular (aperte E)" -> {
                                        startDialogue(PLAY_STATE, block.getDialogueLines());
                                        obj[i] = null;
                                        player.inventario[1] = "phone";
                                        checkLastItem();
                                    }
                        
                                    default -> {
                                        pendingHideSequence = true;
                                        startDialogue(PLAY_STATE, block.getDialogueLines());
                                    }
                                }
                            break;
                            }
                        }
                    }
                }

                if (FASE_STATE == 1) {
                    /*int tilePortalSaida = 30;  // tile onde ela teleporta (ajuste aqui)
                            // Ao encostar na professora, mostrar a tela da psicóloga e iniciar diálogo
                            String[] psychLines = new String[] { "Ainda bem que você procurou uma autoridade para te ajudar, esse tipo de violência deve ser evitada!" };
                            String[] psychSpeakers = new String[] { "psicologa" };
                            cutsceneLines = psychLines;
                            cutsceneSpeakers = psychSpeakers;
                            cutsceneBackground = cutscenePsychologistBackground;
                            setDialog(cutsceneLines);
                            currentDialogIndex = 0;
                            nextGameStateAfterDialog = VICTORY_STATE;
                            key.interactPressed = false;
                            key.enterPressed = false;
                            player.autoWalk = false;
                            player.isMoving = false;
                            fromSafezonePsychDialog = false; // evitar comportamento antigo
                            gameState = CUTSCENE_2_STATE;
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
                        // Ao encostar na professora: mostrar a cena da psicóloga com uma linha curta
                        schoolCompleted = true;
                        player.inventario[0] = null; //remove o estojo
                        cutsceneLines = new String[] { 
                            "Ainda bem que você conseguiu escapar!",
                            "O que você viveu na escola também foi uma forma de violência. O bullying acontece quando uma pessoa é humilhada, excluída, ameaçada ou perseguida repetidamente, e isso nunca é culpa da vítima.",
                            "Esconder seus pertences, fazer brincadeiras para constranger você ou tratá-la de forma diferente por ser menina são atitudes que machucam e podem causar sofrimento por muito tempo.",
                            "Nessas situações, procurar um adulto de confiança, como um professor, coordenador ou um familiar, é a atitude mais segura. Pedir ajuda não é sinal de fraqueza, e sim uma forma de se proteger.",
                            "Você fez a escolha certa ao procurar sua professora. Ninguém deveria enfrentar esse tipo de situação sozinho.",
                            "Você consegue se lembrar de outra situação em que tenha sentido medo ou precisado lutar para se proteger?",
                            "Sim... Na minha antiga casa, ela foi invadida durante a noite. Eu precisei pegar meu celular, minha carteira e a chave para fugir dali... mas, antes de conseguir escapar, tive que me esconder do invasor."
                        };

                        cutsceneSpeakers = new String[] {
                            "psicologa",
                            "psicologa",
                            "psicologa",
                            "psicologa",
                            "psicologa",
                            "psicologa",
                            "Stella"
                        };

                        cutsceneBackground = cutscenePsychologistBackground;
                        setDialog(cutsceneLines);
                        currentDialogIndex = 0;
                        fromSafezonePsychDialog = true;
                        // Se já concluiu a fase da casa, segue para vitória; caso contrário volta ao jogo
                        nextGameStateAfterDialog = homeCompleted ? VICTORY_STATE : PLAY_STATE;
                        key.interactPressed = false;
                        key.enterPressed = false;
                        player.autoWalk = false;
                        player.isMoving = false;
                        gameState = CUTSCENE_2_STATE;
                    }
                    for (int i = 0; i < obj.length; i++) {
                        if (obj[i] instanceof Enemy) {
                            ((Enemy) obj[i]).update(player);
                        }
                    }
                }

                if (FASE_STATE == 3) {
                    if (!invasorTriggered && phase3StartTime > 0
                        && System.currentTimeMillis() - phase3StartTime >= INVASOR_TRIGGER_DELAY_MS) {
                        invasorTriggered = true;
                        invasorEnemy = aSetter.spawnInvasor();
                        pendingHideSequence = true;
                        startDialogue(PLAY_STATE, "Alguém entrou em casa... rápido, se esconda!");
                    }

                    if (player.checkSafezone()) {
                        triggerFase3Ending();
                    }
                    for (int i = 0; i < obj.length; i++) {
                        if (obj[i] instanceof Enemy) {
                            ((Enemy) obj[i]).update(player);
                        }
                    }
                }

                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] == null) continue;
                    if (obj[i] instanceof com.stella.entities.StepDialogueObject step) {
                        if (step.isPlayerOnTop(player) && !step.triggered) {
                            step.triggered = true;
                            startDialogue(PLAY_STATE, step.dialogueLines);
                            break;
                        }
                    }
                }
                
                player.andar(false);
                updateCam(); 
                
            }
            private boolean shouldTriggerGameOver() {
                return Fear.situation >= 1.0 && gameState != FADE_STATE && gameState != GAME_OVER_STATE && gameState != VICTORY_STATE;
            }

            public boolean isInvasorTriggered() {
                return invasorTriggered;
            }

            private void triggerFase3Ending() {
                homeCompleted = true;
                cutsceneLines = new String[] {
                    "O que você viveu naquela noite foi uma situação de violência que colocou sua vida em risco. Ninguém deveria precisar fugir da própria casa ou se esconder para sobreviver.",
                    "Em situações como essa, buscar ajuda de pessoas de confiança e das autoridades é fundamental. A violência nunca deve ser enfrentada sozinha, e pedir ajuda é um passo importante para recuperar a própria segurança.",
                    "Obrigada por me ouvir... Reviver tudo isso foi muito difícil, mas hoje eu entendo que nada daquilo foi culpa minha. Eu só espero que ninguém mais precise passar pelo medo e pelo sofrimento que eu vivi."
                };
                cutsceneSpeakers = new String[] { "psicologa", "psicologa", "Stella" };
                cutsceneBackground = cutsceneFinalBackground;
                setDialog(cutsceneLines);
                currentDialogIndex = 0;
                fromSafezonePsychDialog = true;
                nextGameStateAfterDialog = FINAL_IMAGE_STATE;
                key.interactPressed = false;
                key.enterPressed = false;
                player.autoWalk = false;
                player.isMoving = false;
                gameState = CUTSCENE_2_STATE;
            }

            private void dialogState() {

                player.update();
                if (shouldTriggerGameOver()) {
                    fadeStartTime = -1;
                    gameState = FADE_STATE;
                    return;
                }
                player.andar(true);
                updateCam();

                if (FASE_STATE == 1) {
                    player.autoWalk = false;
                    player.isMoving = false;
                    player.andar(false);
                } else if (FASE_STATE == 2) {
                    player.autoWalkDirection = "left";
                    player.autoWalk = false;
                    player.andar(false);
                } else if (FASE_STATE == 3) {
                    player.andar(false);
                }

                if (key.enterPressed) {
                    key.enterPressed = false;
                    currentDialogIndex++;
                    if (currentDialogIndex >= dialogueLength) {
                        currentDialogIndex = 0;
                        if (pendingInvasorReturn) {
                            pendingInvasorReturn = false;
                            pendingHideSequence = true;
                            startDialogue(PLAY_STATE, "Peguei tudo... mas ele voltou! Rápido, se esconda!");
                            return;
                        }
                        if (pendingHideSequence) {
                            pendingHideSequence = false;
                            startHideSequence();
                            return;
                        }
                        if (gameState == CUTSCENE_1_STATE) {
                            gameState = CUTSCENE_2_STATE;
                        } else if (FASE_STATE == 1) {
                            gameState = PLAY_STATE;
                        } else if (FASE_STATE == 2 || FASE_STATE == 3) {
                            gameState = nextGameStateAfterDialog;
                        } else {
                            gameState = nextGameStateAfterDialog;
                        }
                    }
                }
            }

            private void cutscene1State() {
                player.update();
                player.andar(true);
                player.isMoving = false;
                if (key.enterPressed) {
                    key.enterPressed = false;
                    currentDialogIndex++;
                    if (currentDialogIndex >= dialogueLength) {
                        currentDialogIndex = 0;
                        startCutscene2();
                    }
                }
            }

            private void cutscene2State() {
                player.update();
                player.andar(true);
                player.isMoving = false;
                if (key.enterPressed) {
                    key.enterPressed = false;
                    currentDialogIndex++;
                    if (currentDialogIndex >= dialogueLength) {
                        currentDialogIndex = 0;
                        // Se este cutscene tinha como próximo estado a vitória, vai direto para VICTORY
                        if (nextGameStateAfterDialog == VICTORY_STATE) {
                            nextGameStateAfterDialog = PLAY_STATE;
                            gameState = VICTORY_STATE;
                            return;
                        }

                        if (nextGameStateAfterDialog == FINAL_IMAGE_STATE) {
                            nextGameStateAfterDialog = PLAY_STATE;
                            returnTitleButton.setVisible(true);
                            nextFaseButton.setVisible(false);
                            gameState = FINAL_IMAGE_STATE;
                            return;
                        }

                        if (fromSafezonePsychDialog) {
                            fromSafezonePsychDialog = false;
                            FASE_STATE++;
                            setupGame();
                            player.autoWalk = false;
                            player.isMoving = false;
                            updateCam();
                            gameState = PLAY_STATE;
                        } else if (phase1IntroActive) {
                            phase1IntroActive = false;
                            gameState = PLAY_STATE;
                        } else {
                            FASE_STATE = 1;
                            setupGame();
                            startPhase1Intro();
                        }
                    }
                }
            }

            private void startPhase1Intro() {
                cutsceneLines = new String[] {
                    "O primeiro caso foi em uma pizzaria que fui para um aniversário, eu precisava achar a sala do aniversariante, então resolvi explorar"
                };
                cutsceneSpeakers = new String[] { "Stella" };
                cutsceneBackground = cutsceneCharacterBackground;
                phase1IntroActive = true;
                currentDialogIndex = 0;
                setDialog(cutsceneLines);
                nextGameStateAfterDialog = PLAY_STATE;
                key.interactPressed = false;
                key.enterPressed = false;
                gameState = CUTSCENE_2_STATE;
            }

            private void hideSequenceState() {
                player.update();
                player.autoWalk = false;
                player.isMoving = false;
                player.andar(false);
                updateCam();

                if (!hideSequenceActive) {
                    gameState = PLAY_STATE;
                    return;
                }

                long elapsed = System.currentTimeMillis() - hideSequenceStartedAt;
                hideCountdownSeconds = Math.max(0, (int) Math.ceil((HIDE_SEQUENCE_DURATION_MS - elapsed) / 1000.0));

                double remainingRatio = Math.max(0.0, 1.0 - (elapsed / (double) HIDE_SEQUENCE_DURATION_MS));
                double fearIncreasePerFrame = 0.0015 + (0.0035 * (1.0 - remainingRatio));
                Fear.situation = Math.min(1.0, Fear.situation + fearIncreasePerFrame);

                if (elapsed >= HIDE_SEQUENCE_DURATION_MS) {
                    hideSequenceActive = false;
                    gameState = FADE_STATE;
                    return;
                }

                if (key.interactPressed || key.enterPressed) {
                    key.interactPressed = false;
                    key.enterPressed = false;
                    if (canHideBehindTable()) {
                        playerHidden = true;
                        hideSequenceActive = false;
                        Fear.situation = 0;
                        player.autoWalk = false;
                        player.isMoving = false;

                        if (invasorEnemy != null) {
                            for (int i = 0; i < obj.length; i++) {
                                if (obj[i] == invasorEnemy) { obj[i] = null; break; }
                            }
                            invasorEnemy = null;
                        }

                        if (FASE_STATE == 3 && invasorAlreadyCame == false){
                            invasorAlreadyCame = true;
                            startDialogue(PLAY_STATE, "Preciso sair daqui! Tenho que pegar as minhas chaves, meu celular e minha carteira");
                        }
                        return;
                    }
                }
            }

            private void checkLastItem() {
                if (hasAllHomeItems()) {
                    pendingInvasorReturn = true;
                }
            }

            private boolean hasAllHomeItems() {
                return "wallet".equals(player.inventario[0]) &&
                       "phone".equals(player.inventario[1]) &&
                       "key".equals(player.inventario[2]);
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
                    returnTitleButton.setVisible(false);
                    // Desenha o background da tela de título
                    if (backgroundImage != null) {
                        g2.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight, this);
                    }
            }
            private void drawObjects(Graphics2D g2) {
                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] == null) continue;
                    // Na fase 2, normalmente ocultamos aliados, exceto a professora (nome "Teacher").
                    if (obj[i].ally && FASE_STATE == 2 && !"Teacher".equals(obj[i].name)) continue;
                    obj[i].draw(g2, this);
                }
            }
            private void drawPlay(Graphics2D g2) {
                tileManager.Draw(g2);
                drawObjects(g2);
                player.Draw(g2);
                HUD.Draw(g2);

                if (!interactionPrompt.isEmpty()) {
                    g2.setColor(new Color(255, 255, 255, 220));
                    g2.fillRoundRect(screenWidth / 2 - 180, screenHeight - 120, 360, 40, 20, 20);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Arial", Font.BOLD, 18));
                    int promptWidth = g2.getFontMetrics().stringWidth(interactionPrompt);
                    g2.drawString(interactionPrompt, screenWidth / 2 - promptWidth / 2, screenHeight - 94);
                }
            }
            private void updateInteractionPrompts() {
                interactionPrompt = "";

                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] == null) continue;
                    if (obj[i] instanceof com.stella.entities.InteractionBlock block) {
                        if (block.isNear(player)) {
                            interactionPrompt = block.promptText;
                            break;
                        }
                    }
                }
            }

            private void drawDialog(Graphics2D g2) {
                tileManager.Draw(g2);
                drawObjects(g2);
                player.Draw(g2);
                HUD.Draw(g2);

                // Caixa de diálogo
                g2.setColor(new Color(0, 0, 0, 220));
                g2.fillRoundRect(50, screenHeight - 160, screenWidth - 100, 120, 20, 20);

                // Nome do falante
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.drawString("Stella:", 70, screenHeight - 128);

                // Texto de diálogo
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.PLAIN, 18));

                String dialogText = dialogue[currentDialogIndex];
                int maxTextWidth = screenWidth - 140; // dialog box width minus paddings
                String[] lines = wrapText(dialogText, g2, maxTextWidth);
                int lineHeight = 22;
                int startY = screenHeight - 100;
                for (int i = 0; i < lines.length; i++) {
                    g2.drawString(lines[i], 70, startY + i * lineHeight);
                }

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                String continuePrompt = "Pressione Enter para continuar";
                int promptWidth = g2.getFontMetrics().stringWidth(continuePrompt);
                g2.drawString(continuePrompt, screenWidth - 70 - promptWidth, screenHeight - 60);
            }
            private void drawHideSequence(Graphics2D g2) {
                tileManager.Draw(g2);
                drawObjects(g2);
                if (!playerHidden) {
                    player.Draw(g2);
                }
                HUD.Draw(g2);

                g2.setColor(new Color(0, 0, 0, 220));
                g2.fillRoundRect(60, 60, screenWidth - 120, 170, 20, 20);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 28));
                g2.drawString("Eles te viram...", 90, 105);

                g2.setFont(new Font("Arial", Font.PLAIN, 22));
                g2.drawString("Tempo restante: " + hideCountdownSeconds + "s", 90, 145);

                if (canHideBehindTable()) {
                    g2.setColor(Color.YELLOW);
                    g2.drawString("Aperte E para se esconder atrás da mesa", 90, 180);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.drawString("Encontre uma mesa para se esconder", 90, 180);
                }
            }

            private void drawCutscene(Graphics2D g2) {
                BufferedImage sceneImage = cutsceneBackground;
                if (currentDialogIndex < cutsceneSpeakers.length) {
                    String speaker = cutsceneSpeakers[currentDialogIndex];
                    if ("personagem".equalsIgnoreCase(speaker)) {
                        sceneImage = cutsceneCharacterBackground;
                    } else if ("psicologa".equalsIgnoreCase(speaker)) {
                        sceneImage = cutscenePsychologistBackground;
                    }
                }

                if (sceneImage != null) {
                    g2.drawImage(sceneImage, 0, 0, screenWidth, screenHeight, this);
                } else {
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0, 0, screenWidth, screenHeight);
                }

                g2.setColor(new Color(0, 0, 0, 220));
                g2.fillRoundRect(50, screenHeight - 160, screenWidth - 100, 120, 20, 20);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                String speakerLabel = "Stella";
                if (currentDialogIndex < cutsceneSpeakers.length) {
                    String speaker = cutsceneSpeakers[currentDialogIndex];
                    if ("psicologa".equalsIgnoreCase(speaker)) {
                        speakerLabel = "Psicóloga";
                    } else if ("personagem".equalsIgnoreCase(speaker)) {
                        speakerLabel = "Stella";
                    }
                }
                g2.drawString(speakerLabel + ":", 70, screenHeight - 128);

                g2.setFont(new Font("Arial", Font.PLAIN, 18));
                String dialogText = dialogue[currentDialogIndex];
                int maxTextWidth = screenWidth - 140; // dialog box width minus paddings
                String[] lines = wrapText(dialogText, g2, maxTextWidth);
                int lineHeight = 22;
                int startY = screenHeight - 100;
                for (int i = 0; i < lines.length; i++) {
                    g2.drawString(lines[i], 70, startY + i * lineHeight);
                }

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                String continuePrompt = "Pressione Enter para continuar";
                int promptWidth = g2.getFontMetrics().stringWidth(continuePrompt);
                g2.drawString(continuePrompt, screenWidth - 70 - promptWidth, screenHeight - 60);
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
            
            private String[] wrapText(String text, Graphics2D g2, int maxWidth) {
                java.util.List<String> lines = new ArrayList<>();
                if (text == null) return new String[0];
                String[] paragraphs = text.split("\\n");
                FontMetrics fm = g2.getFontMetrics();
                for (String para : paragraphs) {
                    if (para.isEmpty()) {
                        lines.add("");
                        continue;
                    }
                    String[] words = para.split(" ");
                    StringBuilder line = new StringBuilder();
                    for (String word : words) {
                        String test = line.length() == 0 ? word : line + " " + word;
                        if (fm.stringWidth(test) <= maxWidth) {
                            if (line.length() == 0) line.append(word);
                            else { line.append(" ").append(word); }
                        } else {
                            if (line.length() > 0) {
                                lines.add(line.toString());
                                line.setLength(0);
                            }
                            // word longer than maxWidth: break it
                            if (fm.stringWidth(word) <= maxWidth) {
                                line.append(word);
                            } else {
                                StringBuilder part = new StringBuilder();
                                for (char c : word.toCharArray()) {
                                    part.append(c);
                                    if (fm.stringWidth(part.toString()) > maxWidth) {
                                        // remove last char and add
                                        part.setLength(part.length() - 1);
                                        lines.add(part.toString());
                                        part.setLength(0);
                                        part.append(c);
                                    }
                                }
                                if (part.length() > 0) line.append(part.toString());
                            }
                        }
                    }
                    if (line.length() > 0) lines.add(line.toString());
                }
                return lines.toArray(new String[0]);
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
                    returnTitleButton.setVisible(true);
                    restartButton.setVisible(true);
                    
            }
            private void drawVictory(Graphics2D g2) {
                g2.setColor(new Color(0, 0, 0, 200));
                    g2.fillRect(0, 0, screenWidth, screenHeight);

                    g2.setColor(Color.GREEN);
                    g2.setFont(new Font("Arial", Font.BOLD, 56));
                    String ending = "Parabéns! Fim do jogo";
                    int ew = g2.getFontMetrics().stringWidth(ending);
                    g2.drawString(ending, screenWidth/2 - ew/2, screenHeight/2 - 50);
                    returnTitleButton.setVisible(true);
                    restartButton.setVisible(false);
            }

            private void drawFinalImage(Graphics2D g2) {
                if (cutsceneFinalBackground != null) {
                    g2.drawImage(cutsceneFinalBackground, 0, 0, screenWidth, screenHeight, this);
                } else {
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0, 0, screenWidth, screenHeight);
                }
            
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, screenWidth, screenHeight);
            
                int panelWidth = screenWidth - 200;
                int panelX = (screenWidth - panelWidth) / 2;
                int panelY = 90;
                int panelHeight = screenHeight - panelY - 140;
            
                g2.setColor(new Color(0, 0, 0, 190));
                g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 28, 28);
            
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 42));
                String title = "Obrigada por jogar";
                int titleWidth = g2.getFontMetrics().stringWidth(title);
                g2.drawString(title, screenWidth / 2 - titleWidth / 2, panelY + 70);
            
                g2.setFont(new Font("Arial", Font.PLAIN, 22));
                String subtitle = "Desenvolvido por:";
                int subtitleWidth = g2.getFontMetrics().stringWidth(subtitle);
                g2.drawString(subtitle, screenWidth / 2 - subtitleWidth / 2, panelY + 120);
            
                String[] developers = new String[] {
                    "Daniel Damasceno",
                    "João Pedro Santos",
                    "Leticia Pires",
                    "Mateus Fagundes",
                    "Victor Dias"
                };
            
                g2.setFont(new Font("Arial", Font.PLAIN, 26));
                int lineHeight = 40;
                int devsBlockHeight = developers.length * lineHeight;
                int availableSpace = panelHeight - 120;
                int devStartY = panelY + 120 + Math.max(0, (availableSpace - devsBlockHeight) / 2) + lineHeight;
            
                for (int i = 0; i < developers.length; i++) {
                    String dev = developers[i];
                    int dw = g2.getFontMetrics().stringWidth(dev);
                    g2.drawString(dev, screenWidth / 2 - dw / 2, devStartY + i * lineHeight);
                }
                restartButton.setVisible(true);
            }

            /**
             * Atualiza a lógica do jogo a cada frame.
             */
            public void update() {
                switch (gameState) {
                    case PLAY_STATE:      playState();     break;
                    case DIALOG_STATE:    dialogState();   break;
                    case HIDE_SEQUENCE_STATE: hideSequenceState(); break;
                    case CUTSCENE_1_STATE: cutscene1State(); break;
                    case CUTSCENE_2_STATE: cutscene2State(); break;
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
                    case HIDE_SEQUENCE_STATE: drawHideSequence(g2); break;
                    case CUTSCENE_1_STATE: drawCutscene(g2);     break;
                    case CUTSCENE_2_STATE: drawCutscene(g2);     break;
                    case FADE_STATE:      drawFade(g2);         break;
                    case FADE_IN_STATE:   drawFadeIn(g2);       break;
                    case FADE_OUT_STATE:  drawFadeOut(g2);      break;
                    case LOADING_STATE:   drawLoading(g2);      break;
                    case TRANS_STATE:     drawTrans(g2);        break;
                    case GAME_OVER_STATE: drawGameOver(g2);     break;
                    case VICTORY_STATE:   drawVictory(g2);      break;
                    case FINAL_IMAGE_STATE: drawFinalImage(g2);  break;
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
