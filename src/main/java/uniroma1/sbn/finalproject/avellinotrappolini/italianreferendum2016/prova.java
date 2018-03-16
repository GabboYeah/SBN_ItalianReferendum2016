/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;

/**
 *
 * @author Gabriele
 */
public class prova {
    public static void main(String[] args){
        
        Supporter s = new Supporter("giovannimerda", 0, 0, 0, 0, 0, 0);
        
        ObjectMapper mapper = new ObjectMapper();

        File file = new File("gio.json");
        try {
            // Serialize Java object info JSON file.
            mapper.writeValue(file, s);
            Supporter newArtist = mapper.readValue(file, Supporter.class);
            System.out.println(newArtist.getUserId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
