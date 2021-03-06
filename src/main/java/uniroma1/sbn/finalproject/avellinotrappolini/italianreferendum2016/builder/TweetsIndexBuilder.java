package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import org.apache.lucene.document.TextField;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;
import static org.apache.lucene.util.Version.LUCENE_41;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.StatusWrapper;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 * Builder that creates index of Tweets
 *
 * @author Gabriele Avellino
 * @author Giovanni Trappolini
 */
public class TweetsIndexBuilder extends IndexBuilder {

    // Document Structure
    private Document tweet;
    // Long: because it is a very long int
    private LongField userId;
    // Long: because it is a very long int
    private LongField date;
    // String: beacuse it's important to save it without applying the analyzer
    private StringField name;
    // String: beacuse it's important to save it without applying the analyzer
    private StringField screenName;
    // Text: analyser applyed
    private TextField tweetText;
    // Text: analyser applyed
    private TextField hashtags;
    // Text: analyser applyed
    private TextField mentioned;
    private LongField followers;

    // Map that define which field is going to have which analyzer
    private Map<String, Analyzer> analyzerPerField;
    // Wrapper that allow to use different analyzers
    private PerFieldAnalyzerWrapper wrapper;

    /**
     * Inizialize builder parameters
     *
     * @param sourcePath where the data to create the index are stored
     * @param indexPath where the index will be stored
     */
    public TweetsIndexBuilder(String sourcePath, String indexPath) {
        // Initialize the document
        this.tweet = new Document();

        // Initialize its fields
        this.userId = new LongField("userId", 0L, Field.Store.YES);
        this.date = new LongField("date", 0L, Field.Store.YES);
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.tweetText = new TextField("tweetText", "", Field.Store.YES);
        this.hashtags = new TextField("hashtags", "", Field.Store.YES);
        this.mentioned = new TextField("mentioned", "", Field.Store.YES);
        this.followers = new LongField("followers", 0L, Field.Store.YES);

        // Add the fields to the document
        this.tweet.add(this.userId);
        this.tweet.add(this.date);
        this.tweet.add(this.name);
        this.tweet.add(this.screenName);
        this.tweet.add(this.tweetText);
        this.tweet.add(this.hashtags);
        this.tweet.add(this.mentioned);
        this.tweet.add(this.followers);

        // Initialize paths
        this.sourcePath = sourcePath;
        this.indexPath = indexPath;
    }

    /**
     * Create the index: In this case data are stored in several directories of
     * zip files
     *
     * @throws IOException
     * @throws TwitterException
     */
    @Override
    public void build() throws IOException, TwitterException {
        // Initialize a path source from source path
        Path sourceDirPath = Paths.get(sourcePath);

        // Set builder params
        setBuilderParams(indexPath);

        // Get a stream of all the directories into the source path
        DirectoryStream<Path> dailyStreamPaths = Files.newDirectoryStream(sourceDirPath);

        // Initialize a status wrapper to get information from the source files
        StatusWrapper sw;
        int j = 1;
        // For each directory in the stream
        for (Path streamDay : dailyStreamPaths) {
            System.out.println(streamDay);

            // Get a new stream of all the files in the directory
            DirectoryStream<Path> streamFiles = Files.newDirectoryStream(streamDay);
            int i = 1;
            // Get the number of files in the dir
            int n = new File(streamDay.toString()).listFiles().length;

            // For each file
            for (Path stream : streamFiles) {

                System.out.println(j + ") " + i + "/" + n);
                i++;
                
                // initialize a new zip file reader
                FileInputStream fstream = new FileInputStream(stream.toString());
                GZIPInputStream gzstream = new GZIPInputStream(fstream);
                InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
                BufferedReader br = new BufferedReader(isr);

                String line;
                // For each line in the file unzipped
                while ((line = br.readLine()) != null) {
                    // load that in a new status wrapper and get elements of interest
                    sw = new StatusWrapper();
                    sw.load(line);
                    this.userId.setLongValue(sw.getStatus().getUser().getId());
                    this.date.setLongValue(sw.getTime());
                    this.name.setStringValue(sw.getStatus().getUser().getName().toLowerCase());
                    this.screenName.setStringValue(sw.getStatus().getUser().getScreenName());
                    // Clean the text
                    String cleanedText = removeTrashParts(sw.getStatus().getText());
                    this.tweetText.setStringValue(cleanedText);
                    
                    this.followers.setLongValue((long) sw.getStatus().getUser().getFollowersCount());
                    
                    // Get te mentioned people
                    String mentionedPeople = "";
                    for (UserMentionEntity user : sw.getStatus().getUserMentionEntities()) {
                        mentionedPeople += user.getText() + " ";
                    }
                    this.mentioned.setStringValue(mentionedPeople);
                    
                    // Get the hashtags used
                    String hashtags = "";
                    for (HashtagEntity hashtag : sw.getStatus().getHashtagEntities()) {
                        hashtags += "#" + hashtag.getText() + " ";
                    }
                    this.hashtags.setStringValue(hashtags.toLowerCase());
                    // Add the document
                    this.writer.addDocument(this.tweet);
                }
            }
            j++;
            // Make a commit
            this.writer.commit();
        }
        
        // Close writer
        this.writer.close();
    }

