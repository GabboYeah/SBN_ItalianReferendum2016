/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.lucene.document.Document;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;

/**
 *
 * @author Gabriele
 */
public class prova {
    public static void main(String[] args) throws IOException{
        
        PoliticiansIndexManager pim = new PoliticiansIndexManager("index/AllPoliticiansIndex");
        pim.setReader("index/AllPoliticiansIndex");
        ArrayList<Document> docs = pim.searchForField("vote", "no", 10000000);
        ArrayList<String> relDocs = pim.getFieldValuesList(docs, "name");
        HashSet<String> set = new HashSet<String>();
        set.addAll(relDocs);
        System.err.println(relDocs.size() + " " + set.size());
        for(String name : set)
            relDocs.remove(name);
        
        System.out.println(relDocs);
    }
    
}
