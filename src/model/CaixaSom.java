package model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class CaixaSom {

    SourceDataLine caixaSom; /* I/O com a caixa de som */
    AudioFormat formatoAudio = new AudioFormat(8000.0f, 16, 1, true, true); /* qualidade do som */
    
   
    public CaixaSom() throws Exception {

        /* capturando a placa de som */
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, formatoAudio);
        caixaSom = (SourceDataLine) AudioSystem.getLine(info);
        caixaSom.open(formatoAudio);
        caixaSom.start();
        
    }
    
    public void tocar(byte[] audio) throws Exception {
        
        InputStream input = new ByteArrayInputStream(audio);
        AudioInputStream ais = new AudioInputStream(input, formatoAudio, audio.length / formatoAudio.getFrameSize());
        
        int count;
        byte buffer[] = new byte[256];
        
        /* tocando o som */
        while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
            if (count > 0) {
                caixaSom.write(buffer, 0, count);
            }
        }
    }
    
}
