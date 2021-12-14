/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.internalizable.musically.utilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import me.internalizable.musically.persistence.Song;

/**
 *
 * @author Internalizable
 */
public class FileUtils {
    
    public static ImageIcon getImage(Song song, int w, int h) {
        
        ImageIcon icon = null;
        
        if(song.getArtworkLink() != null) {
                URL url;
                String artwork = song.getArtworkLink().replace("{w}", String.valueOf(w)).replace("{h}", String.valueOf(h));
                try {
                    File imageFile = new File("data/" + Base64.getEncoder().encodeToString((artwork).getBytes()));
                    BufferedImage image;
                    
                    if(!imageFile.exists()) {
                        url = new URL(artwork);
                        
                        image = ImageIO.read(url);
                        ImageIO.write(image, "jpg", imageFile);
                    } else 
                        image = ImageIO.read(imageFile);
   
                    icon = new ImageIcon(image);
                } catch (Exception ignored) {
                    
                }
        }
        
        return icon;
    }
}
