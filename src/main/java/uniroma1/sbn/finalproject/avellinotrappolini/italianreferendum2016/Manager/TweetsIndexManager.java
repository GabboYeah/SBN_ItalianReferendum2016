/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetsIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class TweetsIndexManager {
    
    private static TweetsIndexManager instance = new TweetsIndexManager();
    
    private static String sourcePath = "stream";
    private static String indexPath = "AllTweetsIndex";
    
    
    private TweetsIndexManager(){
//        File dir = new File("AllTweetsIndex");
//	
//        if(!dir.exists()){
//            this.create();
//        }
    }
    
    public static TweetsIndexManager getInstance(){
      return instance;
    }
    
    private void create() {
        System.out.println("TweetsIndex Creation!");
        TweetsIndexBuilder tib = new TweetsIndexBuilder();      
        Path streamDirPath = Paths.get(sourcePath);     
        tib.create(streamDirPath, indexPath);
    }
    
    public ArrayList<Document> searchForName(String name) throws IOException{
        Directory dir = new SimpleFSDirectory(new File(indexPath));
        IndexReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);

        Query q = new TermQuery(new Term("name", (name).toLowerCase()));
        TopDocs top = searcher.search(q, 100);
        ScoreDoc[] hits = top.scoreDocs;

        ArrayList<Document> results = new ArrayList<>();

        Document doc = null;

        for (ScoreDoc entry : hits) {
            doc = searcher.doc(entry.doc);
            results.add(doc);
        }
        
        return results;
    }
    
    public ArrayList<Document> searchForScreenName(String name) throws IOException{
        Directory dir = new SimpleFSDirectory(new File(indexPath));
        IndexReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);

        Query q = new TermQuery(new Term("screenName", (name).toLowerCase()));
        TopDocs top = searcher.search(q, 800);
        ScoreDoc[] hits = top.scoreDocs;

        ArrayList<Document> results = new ArrayList<>();

        Document doc = null;

        for (ScoreDoc entry : hits) {
            doc = searcher.doc(entry.doc);
            results.add(doc);
        }
        
        return results;
    }
        
    public void getAllDocuments() throws IOException{
        Directory dir = new SimpleFSDirectory(new File(indexPath));
        IndexReader ir = DirectoryReader.open(dir);

        Document doc = null;
        
        int i;
        
        for (i = 0; i < ir.maxDoc(); i++) {
            doc = ir.document(i);
            System.out.println(doc.get("name") + " " + doc.get("date"));
        }
    }
    
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
            Directory dir = new SimpleFSDirectory(new File(indexPath));
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
