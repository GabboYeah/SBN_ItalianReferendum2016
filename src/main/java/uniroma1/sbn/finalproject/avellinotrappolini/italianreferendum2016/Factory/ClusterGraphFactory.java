/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Factory;

import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.NodesMapper;
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
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetTerm;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.ClusterGraph;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class ClusterGraphFactory {

    private int nCluster;
    private int nIter;

    /**
     *
     * @param nCluster
     * @param nIter
     */
    public ClusterGraphFactory(int nCluster, int nIter) {
        this.nCluster = nCluster;
        this.nIter = nIter;
    }

    /**
     *
     * @param relWords
     * @param tim
     * @return
     */
    public ArrayList<ClusterGraph> generate(ArrayList<TweetTerm> relWords, TweetsIndexManager tim) {

        ArrayList<ClusterGraph> cgs = new ArrayList<ClusterGraph>();

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
            ArrayList<TweetTerm> clusterWords = new ArrayList<TweetTerm>();

            for (int idx = 0; idx < membership.length; idx++) {
                if (membership[idx] == k) {
                    clusterWords.add(relWords.get(idx));
                }
            }

            WeightedUndirectedGraph g = new WeightedUndirectedGraph(clusterWords.size() + 1);
            NodesMapper<String> nodeMapper = new NodesMapper<String>();

            for (int i = 1; i < clusterWords.size(); i++) {
                String u = clusterWords.get(i).getWord();
                nodeMapper.getId(u);
                String uType = clusterWords.get(i).getType();
                double uSize = clusterWords.get(i).getFrequency();

                for (int j = i + 1; j < clusterWords.size(); j++) {
                    String v = clusterWords.get(j).getWord();
                    String vType = clusterWords.get(j).getType();
                    double vSize = clusterWords.get(j).getFrequency();

                    double intersection = tim.searchTwoTermsInFields(u, uType, v, vType).length;

                    double div1 = intersection / uSize;
                    double div2 = intersection / vSize;
                    float maxRelFreq = max((float) div1, (float) div2);

                    if (maxRelFreq > 0.0001) {
                        g.add(i, j, 1); // NB: lo stiamo facendo non pesato
                    }
                }
            }

            cgs.add(new ClusterGraph(g, nodeMapper));
        }

        return cgs;
    }
}
