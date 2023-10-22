import javax.sound.sampled.*;
import java.io.*;

public class Sound{
	Clip clip;//clip used for sound
	
	public Sound(){
		
	}
	public void play(String filePath, boolean loop){//takes in the file path of the sound to be played and whether the sound should be looped
		try{
			File soundPath = new File(filePath);//finding sound file
			AudioInputStream sound = AudioSystem.getAudioInputStream(soundPath);//using previous file for an input stream

			clip = AudioSystem.getClip();
			clip.open(sound);
			
			if(loop){//if the sound is to be looped
				clip.loop(clip.LOOP_CONTINUOUSLY);
			}
			else{//played once
				clip.start();
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	public void stop(){//stops the clip from playing
		clip.stop();
	}
}