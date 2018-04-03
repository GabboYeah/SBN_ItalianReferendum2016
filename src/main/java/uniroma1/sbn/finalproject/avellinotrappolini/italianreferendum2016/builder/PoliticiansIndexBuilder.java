package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.CSVReader;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 * Builder that creates index of politicians
 *
 * @author Gabriele Avellino
 * @author Giovanni Trappolini
 */
public class PoliticiansIndexBuilder extends IndexBuilder {

    //Politic-Groups and its vote decision
    private HashMap<String, String> groupVote;

    //Document
    private Document politician;
    private StringField vote;
    private StringField name;
    private StringField screenName;

    /**
     * Inizialize builder parameters
     *
     * @param sourcePath where the data to create the index are stored
     * @param indexPath where the index will be stored
     */
    public PoliticiansIndexBuilder(String sourcePath, String indexPath) {
        // Initialize the document
        this.politician = new Document();

        // Initialize its fields
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.vote = new StringField("vote", "", Field.Store.YES);

        // Add the fields to the document
        politician.add(name);
        politician.add(screenName);
        politician.add(vote);

        // Fill the hashmap of politic group and their decisions
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

        // Set paths
        this.sourcePath = sourcePath;
        this.indexPath = indexPath;
    }

    /**
     * Create the index starting from a csv file of politicians
     * @throws IOException
     * @throws TwitterException
     */
    @Override
    public void build() throws IOException, TwitterException {
        // Read the csv file
        CSVReader csvr = new CSVReader(",", sourcePath);
        // Will contains the csv file rows
        ArrayList<String[]> rows;
        // Set builder params
        setBuilderParams(indexPath);
        // Get the whole CSV rows
        rows = csvr.readCSV();

        // Mantain the id of a user
        String id;
        // Manatain the id and the number of the followers of the user
        String[] result;
        // Mantain the number of followers of that user
        int followers;
        // For each politicians in the file read
        for (String[] row : rows) {
            // Get the politician name
            String name = row[0];
            // Get the politician surname
            String surname = row[1];
            // Get all users that have the same name of the politician
            result = findUserTwitterId(name, surname);
            // Get the userId
            id = result[0];
            // Get the number of followers
            followers = Integer.parseInt(result[1]);
            
            System.out.println("Search for : " + name + " " + surname + ", followers: " + followers);
            // If a user was found and it has at list a certain value of followers
            if (!id.equals("") && followers >= 1000) {
                // Create a new document for the index
                this.name.setStringValue((name + " " + surname).toLowerCase());
                this.screenName.setStringValue(id);

                System.out.println("");
                System.out.println(this.politician.get("name"));
                System.out.println(this.politician.get("screenName"));

                // Define the politician vote
                // If the pol vote is equal to his group vote
                if (row[2].equals(groupVote.get(row[3]))) {
                    // Return his vote and write the document
                    this.vote.setStringValue(row[2]);
                    this.writer.addDocument(this.politician);
                    System.out.println(this.politician.get("vote"));
                // If the pol vote is defined in the csv file
                } else if (row[2].equals("si") || row[2].equals("no")) {
                    // Return his vote and write the document
                    this.vote.setStringValue(row[2]);
                    this.writer.addDocument(this.politician);
                    System.out.println(this.politician.get("vote"));
                // If the pol vote is not defined in the csv file
                } else if (row[2].equals("-") && !row[3].equals("Misto")) {
                    // Return his group vote and write the document
                    this.vote.setStringValue(groupVote.get(row[3]));
                    this.writer.addDocument(this.politician);
                    System.out.println(this.politician.get("vote"));
                }
            }
            System.out.println("----------------");
        }
        this.writer.commit();
        this.writer.close();
    }

    @Override
    public void build(String fieldName, ArrayList<String> fieldValues) throws IOException {
        // Not implemented
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Find all the userId related to the politicians name and surname
     * @param name Politician name
     * @param surname Politician surname
     * @return Values of the best result
     * @throws IOException
     */
    public String[] findUserTwitterId(String name, String surname) throws IOException {
        // Initialize a new Tweets index builder for the index of the all tweets
        TweetsIndexManager tim = new TweetsIndexManager("index/AllTweetsIndex");

        ArrayList<String> fieldValues = new ArrayList<String>();
        // Search user that match name + surname
        fieldValues.add((name + " " + surname).toLowerCase());
        // Search user that match surname + name
        fieldValues.add((surname + " " + name).toLowerCase());
        
        ArrayList<Document> results = tim.searchForField("name", fieldValues, 10000);
        
        // Variable that will mantain the max number of followers among the users found
        int max = 0;
        // User id related to the max
        String id = "";
        
        // For each document found 
        for (Document doc : results) {
            // check if the user that made it has the more influencer of our actual max
            if (Integer.parseInt(doc.get("followers")) >= max) {
                // And in case take it as new max
                max = Integer.parseInt(doc.get("followers"));
                id = doc.get("screenName");
            }
        }
        // Return the max
        String[] result = {id, new Integer(max).toString()};
        return result;
    }
}
