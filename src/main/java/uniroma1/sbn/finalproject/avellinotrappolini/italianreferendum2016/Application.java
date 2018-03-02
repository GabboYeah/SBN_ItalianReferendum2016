/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

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

        TweetsIndexManager timYes = new TweetsIndexManager("index/AllTweetsIndex", "index/AllPoliticiansIndex", "index/AllYesTweetsIndex");
        dir = Paths.get("index/AllYesTweetsIndex");
        if (!Files.exists(dir)) {
            timYes.create("vote", "si");
        } else {
            System.out.println(dir.toString() + ": Index already created!");
        }

        TweetsIndexManager timNo = new TweetsIndexManager("index/AllTweetsIndex", "index/AllPoliticiansIndex", "index/AllNoTweetsIndex");
        dir = Paths.get("index/AllNoTweetsIndex");
        if (!Files.exists(dir)) {
            timNo.create("vote", "no");
        } else {
            System.out.println(dir.toString() + ": Index already created!");
        }

        int sizeYes = timYes.getIndexSizes();
        int sizeNo = timNo.getIndexSizes();
        System.out.println("");
        System.out.println("YES TWEETS: " + sizeYes);
        System.out.println("NO TWEETS: " + sizeNo);
        System.out.println("TOT TWEETS: " + (sizeYes + sizeNo));
    }
}
