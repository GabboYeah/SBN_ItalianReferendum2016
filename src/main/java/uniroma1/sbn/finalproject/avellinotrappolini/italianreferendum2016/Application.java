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
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.KppNeg;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.Core;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.GraphReader;
import it.stilo.g.util.NodesMapper;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Float.max;
import static java.lang.Math.PI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.apache.commons.lang3.ArrayUtils;
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
import org.math.plot.plotObjects.BaseLabel;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools.ComunityLPA;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools.Kmeans;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools.PlotTool;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetTerm;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.ClusterGraph;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Factory.ClusterGraphFactory;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.SupportersIndexManager;

/**
 *
 * @author Gabriele Avellino
 * @author Giovanni Trappolini
 */
public class Application {

    public static void main(String[] args) {
//        if (!Files.exists(Paths.get("output/relWords.json"))
//                || !Files.exists(Paths.get("output/relComps.json"))
//                || !Files.exists(Paths.get("output/relCores.json"))) {
        temporalAnalysis();
//        }
        if (!Files.exists(Paths.get("output/yesAuthorities.txt"))
                || !Files.exists(Paths.get("output/noAuthorities.txt"))
                || !Files.exists(Paths.get("output/yesHubs.txt"))
                || !Files.exists(Paths.get("output/noHubs.txt"))
                || !Files.exists(Paths.get("output/yesBrokers.txt"))
                || !Files.exists(Paths.get("output/noBrokers.txt"))) {
            part1();
        }

        part2();
    }

