/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jdk.nashorn.api.scripting.JSObject;
import net.seninp.jmotif.sax.SAXException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetWord;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class TermFreqIndexBuilder {

    private ArrayList<TweetWord> relWords;
    private HashMap<String, ArrayList> invertedIndex;
    private long stepSize;
    private String indexPath;
    private static long max = 1481036346994L;
    private static long min = 1480170614348L;

    public TermFreqIndexBuilder(long stepSize, String indexPath) {
        this.stepSize = stepSize;
        this.indexPath = indexPath;
        this.relWords = new ArrayList<TweetWord>();
        this.invertedIndex = new HashMap<String, ArrayList>();
    }

    public void build() throws IOException, TwitterException, ParseException, SAXException {
        Directory dir = new SimpleFSDirectory(new File(indexPath));
        IndexReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);

        Fields fields = MultiFields.getFields(ir);
        String[] relevantFields = {"tweetText"};

        int arraySize = (int) (((max - min) / stepSize) + 1);

        double[] initialArray = new double[arraySize];

        for (String rel : relevantFields) {
            Terms terms = fields.terms(rel);

            TermsEnum termsEnum = terms.iterator(null);

            long freq;
            String word;
            int i;

            TweetWordBuilder twb = new TweetWordBuilder(2, 0.01);

            while (termsEnum.next() != null) {
                freq = termsEnum.totalTermFreq();

                BytesRef byteRef = termsEnum.term();
                word = new String(byteRef.bytes, byteRef.offset, byteRef.length);
                word = word.replaceAll("[^(\\w|\\d|\\s)]", "");
                //System.out.println("-------------> " + word);
                //System.out.println("freq: " + freq);

                double[] wordValues = initialArray.clone();

                Analyzer stdAn = new StandardAnalyzer(Version.LUCENE_41);
                QueryParser parser = new QueryParser(Version.LUCENE_41, rel, stdAn);
                Query q;
                q = parser.parse(word);

                TopDocs hits = searcher.search(q, 1000000);
                ScoreDoc[] scoreDocs = hits.scoreDocs;
    
                ArrayList<Integer> invertedList = new ArrayList<Integer>();
                
                for (ScoreDoc sd : scoreDocs) {
                    i = (int) ((Long.parseLong(ir.document(sd.doc).get("date")) - min) / stepSize);
                    invertedList.add(sd.doc);
                    wordValues[i]++;
                }
                TweetWord tw = twb.build(word, wordValues, (int) freq);
                //System.out.println(tw.getSaxRep().matches("a+b+a*b*a*"));
                if (tw.getSaxRep().matches("a+b+a*b*a*")) {
                    relWords.add(tw);
                    invertedIndex.put(word, invertedList);
                }
            }
        }

        Collections.sort(relWords);

        relWords = (ArrayList) relWords.stream().limit(1000).collect(Collectors.toList());

        
    }

    public ArrayList<TweetWord> getRelWords() {
        return relWords;
    }

    public HashMap<String, ArrayList> getInvertedIndex() {
        return invertedIndex;
    }
    
    
}
