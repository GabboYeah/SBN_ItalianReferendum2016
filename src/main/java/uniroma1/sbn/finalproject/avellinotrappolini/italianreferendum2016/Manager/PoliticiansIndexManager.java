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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.PoliticiansIndexBuilder;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetsIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class PoliticiansIndexManager {
    private static PoliticiansIndexManager instance = new PoliticiansIndexManager();
    
    private static String sourcePath = "politicians.csv";
    private static String indexPath = "AllPoliticiansIndex";
    
    
    private PoliticiansIndexManager(){
//        File dir = new File(indexPath);
//	
//        if(!dir.exists()){
//            this.create();
//        }
    }
    
    public static PoliticiansIndexManager getInstance(){
      return instance;
    }
    
    public void create() {
        System.out.println("PoliticiansIndex Creation!");
        PoliticiansIndexBuilder tib = new PoliticiansIndexBuilder();      
        tib.create(sourcePath, indexPath, ",");
    }
    
    public void getAnalytics() {
        try{
            ArrayList<Document> yesResults = searchForVote("si");
            ArrayList<Document> noResults = searchForVote("no");
            
            System.out.println("YES: " + yesResults.size());
            System.out.println("NO: " + noResults.size());
            System.out.println("TOT: " + (yesResults.size() + noResults.size()));
            
        } catch (IOException ex) {
            System.out.println("Errore nell'apertura della cartella " + indexPath);
            ex.printStackTrace();
        }
    }
    
    public ArrayList<Document> searchForVote(String vote) throws IOException{
        Directory dir = new SimpleFSDirectory(new File(indexPath));
        IndexReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);

        Query q = new TermQuery(new Term("vote", vote));
        TopDocs top = searcher.search(q, 900);
        ScoreDoc[] hits = top.scoreDocs;

        ArrayList<Document> results = new ArrayList<>();

        Document doc = null;

        for (ScoreDoc entry : hits) {
            doc = searcher.doc(entry.doc);
            results.add(doc);
        }
        
        return results;
    }
}
