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
        File dir = new File(indexPath);
	
        if(!dir.exists()){
            this.create();
        }
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

}
