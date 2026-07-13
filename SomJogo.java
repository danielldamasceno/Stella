
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SomJogo {
    private Clip clipeTrilhaAtual;

    public void tocarTrilha(String caminhoArquivo) {
        pararTrilha();
        try {
            File arquivoAudio = new File(caminhoArquivo);
            AudioInputStream streamAudio = AudioSystem.getAudioInputStream(arquivoAudio);
            
            clipeTrilhaAtual = AudioSystem.getClip();
            clipeTrilhaAtual.open(streamAudio);
            clipeTrilhaAtual.loop(Clip.LOOP_CONTINUOUSLY);
            clipeTrilhaAtual.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Erro ao carregar trilha: " + e.getMessage());
        }
    }

    public void pararTrilha() {
        if (clipeTrilhaAtual != null && clipeTrilhaAtual.isRunning()) {
            clipeTrilhaAtual.stop();
            clipeTrilhaAtual.close();
        }
    }

    public void tocarEfeito(String caminhoArquivo) {
        try {
            File arquivoAudio = new File(caminhoArquivo);
            AudioInputStream streamAudio = AudioSystem.getAudioInputStream(arquivoAudio);
            
            Clip clipeEfeito = AudioSystem.getClip();
            clipeEfeito.open(streamAudio);
            clipeEfeito.start();
            
            clipeEfeito.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clipeEfeito.close();
                }
            });
        } catch (Exception e) {
            System.out.println("Erro ao tocar efeito: " + e.getMessage());
        }
    }
}
