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
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.VoteIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.PoliticiansIndexBuilder;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetsIndexBuilder;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.VoteIndexBuilder;
import org.math.plot.*;

/**
 *
 * @author Gabriele
 */
public class Application {
    
    public static void main(String[] args) {
        
        TweetsIndexManager tim = TweetsIndexManager.getInstance();
        PoliticiansIndexManager pim = PoliticiansIndexManager.getInstance();
        
        //pim.create();
        int[] politicianSizes = pim.getAnalytics();
        
        System.out.println("YES POLITICIANS: " + politicianSizes[0]);
        System.out.println( "NO POLITICIANS: " + politicianSizes[1]);
        System.out.println("TOT POLITICIANS: " + (politicianSizes[0] + politicianSizes[1]));
        
        VoteIndexManager vim = VoteIndexManager.getInstance();
        int[] TweetSizes = vim.getSizes();
        
        System.out.println("");
        System.out.println("YES TWEETS: " + TweetSizes[0]);
        System.out.println( "NO TWEETS: " + TweetSizes[1]);
        System.out.println("TOT TWEETS: " + (TweetSizes[0] + TweetSizes[1]));    
        
        
//        try {
//            tim.getAllDocuments();
//        } catch (IOException ex) {
//            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
//        }
        tim.getDistros();
        
        //vib.create("YesTweetsIndex", "si");
        //vib.create("NoTweetsIndex", "no");
        
        
    }
}