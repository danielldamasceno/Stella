package com.stella.player;

import com.stella.core.GamePanel;
import com.stella.entities.Enemy;
import com.stella.entities.Entity;
import com.stella.entities.superObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
/**
 * Representa o jogador controlado pelo usuário.
 * Gerencia movimento, colisão e detecção de inimigos próximos.
 */
public class Player extends Entity {

    public static Enemy Enemy;

    // Referências para o painel do jogo e entrada do teclado
    GamePanel gp;
    KeyHandler key;
    
    // Posição do jogador na tela (fixa, câmera segue ele)
    public int screenX;
    public int screenY;

    public int lastWorldX, lastWorldY;
    public String currentRoom;

    // Imagens do jogador para cada direção (2 frames cada)
    BufferedImage back1, back2, front1, front2, left1, left2, right1, right2;
    // Imagens idle (parado) para cada direção
    BufferedImage idleBack, idleFront, idleLeft, idleRight;

    BufferedImage fearBarInit, fearbarMid, fearBarEnd, fearBarProgression;

    int animationCounter = 0;
    public boolean isMoving = false;
    public boolean autoWalk = false;
    public String autoWalkDirection = "right";
    
    // Texto de status atual (ex: "Barra de medo: 50%")
    private String situation;
    //private int winCondition = 0;
    /*  !!DEBUG!!
    // private String inDist; //Distância do inimigo mais próximo
        !!DEBUG!!   */  

    public Player(GamePanel gp, KeyHandler key){
        this.gp = gp;
        
        // Coloca o jogador no centro da tela
        screenX = gp.screenWidth/2-(gp.tileSz/2);
        screenY = gp.screenHeight/2-(gp.tileSz/2);
        
        // A posição no mundo começa onde a câmera está
        worldX = screenX+600;
        worldY = screenY+1200;
        
        // Define a área de colisão (pequena, não ocupa todo o tile)
        solidArea = new Rectangle(gp.tileSz/4, gp.tileSz/2, gp.tileSz/2, gp.tileSz/2);
        
        this.key = key;
        
        // Carrega as imagens do jogador
        getPlayerImages();
    }

    public void setStartPosition(int faseState) {
        screenX = gp.screenWidth / 2 - (gp.tileSz / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSz / 2);

        switch (faseState) {
            case 1: 
                worldX = 5 * gp.tileSz;
                worldY = 5 * gp.tileSz;
                break;
            case 2: 
                worldX = 10 * gp.tileSz;
                worldY = 21 * gp.tileSz;
                break;
            case 3:
                worldX = 43 * gp.tileSz;
                worldY = 15 * gp.tileSz;
                break;
        }
        direction = "bottom";
    } 

    /**
     * Atualiza o estado do jogador a cada frame.
     */
    public GamePanel getGamePanel() {
        return gp;
    }

    public void update(){
        // Checa proximidade com inimigos
        checkFear();
        // Checa proximidade com aliados (safezone)
        checkSafezone();

        /*if(checkSafezone() == true) {
            gp.FASE_STATE=2;
            screenX=gp.screenWidth/2;
            screenY=gp.screenHeight/2;
            worldX = screenX+800;
            worldY=screenY+400;
            //winCondition=0;
        }*/

        if (Fear.situation == 1.0) { 
            //gp.gameState = GamePanel.FADE_STATE; //debug 
        }
    }

    /**
     * Move o jogador baseado nas teclas pressionadas.
     * Só se move se não colidiu com nada.
     */
    public void andar(boolean blockMovement){

        if(blockMovement == true) {
            autoWalk = false;
            return;
        }

        if (autoWalk) {
            String intendedDirection = autoWalkDirection;
            switch (intendedDirection) {
                case "left":  worldX -= 1.4; break;
                case "right": worldX += 1.4; break;
                case "top":   worldY -= 1.4; break;
                case "bottom":worldY += 1.4; break;
            }
            direction = intendedDirection;
            isMoving = true;
            return; // ignora input do teclado
        }

        int moveX = 0;
        int moveY = 0;

        if (key.leftPressed) moveX -= 6;
        if (key.rightPressed) moveX += 6;
        if (key.upPressed) moveY -= 6;
        if (key.downPressed) moveY += 6;

        isMoving = moveX != 0 || moveY != 0;
        if (!isMoving) {
            return;
        }

        if (moveX != 0) {
            String horizontalDirection = moveX < 0 ? "left" : "right";
            direction = horizontalDirection;
            int oldX = worldX;
            worldX += moveX;
            gp.cChecker.checkTile(this, horizontalDirection);
            if (collisonOn) {
                worldX = oldX;
            }
        }

        if (moveY != 0) {
            String verticalDirection = moveY < 0 ? "top" : "bottom";
            direction = verticalDirection;
            int oldY = worldY;
            worldY += moveY;
            gp.cChecker.checkTile(this, verticalDirection);
            if (collisonOn) {
                worldY = oldY;
            }
        }
    }

