/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import static org.apache.lucene.util.Version.LUCENE_41;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.CSVReader;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class VoteIndexBuilder {
    private Directory dir;
    private Analyzer analyzer;
    private IndexWriterConfig cfg;
    private IndexWriter writer;
    
    public void create(String indexPath, String vote) {
        
        try {  
            setBuilderParams(indexPath);
            PoliticiansIndexManager pim = PoliticiansIndexManager.getInstance();
            TweetsIndexManager tim = TweetsIndexManager.getInstance();
            
            ArrayList<Document> politicians = pim.searchForVote(vote);
            
            ArrayList<Document> politcianTweets;
            
            for(Document p : politicians){
                //System.out.println("---------------------------");
                //System.out.println(p.get("name"));
                politcianTweets = tim.searchForScreenName(p.get("screenName"));
                
                //System.out.println(politcianTweets.size());
                for(Document tweet : politcianTweets){
                    this.writer.addDocument(tweet);
                }
                
                this.writer.commit();
            }
            
        } catch (IOException ex) {
            System.out.println("Impossibile leggere il CSV!");
            ex.printStackTrace();
        }
    }
    
    private void setBuilderParams(String indexPath) throws IOException {
        this.dir = new SimpleFSDirectory(new File(indexPath));
        this.analyzer = new ItalianAnalyzer(LUCENE_41);
        this.cfg = new IndexWriterConfig(LUCENE_41, analyzer);
        this.writer = new IndexWriter(dir, cfg);
    }
}
