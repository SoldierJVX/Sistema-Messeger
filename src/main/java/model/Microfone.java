package model;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class Microfone {

    TargetDataLine microphone;
    
    public Microfone() throws Exception {
        
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);        
        
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        
        microphone.open(format);
        
        microphone.start();
    }
    
    public byte[] ouvir(){
        byte[] buf = new byte[256];
        microphone.read(buf, 0, buf.length);
        return buf;
    }
    
}
