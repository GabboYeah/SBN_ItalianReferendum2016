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

    private HashMap<String, float[]> termFreqIndex;
    private long stepSize;
    private String indexPath;
    private static long max = 1481036346994L;
    private static long min = 1480170614348L;

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
            String[] relevantFields = {"tweetText", "hashtags"};
            
            int arraySize = (int) (((max - min) / stepSize) + 1);
            termFreqIndex = new HashMap<String, float[]>();
            float[] initialArray = new float[arraySize];
            
            int i;
            
            for (i = 0; i < arraySize; i++) {
                initialArray[i] = 0;
            }
            
            for (String rel : relevantFields) {
                Terms terms = fields.terms(rel);
                
                TermsEnum termsEnum = terms.iterator(null);

                while (termsEnum.next() != null) {
                    BytesRef byteRef = termsEnum.term();

                    System.out.println("-------------> " + new String(byteRef.bytes, byteRef.offset, byteRef.length));
                    String word = new String(byteRef.bytes, byteRef.offset, byteRef.length);
                    
                    
                    float[] wordValues = initialArray.clone();
                    
                    Analyzer stdAn = new StandardAnalyzer(Version.LUCENE_41);
                    QueryParser parser = new QueryParser(Version.LUCENE_41, "tweetText", stdAn);
                    Query q;
                    try {
                        q = parser.parse(word);

                        TopDocs hits = searcher.search(q, 1000000);
                        ScoreDoc[] scoreDocs = hits.scoreDocs;
                        System.out.println(scoreDocs.length);
                        for (ScoreDoc sd : scoreDocs) {
                            System.out.println(ir.document(sd.doc).get("tweetText") + " " + ir.document(sd.doc).get("date"));
                            i = 1;
                            while (Long.parseLong(ir.document(sd.doc).get("date")) > (i+1)*stepSize + min) {
                                System.out.println(i + " " + ir.document(sd.doc).get("date") + " " + ((i+1)*stepSize + min));
                                i++;
                            }
                            wordValues[i]++;
                        }
                        termFreqIndex.put(word, wordValues);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
            for(String key : termFreqIndex.keySet()){
                System.out.println(key);
                System.out.println();
                
                for(float value : termFreqIndex.get(key)){
                    System.out.print(value + " ");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TweetsIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
