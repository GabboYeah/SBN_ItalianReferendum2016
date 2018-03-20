/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.seninp.jmotif.sax.SAXException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetTerm;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetWordBuilder;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetsIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class TweetsIndexManager extends IndexManager {

    private static long max = 1481036346994L;
    private static long min = 1480170614348L;

    /**
     *
     * @param indexPath
     */
    public TweetsIndexManager(String indexPath) {
        super(indexPath);
    }

    @Override
    public void create(String sourcePath) {
        System.out.println("Tweets Index Creation!");
        TweetsIndexBuilder tib = new TweetsIndexBuilder(sourcePath, indexPath);
        try {
            tib.build();
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
        } catch (TwitterException ex) {
            System.out.println("---> Problems with Tweets: TwitterException <---");
            ex.printStackTrace();
        }
    }

    @Override
    //from index
    public void create(String sourcePath, String fieldName, ArrayList<String> fieldValues) {
        TweetsIndexBuilder tib = new TweetsIndexBuilder(sourcePath, indexPath);

        try {
            tib.build(fieldName, fieldValues);
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param stepSize
     * @return
     */
    public ArrayList<long[]> getTweetDistro(long stepSize) {
        int distroSize = (int) (((max - min) / stepSize) + 1);

        ArrayList<long[]> distro = new ArrayList<long[]>();
        long[] x = new long[distroSize];
        long[] y = new long[distroSize];

        int i;

        Directory dir;
        try {
            dir = new SimpleFSDirectory(new File(indexPath));
            IndexReader ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);

            Query q;

            for (i = 0; i < distroSize; i++) {
                long leftBound = min + i * stepSize;
                long rightBound = min + (i + 1) * stepSize;

                q = NumericRangeQuery.newLongRange("date", leftBound, rightBound, true, false);
                TopDocs top = searcher.search(q, 10000);
                x[i] = rightBound;
                y[i] = top.totalHits;
            }

            distro.add(x);
            distro.add(y);

            return distro;

        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();

            return null;
        }
    }

    /**
     *
     * @param fieldNames
     * @param regex
     * @param stepSize
     * @return
     */
    public ArrayList<TweetTerm> getRelFieldTerms(String[] fieldNames, String regex, long stepSize) {
        try {
            setReader(indexPath);

            ArrayList<TweetTerm> relWords = new ArrayList<TweetTerm>();

            Fields fields = MultiFields.getFields(ir);
            String[] relevantFields = fieldNames;

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
                    word = byteRef.utf8ToString();

                    double[] wordValues = getTermTimeSeries(word, rel, stepSize);

                    TweetTerm tw = twb.build(word, rel, wordValues, (int) freq);

                    //System.out.println(tw.getSaxRep().matches("a+b+a*b*a*"));
                    if (tw.getSaxRep().matches(regex)) {
                        relWords.add(tw);
                    }
                }
            }

            Collections.sort(relWords);

            relWords = (ArrayList) relWords.stream().limit(1000).collect(Collectors.toList());

            return relWords;

        } catch (IOException ex) {
            Logger.getLogger(TweetsIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(TweetsIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     *
     * @param term
     * @param field
     * @param stepSize
     * @return
     */
    public double[] getTermTimeSeries(String term, String field, double stepSize) {

        int arraySize = (int) (((max - min) / stepSize) + 1);
        double[] wordValues = new double[arraySize];

        ScoreDoc[] scoreDocs = searchTermInAField(term, field);

        try {
            for (ScoreDoc sd : scoreDocs) {
                int i;

                i = (int) ((Long.parseLong(ir.document(sd.doc).get("date")) - min) / stepSize);

                wordValues[i]++;
            }
        } catch (IOException ex) {
            Logger.getLogger(TweetsIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return wordValues;
    }

    public ScoreDoc[] searchORANDCondInAField(ArrayList<ArrayList<String>> compList) {

        BooleanQuery query = new BooleanQuery();
        for (ArrayList<String> comp : compList) {
            BooleanQuery compQuery = new BooleanQuery();

            for (String term : comp) {
                if (term.startsWith("#")) {
                    compQuery.add(new TermQuery(new Term("hashtags", term)), BooleanClause.Occur.MUST);
                } else {
                    compQuery.add(new TermQuery(new Term("tweetText", term)), BooleanClause.Occur.MUST);
                }
            }

            query.add(compQuery, BooleanClause.Occur.SHOULD);
        }

        try {
            TopDocs hits = searcher.search(query, 10000000);
            ScoreDoc[] scoreDocs = hits.scoreDocs;

            return scoreDocs;

        } catch (IOException ex) {
            Logger.getLogger(IndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
