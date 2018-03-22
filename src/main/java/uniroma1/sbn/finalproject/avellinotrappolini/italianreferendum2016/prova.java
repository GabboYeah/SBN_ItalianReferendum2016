/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.NodesMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import org.apache.lucene.document.Document;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;

/**
 *
 * @author Gabriele
 */
public class prova {

    public static void main(String[] args) throws IOException {

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
        String sourcePath = "input/Official_SBN-ITA-2016-Net.gz";
        
        FileInputStream fstream = new FileInputStream(sourcePath);
        GZIPInputStream gzstream = new GZIPInputStream(fstream);
        InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
        BufferedReader br = new BufferedReader(isr);

        String line; 
        
        NodesMapper<String> nodeMapper = new NodesMapper<String>();

        WeightedUndirectedGraph g = new WeightedUndirectedGraph(5000000);

        while ((line = br.readLine()) != null) {
            String[] splittedLine = line.split("\t");
            g.add(nodeMapper.getId(splittedLine[0]), nodeMapper.getId(splittedLine[1]), Integer.parseInt(splittedLine[2]));
        }
    }
}