    /**
     * Create the index: In this case data are stored in another index 
     * @param fieldName name of the field you want to analyze
     * @param fieldValues values of the field you want to match
     * @throws IOException
     */
    @Override
    public void build(String fieldName, ArrayList<String> fieldValues) throws IOException {
        // Set params of the builder
        setBuilderParams(indexPath);
        
        // Create tweet index manager of the source index
        TweetsIndexManager tim = new TweetsIndexManager(sourcePath);

        // A list of all the tweets of interest of the source index
        ArrayList<Document> interestedTweets;

        // For each value in fieldValue
        for (String fieldValue : fieldValues) {
            
            // Get all the tweets that match that value for the chosen field
            interestedTweets = tim.searchForField(fieldName, fieldValue, 10000);
            System.out.println(fieldValue + " " + interestedTweets.size());
            
            // For each tweet found previously, create a doc and add it to the new index
            for (Document tweet : interestedTweets) {
                // Get and save all the fields of the document found
                this.userId.setLongValue(Long.parseLong(tweet.get("userId")));
                this.date.setLongValue(Long.parseLong(tweet.get("date")));
                this.name.setStringValue(tweet.get("name"));
                this.screenName.setStringValue(tweet.get("screenName"));
                this.tweetText.setStringValue(tweet.get("tweetText"));
                this.followers.setLongValue(Long.parseLong(tweet.get("followers")));
                this.hashtags.setStringValue(tweet.get("hashtags"));
                this.mentioned.setStringValue(tweet.get("mentioned"));
                // Write the new document
                this.writer.addDocument(this.tweet);
            }
            // Make a commit
            this.writer.commit();
        }
        // Close the writer
        this.writer.close();
    }

    /**
     * Set builder params
     * @param dirName dir in which inizialize the directory
     * @throws IOException
     */
    @Override
    public void setBuilderParams(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));
        
        // Configure the multianalyzer map
        analyzerPerField = new HashMap<String, Analyzer>();
        // Set an italian analyzer for the tweet texts
        analyzerPerField.put("tweetText", new ItalianAnalyzer(LUCENE_41));
        // Set a white space analizer for the others
        wrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(LUCENE_41), analyzerPerField);

        this.cfg = new IndexWriterConfig(LUCENE_41, wrapper);
        this.writer = new IndexWriter(dir, cfg);
    }

    // Method used to remove irrelevant parts from tweet texts
    private String removeTrashParts(String uncleanedText) {
        // Remove String "RT" used to advise that the tweet is a retweet
        String cleanedText = uncleanedText.replace("RT ", " ");
        // Remove all the urls
        cleanedText = cleanedText.replaceAll("htt\\S*", " ");
        cleanedText = cleanedText.replaceAll("htt\\S*$", " ");
        cleanedText = cleanedText.replaceAll("\\d+\\S*", " ");
        // Remove all the hashtags
        cleanedText = cleanedText.replaceAll("#\\S*", " ");
        // Remove all the mentioned people
        cleanedText = cleanedText.replaceAll("@\\S*", " ");

        return cleanedText;
    }
}