/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.File;
import java.io.IOException;
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

/**
 *
 * @author Gabriele
 */
public abstract class IndexManager {

    public String sourcePath = "politicians.csv";
    public String indexPath = "AllPoliticiansIndex";

    public IndexManager(String sourcePath, String indexPath) {
        this.sourcePath = sourcePath;
        this.indexPath = indexPath;
    }
    
    public IndexManager(String tweetsSourcePath, String polsSourcePath, String indexPath){
        
    }

    public abstract void create();

    public ArrayList<Document> searchForField(String fieldName, String fieldValue, int range) {
        Directory dir;
        try {
            dir = new SimpleFSDirectory(new File(indexPath));
            
            IndexReader ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);

            Query q = new TermQuery(new Term(fieldName, fieldValue));
            TopDocs top = searcher.search(q, range);
            ScoreDoc[] hits = top.scoreDocs;

            ArrayList<Document> results = new ArrayList<>();

            Document doc = null;

            for (ScoreDoc entry : hits) {
                doc = searcher.doc(entry.doc);
                results.add(doc);
            }

            return results;
            
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
            
            return null;
        }
    }
    
    public int getIndexSizes() {
        try {
            Directory dir = new SimpleFSDirectory(new File(indexPath));
            IndexReader ir = DirectoryReader.open(dir);
            return ir.numDocs();

        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
            return -1;
        }
    }
}
