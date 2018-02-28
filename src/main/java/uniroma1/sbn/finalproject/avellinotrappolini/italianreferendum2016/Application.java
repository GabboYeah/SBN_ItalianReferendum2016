/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.CSVReader;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.PoliticiansIndexBuilder;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetsIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class Application {
    
    public static void main(String[] args) {
//        //Nome, cognome, gruppo, voto, assenza
//        int[] relevantColsSenators = {4, 3, 25, 23, 24};
//        pCSVr.readCSV("senatori-votazione_5.csv", relevantColsSenators); 
        
        TweetsIndexManager tim = TweetsIndexManager.getInstance();
        //tim.create();
        try{
            ArrayList<Document> docs = tim.searchForName("matteo renzi");
            for(Document doc : docs){
                System.out.println(doc.get("screenName") +" "+ doc.get("followers"));
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Nome, cognome, gruppo, voto, assenza
//        int[] relevantColsParl = {4, 3, 15, 18};
//        int[] relevantColsSenators = {4, 3, 25, 23, 24};
//        PoliticiansIndexBuilder pib = new PoliticiansIndexBuilder();
//        try {
//            pib.create(",", "deputati_votazione_6.csv", "polititians", relevantColsParl);
//            pib.create(",", "senatori-votazione_5.csv", "polititians", relevantColsParl);
//        } catch (IOException ex) {
//            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}