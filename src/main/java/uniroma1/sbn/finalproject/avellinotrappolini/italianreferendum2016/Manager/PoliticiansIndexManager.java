/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.PoliticiansIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class PoliticiansIndexManager extends IndexManager{

    /**
     *
     * @param indexPath
     */
    public PoliticiansIndexManager(String indexPath){
        super(indexPath);
    }
    
    @Override
    public void create(String sourcePath) {
        System.out.println("Politicians Index Creation!");
        PoliticiansIndexBuilder tib = new PoliticiansIndexBuilder(sourcePath, indexPath);      
        try {
            tib.build();
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
        } catch (TwitterException ex) {
            Logger.getLogger(PoliticiansIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void create(String sourcePath, String fieldName, ArrayList<String> fieldValues){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
