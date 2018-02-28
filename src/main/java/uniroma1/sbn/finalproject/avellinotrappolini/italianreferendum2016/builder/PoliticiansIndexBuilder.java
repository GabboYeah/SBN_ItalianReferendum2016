/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import static org.apache.lucene.util.Version.LUCENE_41;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.CSVReader;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class PoliticiansIndexBuilder{

    HashMap<String, String> groupVote;
    
    private Directory dir;
    private Analyzer analyzer;
    private IndexWriterConfig cfg;
    private IndexWriter writer;
    
    private Document politician;
    private StringField vote;
    private StringField name;
    private StringField screenName;

    public PoliticiansIndexBuilder() {
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.vote = new StringField("vote", "", Field.Store.YES);
        
        politician.add(name);
        politician.add(screenName);
        politician.add(vote);
        
        groupVote = new HashMap<String, String>();
        groupVote.put("AP (NCD-UDC)", "no");
        groupVote.put("DS-CD", "no");
        groupVote.put("FI-PdL", "no");
        groupVote.put("FdI", "no");
        groupVote.put("Lega", "no");
        groupVote.put("M5S", "no");
        groupVote.put("PD", "no");
        groupVote.put("SCpI", "no");
        groupVote.put("SI-SEL", "no");
        groupVote.put("Misto", "no");
 
        groupVote.put("AL-A", "no");
        groupVote.put("Aut (SVP-UV-PATT-UPT-PSI)", "no");
        groupVote.put("CoR", "no");
        groupVote.put("GAL", "no");
    }
    
    public void create(String csvPath, String indexPath, String delimiter, int[] relevantCols) {
        CSVReader csvr = new CSVReader(delimiter, csvPath);
        ArrayList<String[]> rows;
        try {
            rows = csvr.readCSV();
        
            String id; 

            for(String[] row : rows){
                String name = row[relevantCols[0]];
                String surname = row[relevantCols[1]];
                System.out.println((name + " " + surname).toLowerCase());
                id = findUserTwitterId(name, surname);

            }
        } catch (IOException ex) {
            System.out.println("Impossibile leggere il CSV!");
        }
    }
    
    public String findUserTwitterId(String name, String surname) throws IOException{
        TweetsIndexManager tim = TweetsIndexManager.getInstance();
            
        ArrayList<Document> results = tim.searchForName((name + " " + surname).toLowerCase());
        int max = 0;
        String id = "";

        for(Document doc : results){
            if(Integer.parseInt(doc.get("followers")) >= max){
                max = Integer.parseInt(doc.get("followers"));
                id = doc.get("screenName");
            }
        }
        
        return id;
    }
    
    private void setBuilderParams(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));
        this.analyzer = new ItalianAnalyzer(LUCENE_41);
        this.cfg = new IndexWriterConfig(LUCENE_41, analyzer);
        this.writer = new IndexWriter(dir, cfg);
    }
}
