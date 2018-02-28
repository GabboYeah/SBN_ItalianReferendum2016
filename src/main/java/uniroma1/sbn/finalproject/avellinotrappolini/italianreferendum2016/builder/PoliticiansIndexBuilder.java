/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.CSVReader;

/**
 *
 * @author Gabriele
 */
public class PoliticiansIndexBuilder{

    HashMap<String, String> groupVote;

    public PoliticiansIndexBuilder() {
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
    
    public void create(String delimiter, String path, String indexDir, int[] relevantCols) throws IOException {
        CSVReader csvr = new CSVReader(delimiter, path);
        ArrayList<String[]> rows = csvr.readCSV();
        for(String[] row : rows){
            String name = row[relevantCols[0]];
            String surname = row[relevantCols[1]];
            System.out.println((name + " " + surname).toLowerCase());
            findUserTwitterId(name, surname);
        }
    }
    
    public String findUserTwitterId(String name, String surname){
        try {
            Directory dir = new SimpleFSDirectory(new File("AllTweetsIndex"));
            IndexReader ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);

            Query q = new TermQuery(new Term("name", (name + " " + surname).toLowerCase()));
            TopDocs top = searcher.search(q, 100);
            ScoreDoc[] hits = top.scoreDocs;

            Document doc = null;

            for (ScoreDoc entry : hits) {

                doc = searcher.doc(entry.doc);
                System.out.println("-------------------------");
                System.out.println("+NAME: " + doc.get("name"));
                System.out.println("+SCREENNAME: " + doc.get("screenName"));
                System.out.println("+TEXT: " + doc.get("tweetText"));
                System.out.println("+HASHTAGS: " + doc.get("hashtags"));
                System.out.println("+FOLLOWERS: " + doc.get("followers"));
                System.out.println("");

            }

            if(hits.length > 0)
                return searcher.doc(hits[0].doc).get("screenName");
            
            return null;
            
        } catch (IOException ex) {
            Logger.getLogger(PoliticiansIndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }    
    }  
}