    /**
     * Verifica proximidade com inimigos e aumenta o nível de medo.
     * Quanto mais perto do inimigo, maior o medo.
     */
    public void checkFear(){
        // Calcula a menor distância euclidiana entre o jogador e qualquer inimigo
        double minDist = Double.MAX_VALUE;
        double playerCenterX = worldX + gp.tileSz / 2.0;
        double playerCenterY = worldY + gp.tileSz / 2.0;

        for (superObject Obj1 : gp.obj) {
            if (Obj1 == null) continue;
            if (!Obj1.enemy) continue;

            double objCenterX = Obj1.WorldX + Obj1.width / 2.0;
            double objCenterY = Obj1.WorldY + Obj1.height / 2.0;

            double dx = objCenterX - playerCenterX;
            double dy = objCenterY - playerCenterY;

            double dist = Math.hypot(dx, dy); // distância euclidiana
            if (dist < minDist) minDist = dist;
        }


        // Atualiza a situação com base na menor distância encontrada
        if (minDist == Double.MAX_VALUE) {
            situation = null; // nenhum inimigo presente
            return;
        }
        Fear.distanceFear(minDist); // Atualiza o medo com base na distância
        Fear.updateFear(minDist); // Atualiza o medo com base na distância
        situation = Fear.getFearLevel(); // Atualiza o texto de status com o nível de medo atual
        //inDist = String.format("%.2f", minDist); // !!DEBUG!! Mostra a distância do inimigo mais próximo !!DEBUG!!
    }

    public boolean checkSafezone() {

        double minDisttoAlly = Double.MAX_VALUE;
        double playerCenterX = worldX + gp.tileSz / 2.0;
        double playerCenterY = worldY + gp.tileSz / 2.0;

        for (superObject Obj1 : gp.obj) {
            if (Obj1 == null) continue;
            if (!Obj1.ally) continue;

            double objCenterX = Obj1.WorldX + Obj1.width / 2.0;
            double objCenterY = Obj1.WorldY + Obj1.height / 2.0;

            double dx = objCenterX - playerCenterX;
            double dy = objCenterY - playerCenterY;

            double dist = Math.hypot(dx, dy); // distância euclidiana
            if (dist < minDisttoAlly) minDisttoAlly = dist;

            if (minDisttoAlly < 150) {
                Fear.situation = 0; // Zona segura, medo zerado
                situation = Fear.getFearLevel(); // Atualiza o status
                //winCondition = 1;
                return true;
            }
        }
        return false;
    }
    
    /**
     * Carrega todas as imagens do jogador (movimento + idle) da pasta /res/MC.
     */
    public void getPlayerImages(){
        try {
            // Imagens de movimento
            back1 = ImageIO.read(getClass().getResourceAsStream("/res/MC/back1.png"));
            back2 = ImageIO.read(getClass().getResourceAsStream("/res/MC/back2.png"));
            front1 = ImageIO.read(getClass().getResourceAsStream("/res/MC/front1.png"));
            front2 = ImageIO.read(getClass().getResourceAsStream("/res/MC/front2.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/res/MC/left1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/res/MC/left2.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/res/MC/right1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/res/MC/right2.png"));
            
            // Imagens idle (parado)
            idleBack = ImageIO.read(getClass().getResourceAsStream("/res/MC/backIdle.png"));
            idleFront = ImageIO.read(getClass().getResourceAsStream("/res/MC/frontidle.png"));
            idleLeft = ImageIO.read(getClass().getResourceAsStream("/res/MC/leftIdle.png"));
            idleRight = ImageIO.read(getClass().getResourceAsStream("/res/MC/rightIdle.png"));
        } catch (IOException e) {
            System.err.println("Erro ao carregar imagens do jogador: " + e.getMessage());
        }
    }

    public int getSituation() {
        return Integer.parseInt(situation);
    }
    /**
     * Desenha o jogador na tela com a imagem apropriada.
     * Mostra animação quando em movimento, pose idle quando parado.
     */
    public void Draw(Graphics2D g2){
        // Seleciona a imagem baseada na direção e estado de movimento
        BufferedImage image = null;
        
        if(isMoving){
            // Animação de movimento
            animationCounter++;
            int frame = (animationCounter / 15) % 4; // Alterna cada 20 frames
            switch(direction){
                case "top":
                    if(frame==0){
                        image = back1;
                    }else if(frame==1){
                        image=idleBack;
                    }else if(frame==2){
                        
                        image = back2;
                    }else if(frame==3){
                        image=idleBack;
                    }
                      
                    break;
                case "bottom":
                    if(frame==0){
                        image = front1;
                    }else if(frame==1){
                        image=idleFront;
                        
                    }else if(frame==2){
                        image = front2;
                    }else if(frame==3){
                        image=idleFront;
                        
                    }
                    break;
                case "left":
                    if(frame==0){
                        image = left1;
                    }else if(frame==1){
                        image=idleLeft;
                        
                    }else if(frame==2){
                        image = left2;
                    }else if(frame==3){
                        image=idleLeft;
                        
                    }
                    break;
                case "right":
                    if(frame==0){
                        image = right1;
                    }else if(frame==1){
                        image=idleRight;
                        
                    }else if(frame==2){
                        image = right2;
                    }else if(frame==3){
                        image=idleRight;
                        
                    }
                    break;
            }
        } else {
            // Pose idle quando parado
            switch(direction){
                case "top":
                    image = idleBack;
                    break;
                case "bottom":
                    image = idleFront;
                    break;
                case "left":
                    image = idleLeft;
                    break;
                case "right":
                    image = idleRight;
                    break;
            }
            animationCounter = 0; // Reseta animação quando parado
        }
        
        // Desenha a imagem ou um quadrado rosa como fallback
        if(image != null){
            g2.drawImage(image, screenX, screenY, gp.tileSz, gp.tileSz, null);
        } else {
            g2.setColor(Color.PINK);
            g2.fillRect(screenX, screenY, gp.tileSz, gp.tileSz);
        }
        /*  // !!DEBUG!!
        // Mostra o status atual (nível de medo)
        if(situation != null){
            g2.setColor(Color.WHITE);
            g2.drawString(situation, 100, 100);
            g2.drawString(inDist, 100, 120); // Mostra a distância do inimigo mais próximo
        }
           // !!DEBUG!!  */
    }
}
