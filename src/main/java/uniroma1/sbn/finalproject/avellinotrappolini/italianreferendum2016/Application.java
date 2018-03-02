/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016;

import java.util.ArrayList;
import org.apache.lucene.document.Document;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class Application {
    
    public static void main(String[] args) {
        
        TweetsIndexManager tim = new TweetsIndexManager("stream", "index/AllTweetsIndex");
        PoliticiansIndexManager pim = new PoliticiansIndexManager("stream", "index/AllPoliticiansIndex");
        
        //pim.create();
        ArrayList<Document> yesPoliticians = pim.searchForField("vote", "si", 10000);
        ArrayList<Document> noPoliticians = pim.searchForField("vote", "no", 10000);
        
        if(yesPoliticians != null && noPoliticians != null){
            System.out.println("YES POLITICIANS: " + yesPoliticians.size());
            System.out.println( "NO POLITICIANS: " + noPoliticians.size());
            System.out.println("TOT POLITICIANS: " + (yesPoliticians.size() + noPoliticians.size()));
        }
        
        TweetsIndexManager timYes = new TweetsIndexManager("index/AllTweetsIndex", "index/AllPolitcianssIndex", "index/AllYesTweetsIndex");
        TweetsIndexManager timNo = new TweetsIndexManager("index/AllTweetsIndex", "index/AllPolitcianssIndex", "index/AllNoTweetsIndex");
        timYes.create("vote", "si");
        timNo.create("vote", "no");

        int sizeYes = timYes.getIndexSizes();
        int sizeNo = timNo.getIndexSizes();
        System.out.println("");
        System.out.println("YES TWEETS: " + sizeYes);
        System.out.println( "NO TWEETS: " + sizeNo);
        System.out.println("TOT TWEETS: " + (sizeYes + sizeNo));    
        
           
    }
}