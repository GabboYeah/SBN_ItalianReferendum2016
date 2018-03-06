/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class TermFreqIndexBuilder {

    private HashMap<String, int[]> termFreqindex;
    private long stepSize;
    private String indexPath;

    public TermFreqIndexBuilder(long stepSize, String indexPath) {
        this.stepSize = stepSize;
        this.indexPath = indexPath;
    }

    public void build() throws IOException, TwitterException {
        try {
            Directory dir = new SimpleFSDirectory(new File(indexPath));
            IndexReader ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);

            Fields fields = MultiFields.getFields(ir);
            Terms terms = fields.terms("tweetText");
            System.out.println();
            TermsEnum termsEnum = terms.iterator(null);

            while (termsEnum.next() != null) {
                BytesRef byteRef = termsEnum.term();

                System.out.println("-------------> " + new String(byteRef.bytes, byteRef.offset, byteRef.length));
                String word = new String(byteRef.bytes, byteRef.offset, byteRef.length);

                Analyzer stdAn = new StandardAnalyzer(Version.LUCENE_41);
                QueryParser parser = new QueryParser(Version.LUCENE_41, "tweetText", stdAn);
                Query q;
                try {
                    q = parser.parse(word);

                    TopDocs hits = searcher.search(q, 1000000);
                    ScoreDoc[] scoreDocs = hits.scoreDocs;
                    System.out.println(scoreDocs.length);
                    for(ScoreDoc sd : scoreDocs){
                        System.out.println(ir.document(sd.doc).get("tweetText"));
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(TweetsIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
