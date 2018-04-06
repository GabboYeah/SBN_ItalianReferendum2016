/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016;

import it.stilo.g.algo.PageRankPI;
import it.stilo.g.algo.PageRankRW;
import it.stilo.g.example.ZacharyExample;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.MemInfo;
import it.stilo.g.util.ZacharyNetwork;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools.ComunityLPA;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.CSVReader;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class prova {

    protected static final Logger logger = LogManager.getLogger(ZacharyExample.class);

    public static void main(String[] args) throws IOException {

        TweetsIndexManager tim = new TweetsIndexManager("index/AllTweetsIndex");

        tim.setReader("index/AllTweetsIndex");

//        CSVReader readerNo = new CSVReader(",", "input/no_p.txt");
//        CSVReader readerYes = new CSVReader(",", "input/yes_p.txt");
//
//        ArrayList<String[]> listNo = readerNo.readCSV();
//        ArrayList<String[]> listYes = readerYes.readCSV();
//
//        for (String[] lineYes : listYes) {
//            for (String screenNameYes : lineYes) {
//
//                ScoreDoc[] x = tim.searchTermInAField(screenNameYes, "screenName");
//                if (x.length > 500) {
//                    System.out.println("NAME: " + screenNameYes + " # OF TWEETS: " + x.length);
//
////                    for (ScoreDoc y : x) {
////                        System.out.println(tim.ir.document(y.doc).get("name"));
////                        System.out.println(tim.ir.document(y.doc).get("tweetText"));
////                        System.out.println(tim.ir.document(y.doc).get("hashtags"));
////                        System.out.println("------------------");
////                    }
//                }
//
//            }
//
//        }

        ScoreDoc[] x = tim.searchTermInAField("htt", "tweetText");
        for (ScoreDoc y : x) {
            System.out.println(tim.ir.document(y.doc).get("screenName"));
            System.out.println(tim.ir.document(y.doc).get("tweetText"));
            System.out.println(tim.ir.document(y.doc).get("hashtags"));
            System.out.println("------------------");
        }
//        TweetsIndexManager tim = new TweetsIndexManager("index/AllTweetsIndex");
//        tim.setReader("index/AllTweetsIndex");
//        ArrayList<Document> x = tim.searchForField("screenName", "salvoaranzulla", 1000000);
//        for(Document doc : x){
//            System.out.println(doc.get("tweetText"));
//            System.out.println(doc.get("hashtags"));
//            System.out.println(doc.get("mentioned"));
//            
//        }
//        PoliticiansIndexManager pim = new PoliticiansIndexManager("index/AllPoliticiansIndex");
//        pim.setReader("index/AllPoliticiansIndex");
//        ArrayList<Document> docs = pim.searchForField("vote", "no", 10000000);
//        ArrayList<String> relDocs = pim.getFieldValuesList(docs, "name");
//        HashSet<String> set = new HashSet<String>();
//        set.addAll(relDocs);
//        System.err.println(relDocs.size() + " " + set.size());
//        for (String name : set) {
//            relDocs.remove(name);
//        }
//
//        System.out.println(relDocs);
//        String sourcePath = "input/Official_SBN-ITA-2016-Net.gz";
//        
//        FileInputStream fstream = new FileInputStream(sourcePath);
//        GZIPInputStream gzstream = new GZIPInputStream(fstream);
//        InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
//        BufferedReader br = new BufferedReader(isr);
//
//        String line;
//        
//        NodesMapper<String> nodeMapper = new NodesMapper<String>();
//
//        WeightedUndirectedGraph g = new WeightedUndirectedGraph(5000000);
//
//        while ((line = br.readLine()) != null) {
//            String[] splittedLine = line.split("\t");
//            g.add(nodeMapper.getId(splittedLine[0]), nodeMapper.getId(splittedLine[1]), Integer.parseInt(splittedLine[2]));
//        }
    }
}
