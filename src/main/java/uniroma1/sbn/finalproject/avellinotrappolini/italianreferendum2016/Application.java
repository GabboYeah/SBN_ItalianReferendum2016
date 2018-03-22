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
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.Core;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.GraphReader;
import it.stilo.g.util.NodesMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Float.max;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import javax.swing.JFrame;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.math.plot.Plot2DPanel;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools.Kmeans;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetTerm;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.ClusterGraph;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Factory.ClusterGraphFactory;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.SupportersIndexManager;

/**
 *
 * @author Gabriele
 */
public class Application {

    public static void main(String[] args) {
        if (!Files.exists(Paths.get("output/relWords.json")) || !Files.exists(Paths.get("output/relComps.json")) || !Files.exists(Paths.get("output/relCores.json"))) {
            part0();
        }
        part1();
    }

    public static void part0() {

        TweetsIndexManager tim = new TweetsIndexManager("index/AllTweetsIndex");
        Path dir = Paths.get("index/AllTweetsIndex");
        if (!Files.exists(dir)) {
            tim.create("input/stream");
        } else {
            System.out.println(dir.toString() + ": Index already created!");
        }

        PoliticiansIndexManager pim = new PoliticiansIndexManager("index/AllPoliticiansIndex");
        dir = Paths.get("index/AllPoliticiansIndex");
        if (!Files.exists(dir)) {
            pim.create("input/politicians.csv");
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

        TweetsIndexManager yesTim = new TweetsIndexManager("index/AllYesTweetsIndex");
        dir = Paths.get("index/AllYesTweetsIndex");
        if (!Files.exists(dir)) {
            ArrayList<String> yesScreenNames = pim.searchFilteredValueField("vote", "si", "screenName", 10000);
            yesTim.create("index/AllTweetsIndex", "screenName", yesScreenNames);
        } else {
            System.out.println(dir.toString() + ": Index already created!");
        }

        TweetsIndexManager noTim = new TweetsIndexManager("index/AllNoTweetsIndex");
        dir = Paths.get("index/AllNoTweetsIndex");
        if (!Files.exists(dir)) {
            ArrayList<String> noScreenNames = pim.searchFilteredValueField("vote", "no", "screenName", 10000);
            noTim.create("index/AllTweetsIndex", "screenName", noScreenNames);
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
//        long stepSize = 86400000L;
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
        HashMap<String, ArrayList<String>> relWords = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<ArrayList<String>>> relComps = new HashMap<String, ArrayList<ArrayList<String>>>();
        HashMap<String, ArrayList<ArrayList<String>>> relCores = new HashMap<String, ArrayList<ArrayList<String>>>();
        relComps.put("yes", new ArrayList<ArrayList<String>>());
        relComps.put("no", new ArrayList<ArrayList<String>>());
        relCores.put("yes", new ArrayList<ArrayList<String>>());
        relCores.put("no", new ArrayList<ArrayList<String>>());

        long timeInterval = 43200000L;
        String regex = "a+b+a*b*a*";
        String[] fieldNames = {"tweetText", "hashtags"};
        ArrayList<TweetTerm> yesList = yesTim.getRelFieldTerms(fieldNames, regex, timeInterval);
        ArrayList<TweetTerm> noList = noTim.getRelFieldTerms(fieldNames, regex, timeInterval);

        int nCluster = 10;
        int nIter = 1000;

        ClusterGraphFactory cgf = new ClusterGraphFactory(nCluster, nIter);

        ArrayList<ClusterGraph> yesGraphs = cgf.generate(yesList, yesTim);
        ArrayList<ClusterGraph> noGraphs = cgf.generate(noList, noTim);

        int i = 1;
        timeInterval = 3600000L;

        ArrayList<String> representativeYesWordsList = new ArrayList<String>();
        ArrayList<String> representativeNoWordsList = new ArrayList<String>();
        for (ClusterGraph cg : yesGraphs) {
            System.out.println("Yes Cluster n. " + i + ": ");
            i++;
            System.out.println("---> Core:");
            int[] core = cg.getCore().seq;
            ArrayList<Integer> coreList = new ArrayList<Integer>();
            for (int k = 0; k < core.length; k++) {
                coreList.add(core[k]);
            }

            System.out.println(cg.getWords(coreList));
            relCores.get("yes").add(cg.getWords(coreList));

            System.out.println("---> Comps:");
            Set<Set<Integer>> comps = cg.getComps();
            for (Set<Integer> comp : comps) {
                ArrayList<Integer> compElems = new ArrayList<Integer>();
                System.out.println("+++ respective time series: +++");
                for (int elem : comp) {
                    compElems.add(elem);
                    String nodeName = cg.nodeMapper.getNode(elem);
                    if (nodeName.startsWith("#")) {
                        System.out.println(nodeName + "- tag: " + Arrays.toString(yesTim.getTermTimeSeries(nodeName, "hashtags", timeInterval)));
                    } else {
                        System.out.println(nodeName + "- text: " + Arrays.toString(yesTim.getTermTimeSeries(nodeName, "tweetText", timeInterval)));
                    }
                }

                System.out.println("comp = " + cg.getWords(compElems));
                relComps.get("yes").add(cg.getWords(compElems));

                for (String word : cg.getWords(compElems)) {
                    representativeYesWordsList.add(word);
                }
            }
        }

        i = 1;
        for (ClusterGraph cg : noGraphs) {
            System.out.println("No Cluster n. " + i + ": ");
            i++;
            System.out.println("---> Core:");
            int[] core = cg.getCore().seq;
            ArrayList<Integer> coreList = new ArrayList<Integer>();
            for (int k = 0; k < core.length; k++) {
                coreList.add(core[k]);
            }

            System.out.println(cg.getWords(coreList));
            relCores.get("no").add(cg.getWords(coreList));

            System.out.println("---> Comps:");
            Set<Set<Integer>> comps = cg.getComps();
            ArrayList<Integer> compElems = new ArrayList<Integer>();
            for (Set<Integer> comp : comps) {
                compElems = new ArrayList<Integer>();
                System.out.println("+++ respective time series: +++");
                for (int elem : comp) {
                    compElems.add(elem);
                    String nodeName = cg.nodeMapper.getNode(elem);
                    if (nodeName.startsWith("#")) {
                        System.out.println(nodeName + ": " + Arrays.toString(noTim.getTermTimeSeries(nodeName, "hashtags", timeInterval)));
                    } else {
                        System.out.println(nodeName + ": " + Arrays.toString(noTim.getTermTimeSeries(nodeName, "tweetText", timeInterval)));
                    }
                }
                System.out.println("comp = " + cg.getWords(compElems));
                relComps.get("no").add(cg.getWords(compElems));

                for (String word : cg.getWords(compElems)) {
                    if (representativeYesWordsList.contains(word)) {
                        int flag = representativeYesWordsList.indexOf(word);
                        representativeYesWordsList.remove(flag);
                    } else {
                        representativeNoWordsList.add(word);
                    }
                }
            }
            System.out.println("");
        }

        relWords.put("no", representativeNoWordsList);
        relWords.put("yes", representativeYesWordsList);

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File("output/relWords.json"), relWords);
            mapper.writeValue(new File("output/relComps.json"), relComps);
            mapper.writeValue(new File("output/relCores.json"), relCores);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void part1() {

        try {

            ArrayList<String> yesExp = new ArrayList<String>();
            ArrayList<String> noExp = new ArrayList<String>();

            yesExp.add("#iovotosi");
            yesExp.add("#iodicosi");

            noExp.add("#iovotono");
            noExp.add("#iodicono");

            SupportersIndexManager sim = new SupportersIndexManager("index/SupportersIndex", yesExp, noExp);
            Path dir = Paths.get("index/SupportersIndex");
            if (!Files.exists(dir)) {
                sim.create("output/relComps.json");
            } else {
                System.out.println(dir.toString() + ": Index already created!");
            }

            ArrayList<String> nodes = sim.getFieldValuesList(sim.getAllDocs(), "userId");

            String sourcePath = "input/Official_SBN-ITA-2016-Net.gz";

            FileInputStream fstream = new FileInputStream(sourcePath);
            GZIPInputStream gzstream = new GZIPInputStream(fstream);
            InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String line;
            NodesMapper<String> nodeMapper = new NodesMapper<String>();

//            HashSet<Integer> nodeIds = new HashSet<Integer>();
//
//            while ((line = br.readLine()) != null) {
//                String[] splittedLine = line.split("\t");
//                nodeIds.add(nodeMapper.getId(splittedLine[0]));
//                nodeIds.add(nodeMapper.getId(splittedLine[1]));
//            }
//            
//            System.out.println(nodeIds.size()); //450193
            WeightedDirectedGraph g = new WeightedDirectedGraph(450193 + 1);

            while ((line = br.readLine()) != null) {
                String[] splittedLine = line.split("\t");
                g.add(nodeMapper.getId(splittedLine[0]), nodeMapper.getId(splittedLine[1]), Integer.parseInt(splittedLine[2]));
            }

            int[] ids = new int[nodes.size()];
            ArrayList<Integer> ids2 = new ArrayList<Integer>();

            int i = 0;
            for (String node : nodes) {
                if (nodeMapper.getId(node) < 450193) {
                    ids[i] = nodeMapper.getId(node);
                    ids2.add(nodeMapper.getId(node));
                    i++;
                }
            }

            System.out.println("CIAO!!!");
            int worker = (int) (Runtime.getRuntime().availableProcessors());

            WeightedDirectedGraph sg = SubGraph.extract(g, ids, worker);

            System.out.println(sg.getVertex().length + " " + g.size);

            Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(sg, ids, worker);

            System.out.println("cc fatto.");

            int max = 0;
            Set<Integer> maxElem = new HashSet<Integer>();

            for (Set<Integer> comp : comps) {
                if (comp.size() > max) {
                    max = comp.size();
                    maxElem = comp;
                }
            }

            System.out.println(maxElem.size() + " " + Arrays.toString(maxElem.toArray(new Integer[maxElem.size()])));
            System.out.println(comps.size());

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
