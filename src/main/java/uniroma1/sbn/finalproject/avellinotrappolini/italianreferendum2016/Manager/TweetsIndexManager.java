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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetsIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class TweetsIndexManager extends IndexManager {

    private String tweetsSourcePath;
    private String polsSourcePath;
    private static long max = 1481036346994L;
    private static long min = 1480170614348L;

    public TweetsIndexManager(String sourcePath, String indexPath) {
        super(sourcePath, indexPath);
    }

    public TweetsIndexManager(String tweetsSourcePath, String polsSourcePath, String indexPath) {

        super(tweetsSourcePath, polsSourcePath, indexPath);

        this.tweetsSourcePath = tweetsSourcePath;
        this.polsSourcePath = polsSourcePath;
        this.indexPath = indexPath;
    }

    public void create() {
        System.out.println("Tweets Index Creation!");
        TweetsIndexBuilder tib = new TweetsIndexBuilder(sourcePath, indexPath);
        try {
            tib.build();
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
        } catch (TwitterException ex) {
            System.out.println("---> Problems with Tweets: TwitterException <---");
            ex.printStackTrace();
        }
    }

    public void create(String fieldName, String fieldValue) {
        TweetsIndexBuilder tib = new TweetsIndexBuilder(tweetsSourcePath, polsSourcePath, indexPath);

        try {
            tib.build(fieldName, fieldValue);
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
        }
    }

    public ArrayList<long[]> getTweetDistro(long stepSize) {
        int distroSize = (int) (((max - min) / stepSize) + 1);
        
        ArrayList<long[]> distro = new ArrayList<long []>();
        long[] x = new long[distroSize];
        long[] y = new long[distroSize];

        int i;

        Directory dir;
        try {
            dir = new SimpleFSDirectory(new File(indexPath));
            IndexReader ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);
            
            Query q;

            for (i = 0; i < distroSize; i++) {
                long leftBound = min + i * stepSize;
                long rightBound = min + (i + 1) * stepSize;
                
                q = NumericRangeQuery.newLongRange("date", leftBound, rightBound, true, false);
                TopDocs top = searcher.search(q, 10000);
                x[i] = rightBound;
                y[i] = top.totalHits;
            }
            
            distro.add(x);
            distro.add(y);
            
            return distro;
            
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
        
            return null;
        }
    }
    
//        public void getDistros() {
//
//        int arraySize = 10;
//
//        long[] days = {1480170614348L, 1480257098290L,
//            1480343526103L, 1480430203069L,
//            1480516673438L, 1480603295223L,
//            1480690014802L, 1480776565169L,
//            1480863128958L, 1480949681548L, 2480949681548L};
//
////        String[] days = {"1480170614348", "1480257098290",
////                         "1480343526103", "1480430203069",
////                         "1480516673438", "1480603295223",
////                         "1480690014802", "1480776565169",
////                         "1480863128958", "1480949681548"};
//        HashMap<String, int[]> distros = new HashMap<String, int[]>();
//
//        try {
//            Directory dir = new SimpleFSDirectory(new File(indexPath));
//            IndexReader ir = DirectoryReader.open(dir);
//            IndexSearcher searcher = new IndexSearcher(ir);
//
//            distros.put("si", new int[arraySize]);
//
//            int i;
//            TotalHitCountCollector collector;
//            FieldCacheRangeFilter<Long> dateFilter;
//            Query q;
//
//            for (i = 0; i < arraySize; i++) {
//
//                q = NumericRangeQuery.newLongRange("date", days[i], days[i + 1], true, false);
//                TopDocs top = searcher.search(q, 900);
//
//                System.out.println(top.totalHits);
//            }
//
//        } catch (IOException ex) {
//            Logger.getLogger(VoteIndexManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
