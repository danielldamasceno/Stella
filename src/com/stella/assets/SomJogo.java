package com.stella.assets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;

public class SomJogo {
    private Clip clipeTrilhaAtual;

    public synchronized void tocarTrilha(String caminhoArquivo) {
        pararTrilha();
        try {
            AudioInputStream streamAudio = getAudioStream(caminhoArquivo);
            if (streamAudio == null) {
                System.out.println("Arquivo de áudio não encontrado: " + caminhoArquivo);
                return;
            }

            clipeTrilhaAtual = AudioSystem.getClip();
            clipeTrilhaAtual.open(streamAudio);
            clipeTrilhaAtual.loop(Clip.LOOP_CONTINUOUSLY);
            clipeTrilhaAtual.start();
            if (streamAudio != null) {
                try { streamAudio.close(); } catch (IOException ignored) {}
            }
        } catch (IOException | LineUnavailableException e) {
            System.out.println("Erro ao carregar trilha: " + e.getMessage());
        }
    }

    public synchronized void pararTrilha() {
        if (clipeTrilhaAtual != null) {
            try {
                if (clipeTrilhaAtual.isRunning()) clipeTrilhaAtual.stop();
            } catch (Exception ignored) {}
            try {
                clipeTrilhaAtual.close();
            } catch (Exception ignored) {}
            clipeTrilhaAtual = null;
        }
    }

    public void tocarEfeito(String caminhoArquivo) {
        try {
            AudioInputStream streamAudio = getAudioStream(caminhoArquivo);
            if (streamAudio == null) {
                System.out.println("Arquivo de áudio não encontrado: " + caminhoArquivo);
                return;
            }

            Clip clipeEfeito = AudioSystem.getClip();
            clipeEfeito.open(streamAudio);
            clipeEfeito.start();

            clipeEfeito.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    try {
                        clipeEfeito.close();
                    } catch (Exception ignored) {}
                }
            });
            streamAudio.close();
        } catch (Exception e) {
            System.out.println("Erro ao tocar efeito: " + e.getMessage());
        }
    }

    private AudioInputStream getAudioStream(String caminhoArquivo) {
        try {
            // Tenta carregar como recurso de classpath (suporta JAR e IDE)
            String candidate = caminhoArquivo.startsWith("/") ? caminhoArquivo : "/res/" + caminhoArquivo;
            InputStream is = getClass().getResourceAsStream(candidate);
            if (is != null) {
                return AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            }

            // Tenta carregar como arquivo absoluto/relativo no sistema de arquivos
            File arquivo = new File(caminhoArquivo);
            if (arquivo.exists()) {
                return AudioSystem.getAudioInputStream(arquivo);
            }

            // Tenta pasta res/ no diretório de trabalho
            File arquivoRes = new File("res" + File.separator + caminhoArquivo);
            if (arquivoRes.exists()) {
                return AudioSystem.getAudioInputStream(arquivoRes);
            }
        } catch (Exception e) {
            System.out.println("Erro ao abrir stream de áudio: " + e.getMessage());
        }
        return null;
    }
}
