/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Factory;

import it.stilo.g.structures.WeightedUndirectedGraph;
import java.io.IOException;
import static java.lang.Float.max;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.seninp.jmotif.sax.SAXException;
import org.apache.lucene.queries.function.valuesource.TermFreqValueSource;
import org.apache.lucene.queryparser.classic.ParseException;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools.Kmeans;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetWord;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.ClusterGraph;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TermFreqIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class ClusterGraphFactory {

    private int nCluster;
    private int nIter;

    public ClusterGraphFactory(int nCluster, int nIter) {
        this.nCluster = nCluster;
        this.nIter = nIter;
    }

    public ArrayList<ClusterGraph> generate(TermFreqIndexBuilder tfib) {
        try {

            ArrayList<ClusterGraph> cgs = new ArrayList<ClusterGraph>();

            tfib.build();
            ArrayList<TweetWord> relWords = tfib.getRelWords();

            int[] membership = Kmeans.computeKmeans(relWords, nCluster, nIter);
            
            for (int i = 0; i < 10; i++) {
                System.out.println("+Cluster N°" + (i + 1) + ":");
                int k = 0;
                for (int j = 0; j < membership.length; j++) {
                    if (membership[j] == i) {
                        k++;
                    }
                }
                System.out.println("+-+ Nmuber of elements: " + k + "\n");
            }

            for (int k = 0; k < nCluster; k++) {
                ArrayList<TweetWord> clusterWords = new ArrayList<TweetWord>();

                for (int idx = 0; idx < membership.length; idx++) {
                    if (membership[idx] == k) {
                        clusterWords.add(relWords.get(idx));
                    }
                }

                WeightedUndirectedGraph g = new WeightedUndirectedGraph(clusterWords.size());

                for (int i = 0; i < clusterWords.size(); i++) {
                    String u = clusterWords.get(i).getWord();
                    ArrayList uPost = tfib.getPostingList(u);
                    double uSize = uPost.size();

                    for (int j = i + 1; j < clusterWords.size(); j++) {
                        String v = clusterWords.get(j).getWord();
                        ArrayList vPost = tfib.getPostingList(v);
                        double vSize = vPost.size();

                        int p = 0, q = 0;
                        double intersection = 0;

                        while (p < uSize && q < vSize) {
                            if ((int) uPost.get(p) == (int) vPost.get(q)) {
                                p++;
                                q++;
                                intersection++;
                            } else if ((int) uPost.get(p) < (int) vPost.get(q)) {
                                p++;
                            } else {
                                q++;
                            }
                        }

                        double div1 = intersection / uSize;
                        double div2 = intersection / vSize;
                        float maxRelFreq = max((float) div1, (float) div2);

                        if (maxRelFreq > 0.0001) {
                            g.add(i, j, 1); // NB: lo stiamo facendo non pesato
                        }
                    }
                }
                
                ArrayList<String> labels = new ArrayList<String>();
                
                for(TweetWord tw : clusterWords)
                    labels.add(tw.getWord());
                
                cgs.add(new ClusterGraph(g, labels));
            }
            
            return cgs;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (TwitterException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
}
