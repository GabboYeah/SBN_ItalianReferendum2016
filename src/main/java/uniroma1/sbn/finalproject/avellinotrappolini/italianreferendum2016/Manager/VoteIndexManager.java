/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.VoteIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class VoteIndexManager {

    private static VoteIndexManager instance = new VoteIndexManager();

    private final String yesPath = "YesTweetsIndex";
    private final String noPath = "NoTweetsIndex";

    private VoteIndexManager() {

    }

    public static VoteIndexManager getInstance() {
        return instance;
    }

    public void create() {
        VoteIndexBuilder vib = new VoteIndexBuilder();

        vib.create(yesPath, "si");
        vib.create(noPath, "no");
    }

    public int[] getSizes() {
        int[] sizes = new int[2];

        try {
            Directory dir = new SimpleFSDirectory(new File(yesPath));
            IndexReader ir = DirectoryReader.open(dir);
            sizes[0] = ir.numDocs();

            dir = new SimpleFSDirectory(new File(noPath));
            ir = DirectoryReader.open(dir);
            sizes[1] = ir.numDocs();

            return sizes;

        } catch (IOException ex) {
            Logger.getLogger(VoteIndexManager.class.getName()).log(Level.SEVERE, null, ex);
            return sizes;
        }
    }

//    HashMap<String, int[]>
    public void getDistros() {

        int arraySize = 10;

        long[] days = {1480170614348L, 1480257098290L,
                       1480343526103L, 1480430203069L,
                       1480516673438L, 1480603295223L,
                       1480690014802L, 1480776565169L,
                       1480863128958L, 1480949681548L, 2480949681548L};

//        String[] days = {"1480170614348", "1480257098290",
//                         "1480343526103", "1480430203069",
//                         "1480516673438", "1480603295223",
//                         "1480690014802", "1480776565169",
//                         "1480863128958", "1480949681548"};
        HashMap<String, int[]> distros = new HashMap<String, int[]>();

        try {
            Directory dir = new SimpleFSDirectory(new File(yesPath));
            IndexReader ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);

            distros.put("si", new int[arraySize]);

            int i;
            TotalHitCountCollector collector;
            FieldCacheRangeFilter<Long> dateFilter;
            Query q;
            
            for (i = 0; i < arraySize; i++) {
          
                q = NumericRangeQuery.newLongRange("date", days[i], days[i + 1], true, false); 
                TopDocs top = searcher.search(q, 900); 
                
                System.out.println(top.totalHits);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(VoteIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
}
