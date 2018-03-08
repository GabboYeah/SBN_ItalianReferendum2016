/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.api.scripting.JSObject;
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

    private HashMap<String, double[]> termFreqIndex;
    private long stepSize;
    private String indexPath;
    private static long max = 1481036346994L;
    private static long min = 1480170614348L;

    public TermFreqIndexBuilder(long stepSize, String indexPath) {
        this.stepSize = stepSize;
        this.indexPath = indexPath;
    }

    public HashMap<String, double[]> build(String jsonPath) throws IOException, TwitterException {
        try {
            Directory dir = new SimpleFSDirectory(new File(indexPath));
            IndexReader ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);

            Fields fields = MultiFields.getFields(ir);
            String[] relevantFields = {"tweetText", "hashtags"};

            int arraySize = (int) (((max - min) / stepSize) + 1);
            termFreqIndex = new HashMap<String, double[]>();
            double[] initialArray = new double[arraySize];

            int i;

            for (i = 0; i < arraySize; i++) {
                initialArray[i] = 0;
            }

            for (String rel : relevantFields) {
                Terms terms = fields.terms(rel);

                System.out.println("number of words: " + terms.size());

                TermsEnum termsEnum = terms.iterator(null);

                while (termsEnum.next() != null) {
                    BytesRef byteRef = termsEnum.term();

                    System.out.println("-------------> " + new String(byteRef.bytes, byteRef.offset, byteRef.length));
                    String word = new String(byteRef.bytes, byteRef.offset, byteRef.length);

                    double[] wordValues = initialArray.clone();

                    Analyzer stdAn = new StandardAnalyzer(Version.LUCENE_41);
                    QueryParser parser = new QueryParser(Version.LUCENE_41, rel, stdAn);
                    Query q;
                    try {
                        q = parser.parse(word);

                        TopDocs hits = searcher.search(q, 1000000);
                        ScoreDoc[] scoreDocs = hits.scoreDocs;
                        System.out.println(scoreDocs.length);
                        for (ScoreDoc sd : scoreDocs) {
                            i = (int) ((Long.parseLong(ir.document(sd.doc).get("date")) - min) / stepSize);
                            wordValues[i]++;
                        }
                        termFreqIndex.put(word, wordValues);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(jsonPath), termFreqIndex);

            for (String key : termFreqIndex.keySet()) {
                System.out.println();
                System.out.println(key);

                for (double value : termFreqIndex.get(key)) {
                    System.out.print(value + " ");
                }

                System.out.println();
            }

            return termFreqIndex;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
