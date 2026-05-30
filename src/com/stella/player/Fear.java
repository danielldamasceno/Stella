package com.stella.player;


public class Fear {

    /** Distancia ao inimigo (0 = longe, 1 = muito perto) dentro do raio de 500px */
    public static double distIn = 0;

    /** Situação do jogador (0 = seguro, 1 = medo máximo) */
    public static double situation = 0;

    /** Parâmetros */
    private static double distanceTillFear = 420;
    private static double BASE_INCREASE = 0.0001;    // subida mínima por atualização
    private static double MAX_EXTRA = 0.04;       // extra quando a distância é 0
    private static double DECREASE_RATE = 0.02;   // descida fixa por atualização quando longe
    
    /*
    private static final double CLOSE_RETURN_RATE = 0.01; // taxa de redução gradual por atualização
    private static final double CLOSE_THRESHOLD_HIGH = 0.6; // abaixo disso, medo máximo é limitado a 60%
    private static final double CLOSE_THRESHOLD_MID = 0.4;  // abaixo disso, medo máximo é limitado a 40%
    private static final double CLOSE_THRESHOLD_LOW = 0.2;  // abaixo disso, medo máximo é limitado a 20%
    private static final double CLOSE_MAX_H = 0.6;       // medo máximo permitido quando muito perto
    private static final double CLOSE_MAX_M = 0.4;
    private static final double CLOSE_MAX_L = 0.2;
    */

   public static void setFearDificult(int dificulty) {
        switch (dificulty) {
            case 0: //facil
                distanceTillFear = 410;
                BASE_INCREASE = 0.00005; // metade da taxa de aumento
                MAX_EXTRA = 0.02;        // metade do aumento extra
                DECREASE_RATE = 0.04;    // dobra a taxa de diminuição
                break;
            case 1: //medio
                distanceTillFear = 460;
                BASE_INCREASE = 0.0001;
                MAX_EXTRA = 0.04;
                DECREASE_RATE = 0.02;
                break;
            case 2: //dificil
                distanceTillFear = 500;
                BASE_INCREASE = 0.0002; // dobra a taxa de aumento
                MAX_EXTRA = 0.08;       // dobra o aumento extra
                DECREASE_RATE = 0.01;   // metade da taxa de diminuição
                break;
        }
    }

    public static void distanceFear(double distance) {
        if (distance < distanceTillFear) {
            distIn = (distanceTillFear - distance) / distanceTillFear;
        } else {
            distIn = 0;
        }
        if (distIn > 1) distIn = 1;
        if (distIn < 0) distIn = 0;
    }

    public static void updateFear(double distance) {
        if (distance < distanceTillFear) {
            // Aumenta mais rápido quanto maior a proximidade (distIn)
            double inc = BASE_INCREASE + MAX_EXTRA * distIn * 0.16; // multiplicado por 0.16 para ajustar à taxa de atualização
            situation += inc;
            //if (distIn < 160) situation += 0.01 * 0.16; // aumento extra quando muito perto
        } else {
            // Diminui a uma taxa fixa quando longe
            situation -= DECREASE_RATE * 0.16; // multiplicado por 0.16 para ajustar à taxa de atualização
        }

        // Aplica limites gerais
        if (distIn > 0.9) { // GAME OVER! condição final de medo.
            situation = 1;
        }
        if (situation > 1) situation = 1;
        if (situation < 0) situation = 0;

        // Se estiver abaixo do limiar, reduzir gradualmente até CLOSE_MAX 
        /* 
        if (distIn < CLOSE_THRESHOLD_HIGH && situation > CLOSE_MAX_H) {
            situation -= CLOSE_RETURN_RATE * 0.16; // aplica redução gradual
            if (situation < CLOSE_MAX_H) situation = CLOSE_MAX_H;
        }
        if (distIn < CLOSE_THRESHOLD_MID && situation > CLOSE_MAX_M) {
            situation -= CLOSE_RETURN_RATE * 0.16; // aplica redução gradual
            if (situation < CLOSE_MAX_M) situation = CLOSE_MAX_M;
        }
        if (distIn < CLOSE_THRESHOLD_LOW && situation > CLOSE_MAX_L) {
            situation -= CLOSE_RETURN_RATE * 0.16; // aplica redução gradual
            if (situation < CLOSE_MAX_L) situation = CLOSE_MAX_L;
        } */
    }

    public static String getFearLevel() {
        return String.format("Barra de medo: %.1f%%", situation * 100);
    }

}
