/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.CSVReader;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class PoliticiansIndexBuilder extends IndexBuilder {

    //Politic Groups vote decision
    private HashMap<String, String> groupVote;

    //Document
    private Document politician;
    private StringField vote;
    private StringField name;
    private StringField screenName;

    public PoliticiansIndexBuilder(String sourcePath, String indexPath) {
        this.politician = new Document();
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.vote = new StringField("vote", "", Field.Store.YES);

        politician.add(name);
        politician.add(screenName);
        politician.add(vote);

        groupVote = new HashMap<String, String>();
        groupVote.put("AP", "si");
        groupVote.put("DS-CD", "si");
        groupVote.put("FI-PdL", "no");
        groupVote.put("FdI", "no");
        groupVote.put("Lega", "no");
        groupVote.put("M5S", "no");
        groupVote.put("PD", "si");
        groupVote.put("SCpI", "si");
        groupVote.put("SI-SEL", "no");
        groupVote.put("AL-A", "si");
        groupVote.put("Aut", "si");
        groupVote.put("CoR", "no");
        groupVote.put("GAL", "no");
        groupVote.put("Misto", "?");

        this.sourcePath = sourcePath;
        this.indexPath = indexPath;
    }

    @Override
    public void build() throws IOException {
        CSVReader csvr = new CSVReader(",", sourcePath);
        ArrayList<String[]> rows;

        setBuilderParams(indexPath);
        rows = csvr.readCSV();

        String id;
        String[] result;
        int followers;

        for (String[] row : rows) {
            String name = row[0];
            String surname = row[1];
            result = findUserTwitterId(name, surname);
            id = result[0];
            followers = Integer.parseInt(result[1]);
            System.out.println("Search for : " + name + " " + surname + ", followers: " + followers);
            if (!id.equals("") && followers >= 1000) {
                this.name.setStringValue((name + " " + surname).toLowerCase());
                this.screenName.setStringValue(id);

                System.out.println("");
                System.out.println(this.politician.get("name"));
                System.out.println(this.politician.get("screenName"));

                if (row[2].equals(groupVote.get(row[3]))) {
                    this.vote.setStringValue(row[2]);
                    this.writer.addDocument(this.politician);
                    System.out.println(this.politician.get("vote"));

                } else if (row[2].equals("si") || row[2].equals("no")) {
                    this.vote.setStringValue(row[2]);
                    this.writer.addDocument(this.politician);
                    System.out.println(this.politician.get("vote"));

                } else if (row[2].equals("-") && !row[3].equals("Misto")) {
                    this.vote.setStringValue(groupVote.get(row[3]));
                    this.writer.addDocument(this.politician);
                    System.out.println(this.politician.get("vote"));

                }
            }
            System.out.println("----------------");
        }
        this.writer.commit();
    }

    public String[] findUserTwitterId(String name, String surname) throws IOException {
        TweetsIndexManager tim = new TweetsIndexManager("stream", "index/AllTweetsIndex");

        ArrayList<Document> results = tim.searchForField("name", (name + " " + surname).toLowerCase(), 10000);
        int max = 0;
        String id = "";

        for (Document doc : results) {
            if (Integer.parseInt(doc.get("followers")) >= max) {
                max = Integer.parseInt(doc.get("followers"));
                id = doc.get("screenName");
            }
        }
        String[] result = {id, new Integer(max).toString()};
        return result;
    }
}
