package com.stella.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Pequena utilitária para gerar números aleatórios dentro de um intervalo.
 */
public final class RandomUtils {
    private RandomUtils() {}

    /** Retorna um inteiro aleatório entre min e max */
    public static int randInt(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /** Retorna um double aleatório entre min e max */
    public static double randDouble(double min, double max) {
        if (min >= max) throw new IllegalArgumentException("min >= max");
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
