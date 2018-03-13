/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.CoreDecomposition;
import it.stilo.g.structures.Core;
import it.stilo.g.structures.WeightedUndirectedGraph;
import java.io.File;
import java.io.IOException;
import static java.lang.Float.max;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javax.swing.JFrame;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.math.plot.Plot2DPanel;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools.Kmeans;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetWord;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TermFreqIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class Application {

    public static void main(String[] args) {

        TweetsIndexManager tim = new TweetsIndexManager("input/stream", "index/AllTweetsIndex");
        Path dir = Paths.get("index/AllTweetsIndex");
        if (!Files.exists(dir)) {
            tim.create();
        } else {
            System.out.println(dir.toString() + ": Index already created!");
        }

        PoliticiansIndexManager pim = new PoliticiansIndexManager("input/politicians.csv", "index/AllPoliticiansIndex");
        dir = Paths.get("index/AllPoliticiansIndex");
        if (!Files.exists(dir)) {
            pim.create();
        } else {
            System.out.println(dir.toString() + ": Index already created!");
        }

        ArrayList<Document> yesPoliticians = pim.searchForField("vote", "si", 10000);
        ArrayList<Document> noPoliticians = pim.searchForField("vote", "no", 10000);

        if (yesPoliticians != null && noPoliticians != null) {
            System.out.println("YES POLITICIANS: " + yesPoliticians.size());
            System.out.println("NO POLITICIANS: " + noPoliticians.size());
            System.out.println("TOT POLITICIANS: " + (yesPoliticians.size() + noPoliticians.size()));
        }

        TweetsIndexManager yesTim = new TweetsIndexManager("index/AllTweetsIndex", "index/AllPoliticiansIndex", "index/AllYesTweetsIndex");
        dir = Paths.get("index/AllYesTweetsIndex");
        if (!Files.exists(dir)) {
            yesTim.create("vote", "si");
        } else {
            System.out.println(dir.toString() + ": Index already created!");
        }

        TweetsIndexManager noTim = new TweetsIndexManager("index/AllTweetsIndex", "index/AllPoliticiansIndex", "index/AllNoTweetsIndex");
        dir = Paths.get("index/AllNoTweetsIndex");
        if (!Files.exists(dir)) {
            noTim.create("vote", "no");
        } else {
            System.out.println(dir.toString() + ": Index already created!");
        }

        int yesSize = yesTim.getIndexSizes();
        int noSize = noTim.getIndexSizes();
        System.out.println("");
        System.out.println("YES TWEETS: " + yesSize);
        System.out.println("NO TWEETS: " + noSize);
        System.out.println("TOT TWEETS: " + (yesSize + noSize));

// ---> FARE BENE STO CAZZO DI GRAFICO <---
        //long stepSize = 86400000L;
//        long stepSize = 3600000L;
//        ArrayList<long[]> yesDistro = yesTim.getTweetDistro(stepSize);
//        ArrayList<long[]> noDistro = noTim.getTweetDistro(stepSize);
//        
//        double[] x = new double[yesDistro.get(1).length];
//        double[] y = new double[yesDistro.get(1).length];
//        int i;
//        for(i = 0; i < yesDistro.get(1).length; i++){
//            x[i] = i+1;
//            y[i] = Math.log(1 + yesDistro.get(1)[i]);
//        }
//        Plot2DPanel plot = new Plot2DPanel();
//
//        // add a line plot to the PlotPanel
//        plot.addLinePlot("Yes", x, y);
//        plot.addLegend("SOUTH");
//        
//        for(i = 0; i < noDistro.get(1).length; i++){
//            x[i] = i+1;
//            y[i] = Math.log(1 + noDistro.get(1)[i]);
//        }
//        
//        plot.addLinePlot("No", x, y);
//        
//        // put the PlotPanel in a JFrame, as a JPanel
//        JFrame frame = new JFrame("a plot panel");
//        frame.setContentPane(plot);
//        frame.setVisible(true);      
        TermFreqIndexBuilder yesTfib = new TermFreqIndexBuilder(43200000L, "index/AllYesTweetsIndex");
        TermFreqIndexBuilder noTfib = new TermFreqIndexBuilder(43200000L, "index/AllNoTweetsIndex");

        ArrayList<TweetWord> yesList;
        ArrayList<TweetWord> noList;

        try {
            yesTfib.build();
            noTfib.build();
            yesList = yesTfib.getRelWords();
            noList = noTfib.getRelWords();

            int nCluster = 10;
            int nIter = 1000;

            int[] yesMembership = Kmeans.computeKmeans(yesList, nCluster, nIter);
            int[] noMembership = Kmeans.computeKmeans(noList, nCluster, nIter);

//            for (int i = 0; i < 10; i++) {
//                System.out.println("+Cluster N°" + (i + 1) + ":");
//                int k = 0;
//                for (int j = 0; j < membership.length; j++) {
//                    if (membership[j] == i) {
//                        k++;
//                    }
//                }
//                System.out.println("+-+ Nmuber of elements: " + k + "\n");
//            }
            for (int k = 0; k < nCluster; k++) {
                ArrayList<TweetWord> clusterWords = new ArrayList<TweetWord>();

                for (int idx = 0; idx < membership.length; idx++) {
                    if (membership[idx] == k) {
                        clusterWords.add(yesList.get(idx));
                    }
                }
                
                WeightedUndirectedGraph g = new WeightedUndirectedGraph(clusterWords.size());
                
                for (int i = 0; i < clusterWords.size(); i++) {
                    String u = clusterWords.get(i).getWord();
                    ArrayList uPost = yesTfib.getPostingList(u);
                double uSize = uPost.size();
                }
            }

            

                
                

                for (int j = i + 1; j < clusterWords.size(); j++) {

                    String v = clusterWords.get(j).getWord();
                    ArrayList vPost = yesTfib.getPostingList(v);
                    double vSize = yesTfib.getPostingList(v).size();

                    int p = 0, q = 0;
                    double intersection = 0;
                    //System.out.println("--------");
                    while (p < uSize && q < vSize) {

                        //System.out.println((int) uPost.get(p) +" "+ (int) vPost.get(q));
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
                    //System.out.println(u + " " + v + " " + intersection + " " + uSize + " " + vSize + " " + maxRelFreq);

                    if (maxRelFreq > 0.0001) {
                        g.add(i, j, 1); // NB: lo stiamo facendo non pesato
                    }
                }

            }

            for (int i = 0; i < g.out.length; i++) {

                System.out.println(Arrays.toString(g.weights[i]));
            }

            int worker = (int) (Runtime.getRuntime().availableProcessors());

            int[] all = new int[g.size];
            for (int i = 0; i < g.size; i++) {
                all[i] = i;
            }
            Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(g, all, worker);

            for (Set cc : comps) {
                System.out.println(Arrays.toString(cc.toArray()));
            }

            Core c = CoreDecomposition.getInnerMostCore(g, worker);
            System.out.println(c.minDegree);
            System.out.println(c.seq.length);
            System.out.println(Arrays.toString(c.seq));

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("ERROR WITH RELEVANT TWEETS");
        } catch (TwitterException ex) {
            ex.printStackTrace();
            System.out.println("ERROR WITH RELEVANT TWEETS");
        } catch (ParseException ex) {
            ex.printStackTrace();
            System.out.println("ERROR WITH RELEVANT TWEETS");
        } catch (SAXException ex) {
            ex.printStackTrace();
            System.out.println("ERROR WITH RELEVANT TWEETS");
        } catch (InterruptedException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
