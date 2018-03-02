/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.IOException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.PoliticiansIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class PoliticiansIndexManager extends IndexManager{

    public PoliticiansIndexManager(String sourcePath, String indexPath){
        super(sourcePath, indexPath);
    }
    
    public void create() {
        System.out.println("Politicians Index Creation!");
        PoliticiansIndexBuilder tib = new PoliticiansIndexBuilder(sourcePath, indexPath);      
        try {
            tib.build();
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
        }
    }
}