    public static void temporalAnalysis() {
        //Generate the indices needed in firt task of part 0
        indexCreation();
        System.out.println("Index Created");
        // Create a TweetsIndexManager for yes tweets
        TweetsIndexManager yesTim = new TweetsIndexManager("index/AllYesTweetsIndex");
        // Create a TweetsIndexManager for no tweets
        TweetsIndexManager noTim = new TweetsIndexManager("index/AllNoTweetsIndex");
        
        // Define a time interval for SAX procedure (12h)
        long timeInterval = 43200000L;
        // Define the regex to be match that shows a pattern of collective attention
        String regex = "a+b+a*b*a*";
        // Relevant field in which search relevant words
        String[] fieldNames = {"tweetText", "hashtags"};
        // Get Yes and No relevant words
        ArrayList<TweetTerm> yesList = yesTim.getRelFieldTerms(fieldNames, regex, timeInterval);
        ArrayList<TweetTerm> noList = noTim.getRelFieldTerms(fieldNames, regex, timeInterval);
        
        // N° of cluster in witch divide the words found
        int nCluster = 10;
        // max number of iteration for k-means
        int nIter = 1000;

        // Initialize a ClusterGraphFactory in order to create graphs generated by k-means
        // Just created, these graph compute their cc and cores storing them in attributes.
        ClusterGraphFactory cgf = new ClusterGraphFactory(nCluster, nIter);

        // Yes graphs generated by the factory
        ArrayList<ClusterGraph> yesGraphs = cgf.generate(yesList, yesTim);
        // No graphs generated by the factory
        ArrayList<ClusterGraph> noGraphs = cgf.generate(noList, noTim);

        // Set the time interval to 3 hours
        timeInterval = 3600000L;

        // List of all the words of yes and no
        ArrayList<String> representativeYesWordsList = new ArrayList<String>();
        ArrayList<String> representativeNoWordsList = new ArrayList<String>();
        
        // Initialize a map in which put all relevant words for keys "yes" and "no".
        HashMap<String, ArrayList<String>> relWords = new HashMap<String, ArrayList<String>>();
        // Initialize a map in which put all relevant componens for keys "yes" and "no". Each key has a list of 10 elements as value
        HashMap<String, ArrayList<ArrayList<String>>> relComps = new HashMap<String, ArrayList<ArrayList<String>>>();
        // Initialize a map in which put all relevant Cores for keys "yes" and "no". Each key has a list of 10 elements as value
        HashMap<String, ArrayList<ArrayList<String>>> relCores = new HashMap<String, ArrayList<ArrayList<String>>>();
        
        // Initialize values for the two maps already created
        relComps.put("yes", new ArrayList<ArrayList<String>>());
        relComps.put("no", new ArrayList<ArrayList<String>>());
        relCores.put("yes", new ArrayList<ArrayList<String>>());
        relCores.put("no", new ArrayList<ArrayList<String>>());
        
        // For each yes cluster...
        for (ClusterGraph cg : yesGraphs) {
            
            // ...Get the core elements and save them in coreList
            int[] core = cg.getCore().seq;
            ArrayList<Integer> coreList = new ArrayList<Integer>();
            for (int k = 0; k < core.length; k++) {
                coreList.add(core[k]);
            }
            
            // Get all the labels of the words in the core and save them in relCores
            relCores.get("yes").add(cg.getWords(coreList));
            
            // Get cluster comps
            Set<Set<Integer>> comps = cg.getComps();
            // For each comp
            for (Set<Integer> comp : comps) {
                // Get all the elements of the comp
                ArrayList<Integer> compElems = new ArrayList<Integer>();
                for (int elem : comp) {
                    compElems.add(elem);
                }
                
                // Get all the labels of the words in the comp elements and save them in relComps
                relComps.get("yes").add(cg.getWords(compElems));
                
                // Add all the words found in the list of the yes words
                for (String word : cg.getWords(compElems)) {
                    representativeYesWordsList.add(word);
                }
            }
        }
        
        // same for no graphs
        for (ClusterGraph cg : noGraphs) {
            int[] core = cg.getCore().seq;
            ArrayList<Integer> coreList = new ArrayList<Integer>();
            for (int k = 0; k < core.length; k++) {
                coreList.add(core[k]);
            }

            relCores.get("no").add(cg.getWords(coreList));

            Set<Set<Integer>> comps = cg.getComps();
            ArrayList<Integer> compElems = new ArrayList<Integer>();
            for (Set<Integer> comp : comps) {
                compElems = new ArrayList<Integer>();
                for (int elem : comp) {
                    compElems.add(elem);
                    String nodeName = cg.nodeMapper.getNode(elem);
//                    if (nodeName.startsWith("#")) {
//                        System.out.println(nodeName + ": " + Arrays.toString(noTim.getTermTimeSeries(nodeName, "hashtags", timeInterval)));
//                    } else {
//                        System.out.println(nodeName + ": " + Arrays.toString(noTim.getTermTimeSeries(nodeName, "tweetText", timeInterval)));
//                    }
                }
                relComps.get("no").add(cg.getWords(compElems));

                // For each no word found
                for (String word : cg.getWords(compElems)) {
                    // If a word is already in the list of yes words
                    if (representativeYesWordsList.contains(word)) {
                        // Remove it from the Yes list and skip the adding
                        int flag = representativeYesWordsList.indexOf(word);
                        representativeYesWordsList.remove(flag);
                    } else {
                        // add it to the no list
                        representativeNoWordsList.add(word);
                    }
                }
            }
        }
        
        // Add Words to the words map
        relWords.put("yes", representativeYesWordsList);
        relWords.put("no", representativeNoWordsList);

        // Save maps obtained in json
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File("output/relWords.json"), relWords);
            mapper.writeValue(new File("output/relComps.json"), relComps);
            mapper.writeValue(new File("output/relCores.json"), relCores);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void indexCreation() {
        // Initialize a TweetsIndexManager for the index of all tweets
        TweetsIndexManager tim = new TweetsIndexManager("index/AllTweetsIndex");

        // If the index of all tweets doesn't exist
        Path dir = Paths.get("index/AllTweetsIndex");
        if (!Files.exists(dir)) {
            // Create it
            tim.create("input/stream");
        } else {
            // Advise the index already exist
            System.out.println(dir.toString() + ": Index already created!");
        }

        // Initialize a PoliticiansIndexManager for the index of all Politicians found
        PoliticiansIndexManager pim = new PoliticiansIndexManager("index/AllPoliticiansIndex");
        // If the index of all politicians doesn't exist
        dir = Paths.get("index/AllPoliticiansIndex");
        if (!Files.exists(dir)) {
            // Create it
            pim.create("input/politicians.csv");
        } else {
            // Advise the index already exist
            System.out.println(dir.toString() + ": Index already created!");
        }

        // Divide politicians in YES and NO
        ArrayList<Document> yesPoliticians = pim.searchForField("vote", "si", 100000);
        ArrayList<Document> noPoliticians = pim.searchForField("vote", "no", 100000);

        // Show how many politicians we got
        if (yesPoliticians != null && noPoliticians != null) {
            System.out.println("YES POLITICIANS: " + yesPoliticians.size());
            System.out.println("NO POLITICIANS: " + noPoliticians.size());
            System.out.println("TOT POLITICIANS: " + (yesPoliticians.size() + noPoliticians.size()));
        }

        // Initialize a TweetsIndexManager for the index of all yes tweets based on yes pols
        TweetsIndexManager yesTim = new TweetsIndexManager("index/AllYesTweetsIndex");

        // If the index of all yes tweets doesn't exist
        dir = Paths.get("index/AllYesTweetsIndex");
        if (!Files.exists(dir)) {
            // Create it collecting all the yes ploticians screen name
            ArrayList<String> yesScreenNames = pim.searchFilteredValueField("vote", "si", "screenName", 10000);
            yesTim.create("index/AllTweetsIndex", "screenName", yesScreenNames);
        } else {
            // Advise the index already exist
            System.out.println(dir.toString() + ": Index already created!");
        }

        // Initialize a TweetsIndexManager for the index of all no tweets based on no pols
        TweetsIndexManager noTim = new TweetsIndexManager("index/AllNoTweetsIndex");

        // If the index of all no tweets doesn't exist
        dir = Paths.get("index/AllNoTweetsIndex");
        if (!Files.exists(dir)) {
            // Create it collecting all the no ploticians screen name
            ArrayList<String> noScreenNames = pim.searchFilteredValueField("vote", "no", "screenName", 10000);
            noTim.create("index/AllTweetsIndex", "screenName", noScreenNames);
        } else {
            // Advise the index already exist
            System.out.println(dir.toString() + ": Index already created!");
        }

        // Get all tweets of interest(YES and NO tweets related to our pols)
        int yesSize = yesTim.getIndexSizes();
        int noSize = noTim.getIndexSizes();

        // And print the sizes
        System.out.println("");
        System.out.println("YES TWEETS: " + yesSize);
        System.out.println("NO TWEETS: " + noSize);
        System.out.println("TOT TWEETS: " + (yesSize + noSize));

        // Set stepsize to one hour
        long stepSize = 3600000L;

        // Get yes and no tweets distro over our stepsize
        ArrayList<long[]> yesDistro = yesTim.getTweetDistro(stepSize);
        ArrayList<long[]> noDistro = noTim.getTweetDistro(stepSize);

        // Create a PlotTool class
        PlotTool plot = new PlotTool();

        // Generate coordinates for plot
        double[] x1 = new double[yesDistro.get(1).length];
        double[] y1 = new double[yesDistro.get(1).length];
        int i;

        // Rescale tweets frequency data
        for (i = 0; i < yesDistro.get(1).length; i++) {
            x1[i] = i + 1;
            y1[i] = Math.log(1 + yesDistro.get(1)[i]);
        }

        double[] x2 = new double[yesDistro.get(1).length];
        double[] y2 = new double[yesDistro.get(1).length];

        // Rescale tweets frequency data
        for (i = 0; i < noDistro.get(1).length; i++) {
            x2[i] = i + 1;
            y2[i] = Math.log(1 + noDistro.get(1)[i]);
        }

        // Create plots
        plot.createPlot("Yes", x1, y1, "No", x2, y2, "Tweets Distribution", "Time", "Frequency");
        plot.setBounds(0, 0D, 242D);
        plot.setBounds(1, 0D, 5.5D);
        plot.getPlot(1200, 600);
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

            ArrayList<String> nodes = sim.getFieldValuesList(sim.getAllDocs(), "id");

            int worker = (int) (Runtime.getRuntime().availableProcessors());
            WeightedDirectedGraph ccsg;
            NodesMapper<String> nodeMapper;

            dir = Paths.get("output/ccsg.txt");
            if (!Files.exists(dir)) {
                String sourcePath = "input/Official_SBN-ITA-2016-Net.gz";

                FileInputStream fstream = new FileInputStream(sourcePath);
                GZIPInputStream gzstream = new GZIPInputStream(fstream);
                InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
                BufferedReader br = new BufferedReader(isr);

                String line;
                nodeMapper = new NodesMapper<String>();

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

                int i = 0;
                for (String node : nodes) {
                    if (nodeMapper.getId(node) < 450193) {
                        ids[i] = nodeMapper.getId(node);
                        i++;
                    }
                }
                ids = Arrays.copyOf(ids, i);

                WeightedDirectedGraph sg = SubGraph.extract(g, ids, worker);

                System.out.println(ids.length + " " + g.size + " " + sg.size);

                Set<Set<Integer>> comps;
                comps = ConnectedComponents.rootedConnectedComponents(sg, ids, worker);

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

                Integer[] maxElemArray = maxElem.toArray(new Integer[maxElem.size()]);
                int[] ccids = new int[maxElemArray.length];

                for (i = 0; i < maxElemArray.length; i++) {
                    ccids[i] = maxElemArray[i].intValue();
                }

                ccsg = SubGraph.extract(sg, ccids, worker);

                FileWriter fileWriter = new FileWriter("output/ccsg.txt");
                PrintWriter printWriter = new PrintWriter(fileWriter);

                for (i = 0; i < ccsg.out.length; i++) {
                    if (ccsg.out[i] != null) {
                        for (int j = 0; j < ccsg.out[i].length; j++) {
                            printWriter.print(nodeMapper.getNode(i) + " " + nodeMapper.getNode(ccsg.out[i][j]) + " 1\n");
                        }
                    }
                }
                printWriter.close();
            } else {

                System.out.println(dir.toString() + ": Index already created!");
                FileReader fr = new FileReader("output/ccsg.txt");
                BufferedReader br = new BufferedReader(fr);

                ccsg = new WeightedDirectedGraph(46648 + 1);
                nodeMapper = new NodesMapper<String>();

                String line;

                while ((line = br.readLine()) != null) {
                    String[] splittedLine = line.split(" ");
                    ccsg.add(nodeMapper.getId(splittedLine[0]), nodeMapper.getId(splittedLine[1]), Integer.parseInt(splittedLine[2]));
                }
            }

            ArrayList<ArrayList<DoubleValues>> hitsResult = HubnessAuthority.compute(ccsg, 0.00001, worker);
            ArrayList<DoubleValues> authorities = hitsResult.get(0);

            FileWriter fileWriter = new FileWriter("output/authorities.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);

            for (DoubleValues authority : authorities) {
                printWriter.print(authority.index + " " + authority.value + "\n");
            }
            printWriter.close();

            ArrayList<String> yesAuthorities = new ArrayList<>();
            ArrayList<String> noAuthorities = new ArrayList<>();
            ArrayList<String> unclassifiedAuthorities = new ArrayList<>();

            for (int i = 0; i < (1000 < authorities.size() ? 1000 : authorities.size()); i++) {
                Document supporter = sim.searchForField("id", nodeMapper.getNode(authorities.get(i).index), 10).get(0);
                if (Integer.parseInt(supporter.get("isAYesPol")) == 1) {
                    yesAuthorities.add(supporter.get("id"));
                } else if (Integer.parseInt(supporter.get("isANoPol")) == 1) {
                    noAuthorities.add(supporter.get("id"));
                } else {
                    float yesScore = (float) (Integer.parseInt(supporter.get("yesPolsMentioned"))
                            + 0.5 * Integer.parseInt(supporter.get("yesConstructionsUsed"))
                            + 3 * Integer.parseInt(supporter.get("yesExpressionsUsed")));

                    float noScore = (float) (Integer.parseInt(supporter.get("noPolsMentioned"))
                            + 0.5 * Integer.parseInt(supporter.get("noConstructionsUsed"))
                            + 3 * Integer.parseInt(supporter.get("noExpressionsUsed")));

                    float finalScore = yesScore / noScore;
                    if (finalScore > 1.45) {
                        yesAuthorities.add(supporter.get("id"));
                    } else if (finalScore < 0.7) {
                        noAuthorities.add(supporter.get("id"));
                    } else {
                        unclassifiedAuthorities.add(supporter.get("id"));
                    }
                }
            }

            System.out.println("YES AUTHORITIES: " + yesAuthorities.size());
            System.out.println("NO AUTHORITIES: " + noAuthorities.size());
            System.out.println("UNCLASSIFIED AUTHORITIES: " + unclassifiedAuthorities.size());

            fileWriter = new FileWriter("output/yesAuthorities.txt");
            printWriter = new PrintWriter(fileWriter);

            for (String authority : yesAuthorities) {
                printWriter.print(authority + " " + nodeMapper.getId(authority) + "\n");
            }
            printWriter.close();

            fileWriter = new FileWriter("output/noAuthorities.txt");
            printWriter = new PrintWriter(fileWriter);

            for (String authority : noAuthorities) {
                printWriter.print(authority + " " + nodeMapper.getId(authority) + "\n");
            }
            printWriter.close();

            fileWriter = new FileWriter("output/unclassifiedAuthorities.txt");
            printWriter = new PrintWriter(fileWriter);

            for (String authority : unclassifiedAuthorities) {
                printWriter.print(authority + " " + nodeMapper.getId(authority) + "\n");
            }
            printWriter.close();

            ArrayList<String> yesHubs = new ArrayList<>();
            ArrayList<String> noHubs = new ArrayList<>();

            ArrayList<DoubleValues> hubs = hitsResult.get(1);

            fileWriter = new FileWriter("output/hubs.txt");
            printWriter = new PrintWriter(fileWriter);

            for (DoubleValues hub : hubs) {
                printWriter.print(hub.index + " " + hub.value + "\n");
            }
            printWriter.close();

            for (int i = 0; i < hubs.size(); i++) {
                Document supporter = sim.searchForField("id", nodeMapper.getNode(hubs.get(i).index), 10).get(0);
                if (Integer.parseInt(supporter.get("isAYesPol")) == 1) {
                    yesHubs.add(supporter.get("id"));
                } else if (Integer.parseInt(supporter.get("isANoPol")) == 1) {
                    noHubs.add(supporter.get("id"));
                } else {
                    float yesScore = (float) (Integer.parseInt(supporter.get("yesPolsMentioned"))
                            + 0.5 * Integer.parseInt(supporter.get("yesConstructionsUsed"))
                            + 3 * Integer.parseInt(supporter.get("yesExpressionsUsed")));

                    float noScore = (float) (Integer.parseInt(supporter.get("noPolsMentioned"))
                            + 0.5 * Integer.parseInt(supporter.get("noConstructionsUsed"))
                            + 3 * Integer.parseInt(supporter.get("noExpressionsUsed")));

                    float finalScore = yesScore / noScore;
                    if (finalScore > 1.45 && (yesScore + noScore) == 8) {
                        yesHubs.add(supporter.get("id"));
                    } else if (finalScore < 0.7 && (yesScore + noScore) == 8) {
                        noHubs.add(supporter.get("id"));
                    }
                }
                if ((yesHubs.size() >= 500) && (noHubs.size() >= 500)) {
                    break;
                }
            }

            List<String> yesHubsList = yesHubs.subList(0, (500 < yesHubs.size() ? 500 : yesHubs.size()));
            List<String> noHubsList = noHubs.subList(0, (500 < noHubs.size() ? 500 : noHubs.size()));

            fileWriter = new FileWriter("output/yesHubs.txt");
            printWriter = new PrintWriter(fileWriter);

            for (String hub : yesHubs) {
                printWriter.print(hub + " " + nodeMapper.getId(hub) + "\n");
            }
            printWriter.close();

            fileWriter = new FileWriter("output/noHubs.txt");
            printWriter = new PrintWriter(fileWriter);

            for (String hub : noHubs) {
                printWriter.print(hub + " " + nodeMapper.getId(hub) + "\n");
            }
            printWriter.close();

            System.out.println();
            System.out.println("YES HUBS: " + yesHubsList.size());
            System.out.println("NO HUBS: " + noHubsList.size());

//            fileWriter = new FileWriter("output/yesBrokersCorretto.txt");
//            printWriter = new PrintWriter(fileWriter);
//            FileReader fr = new FileReader("output/yesBrokers.txt");
//            BufferedReader br = new BufferedReader(fr);
//
//            String line;
//
//            while ((line = br.readLine()) != null) {
//                String broker = line;
//                printWriter.print(broker + " " + nodeMapper.getId(broker) + "\n");
//            }
//            printWriter.close();
//            br.close();
//
//            fileWriter = new FileWriter("output/noBrokersCorretto.txt");
//            printWriter = new PrintWriter(fileWriter);
//            fr = new FileReader("output/noBrokers.txt");
//            br = new BufferedReader(fr);
//
//            while ((line = br.readLine()) != null) {
//                String broker = line;
//                printWriter.print(broker + " " + nodeMapper.getId(broker) + "\n");
//            }
//            printWriter.close();
//            br.close();
            System.out.println("STOPPAMI!");

//            int[] degreeInDistribution = new int[ccsg.size];
//            int[] degreeOutDistribution = new int[ccsg.size];
//            int[] degreeSumDistribution = new int[ccsg.size];
//            for (int i = 0; i < ccsg.size; i++) {
//                if (ccsg.out[i] != null) {
//                    degreeOutDistribution[i] = ccsg.out[i].length;
//                } else {
//                    degreeOutDistribution[i] = 0;
//                }
//                if (ccsg.in[i] != null) {
//                    degreeInDistribution[i] = ccsg.in[i].length;
//                } else {
//                    degreeInDistribution[i] = 0;
//                }
//                degreeSumDistribution[i] = degreeInDistribution[i] + degreeOutDistribution[i];
//            }
//
//            Arrays.sort(degreeInDistribution);
//            Arrays.sort(degreeOutDistribution);
//            Arrays.sort(degreeSumDistribution);
//
//            System.out.println("IN DEGREE 1%:  " + degreeInDistribution[(int) ccsg.size / 100]);
//            System.out.println("OUT DEGREE 1%: " + degreeOutDistribution[(int) ccsg.size / 100]);
//            System.out.println("SUM DEGREE 1%: " + degreeSumDistribution[(int) ccsg.size / 100]);
//
//            System.out.println("-------------------------------------------------------------------------");
//
//            System.out.println("IN DEGREE 10%:  " + degreeInDistribution[(int) ccsg.size / 10]);
//            System.out.println("OUT DEGREE 10%: " + degreeOutDistribution[(int) ccsg.size / 10]);
//            System.out.println("SUM DEGREE 10%: " + degreeSumDistribution[(int) ccsg.size / 10]);
//
//            System.out.println("-------------------------------------------------------------------------");
//
//            System.out.println("IN DEGREE 25%:  " + degreeInDistribution[(int) ccsg.size / 4]);
//            System.out.println("OUT DEGREE 25%: " + degreeOutDistribution[(int) ccsg.size / 4]);
//            System.out.println("SUM DEGREE 25%: " + degreeSumDistribution[(int) ccsg.size / 4]);
            ArrayList<Integer> nodeTresholdALst = new ArrayList<>();
            for (int i = 0; i < ccsg.size; i++) {
                if (ccsg.out[i] != null && ccsg.out[i].length > 28) {
                    if (ccsg.in[i] != null && ccsg.in[i].length > 35) {
                        nodeTresholdALst.add(i);
                    }
                }
            }

            float nodesRemoved = (float) (ccsg.size - nodeTresholdALst.size()) / ccsg.size;
            System.out.println("NODES REMOVED: " + nodesRemoved);
            int[] nodeTresholdLst = ArrayUtils.toPrimitive(nodeTresholdALst.toArray(new Integer[nodeTresholdALst.size()]));

            WeightedDirectedGraph gkpp = SubGraph.extract(ccsg, nodeTresholdLst, worker);

            List<DoubleValues> brokers = KppNeg.searchBroker(gkpp, nodeTresholdLst, worker);

            fileWriter = new FileWriter("output/brokers.txt");
            printWriter = new PrintWriter(fileWriter);

            for (DoubleValues broker : brokers) {
                printWriter.print(broker.index + " " + broker.value + "\n");
            }
            printWriter.close();

            ArrayList<String> yesBrokers = new ArrayList<>();
            ArrayList<String> noBrokers = new ArrayList<>();

            for (int i = 0; i < brokers.size(); i++) {
                Document supporter = sim.searchForField("id", nodeMapper.getNode(brokers.get(i).index), 10).get(0);
                if (Integer.parseInt(supporter.get("isAYesPol")) == 1) {
                    yesBrokers.add(supporter.get("id"));
                } else if (Integer.parseInt(supporter.get("isANoPol")) == 1) {
                    noBrokers.add(supporter.get("id"));
                } else {
                    float yesScore = (float) (Integer.parseInt(supporter.get("yesPolsMentioned"))
                            + 0.5 * Integer.parseInt(supporter.get("yesConstructionsUsed"))
                            + 3 * Integer.parseInt(supporter.get("yesExpressionsUsed")));

                    float noScore = (float) (Integer.parseInt(supporter.get("noPolsMentioned"))
                            + 0.5 * Integer.parseInt(supporter.get("noConstructionsUsed"))
                            + 3 * Integer.parseInt(supporter.get("noExpressionsUsed")));

                    float finalScore = yesScore / noScore;
                    if (finalScore > 1.45 && (yesScore + noScore) == 8) {
                        yesBrokers.add(supporter.get("id"));
                    } else if (finalScore < 0.7 && (yesScore + noScore) == 8) {
                        noBrokers.add(supporter.get("id"));
                    }
                }
                if ((yesBrokers.size() >= 500) && (noBrokers.size() >= 500)) {
                    break;
                }
            }

            fileWriter = new FileWriter("output/yesBrokers.txt");
            printWriter = new PrintWriter(fileWriter);

            for (String broker : yesBrokers) {
                printWriter.print(broker + " " + nodeMapper.getId(broker) + "\n");
            }
            printWriter.close();

            fileWriter = new FileWriter("output/noBrokers.txt");
            printWriter = new PrintWriter(fileWriter);

            for (String broker : noBrokers) {
                printWriter.print(broker + " " + nodeMapper.getId(broker) + "\n");
            }
            printWriter.close();

            List<String> yesBrokersList = yesBrokers.subList(0, (500 < yesBrokers.size() ? 500 : yesBrokers.size()));
            List<String> noBrokersList = noBrokers.subList(0, (500 < noBrokers.size() ? 500 : noBrokers.size()));

            System.out.println();
            System.out.println("YES Brokers: " + yesBrokersList.size());
            System.out.println("NO Brokers: " + noBrokersList.size());

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void part2() {
        String sourcePath = "input/Official_SBN-ITA-2016-Net.gz";

        FileInputStream fstream;
        try {
            fstream = new FileInputStream(sourcePath);
            GZIPInputStream gzstream = new GZIPInputStream(fstream);
            InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String line;
            NodesMapper<String> nodeMapper = new NodesMapper<String>();

            WeightedDirectedGraph g = new WeightedDirectedGraph(450193 + 1);

            while ((line = br.readLine()) != null) {
                String[] splittedLine = line.split("\t");
                g.add(nodeMapper.getId(splittedLine[0]), nodeMapper.getId(splittedLine[1]), Integer.parseInt(splittedLine[2]));
            }

            int worker = (int) (Runtime.getRuntime().availableProcessors());

            int[] initLabels = getInitLabel("output/yesAuthorities.txt", "output/noAuthorities.txt", g);
            int[] labelsAuthorities = ComunityLPA.compute(g, .99d, worker, initLabels);

            int yes = 0, no = 0, unclassified = 0;

            for (int label : labelsAuthorities) {
                switch (label) {
                    case 1:
                        yes++;
                        break;
                    case 2:
                        no++;
                        break;
                    default:
                        unclassified++;
                        break;
                }
            }

            System.out.println("+++ AUTHORITIES +++");
            System.out.println("YES: " + yes + ", NO: " + no + ", UNCLASSIFIED: " + unclassified);

            initLabels = getInitLabel("output/yesHubs.txt", "output/noHubs.txt", g);
            int[] labelsHubs = ComunityLPA.compute(g, .99d, worker, initLabels);

            yes = 0;
            no = 0;
            unclassified = 0;

            for (int label : labelsHubs) {
                switch (label) {
                    case 1:
                        yes++;
                        break;
                    case 2:
                        no++;
                        break;
                    default:
                        unclassified++;
                        break;
                }
            }

            System.out.println("+++ HUBS +++");
            System.out.println("YES: " + yes + ", NO: " + no + ", UNCLASSIFIED: " + unclassified);

            initLabels = getInitLabel("output/yesBrokers.txt", "output/noBrokers.txt", g);
            int[] labelBrokers = ComunityLPA.compute(g, .99d, worker, initLabels);

            yes = 0;
            no = 0;
            unclassified = 0;

            for (int label : labelBrokers) {
                switch (label) {
                    case 1:
                        yes++;
                        break;
                    case 2:
                        no++;
                        break;
                    default:
                        unclassified++;
                        break;
                }
            }

            System.out.println("+++ BROKERS +++");
            System.out.println("YES: " + yes + ", NO: " + no + ", UNCLASSIFIED: " + unclassified);

            FileWriter fileWriter = new FileWriter("output/LPA.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);

            for (int i = 0; i < g.size; i++) {
                printWriter.print(i + " " + labelsAuthorities[i] + " " + labelsHubs[i] + " " + labelBrokers[i] + "\n");
            }
            printWriter.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int[] getInitLabel(String yesPath, String noPath, WeightedDirectedGraph g) {
        int[] initLabel = new int[g.size];

        FileReader fr;
        try {
            fr = new FileReader(yesPath);
            BufferedReader br = new BufferedReader(fr);

            String line;

            while ((line = br.readLine()) != null) {
                int id = Integer.parseInt(line.split(" ")[1]);
                initLabel[id] = 1;
            }
            br.close();

            fr = new FileReader(noPath);
            br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                int id = Integer.parseInt(line.split(" ")[1]);
                initLabel[id] = 2;
            }
            br.close();

            return initLabel;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
