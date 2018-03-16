/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import static jdk.nashorn.internal.runtime.Version.version;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class TweetsIndexBuilder extends IndexBuilder {

    private Document tweet;
    private LongField userId;
    private LongField date;
    private StringField name;
    private StringField screenName;
    private TextField tweetText;
    private TextField hashtags;
    private TextField mentioned;
    private LongField followers;

    private Map<String, Analyzer> analyzerPerField;
    private PerFieldAnalyzerWrapper wrapper;

    /**
     *
     * @param sourcePath
     * @param indexPath
     */
    public TweetsIndexBuilder(String sourcePath, String indexPath) {
        this.tweet = new Document();
        this.userId = new LongField("userId", 0L, Field.Store.YES);
        this.date = new LongField("date", 0L, Field.Store.YES);
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.tweetText = new TextField("tweetText", "", Field.Store.YES);
        this.hashtags = new TextField("hashtags", "", Field.Store.YES);
        this.mentioned = new TextField("mentioned", "", Field.Store.YES);
        this.followers = new LongField("followers", 0L, Field.Store.YES);

        this.tweet.add(this.userId);
        this.tweet.add(this.date);
        this.tweet.add(this.name);
        this.tweet.add(this.screenName);
        this.tweet.add(this.tweetText);
        this.tweet.add(this.hashtags);
        this.tweet.add(this.mentioned);
        this.tweet.add(this.followers);

        this.sourcePath = sourcePath;
        this.indexPath = indexPath;
    }

    /**
     *
     * @throws IOException
     * @throws TwitterException
     */
    @Override
    public void build() throws IOException, TwitterException {
        Path sourceDirPath = Paths.get(sourcePath);

        setBuilderParams(indexPath);

        DirectoryStream<Path> dailyStreamPaths = Files.newDirectoryStream(sourceDirPath);

        StatusWrapper sw;
        int j = 1;
        for (Path streamDay : dailyStreamPaths) {
            System.out.println(streamDay);

            DirectoryStream<Path> streamFiles = Files.newDirectoryStream(streamDay);
            int i = 1;
            int n = new File(streamDay.toString()).listFiles().length;
            for (Path stream : streamFiles) {

                System.out.println(j + ") " + i + "/" + n);
                i++;

                FileInputStream fstream = new FileInputStream(stream.toString());
                GZIPInputStream gzstream = new GZIPInputStream(fstream);
                InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
                BufferedReader br = new BufferedReader(isr);

                String line;

                while ((line = br.readLine()) != null) {
                    sw = new StatusWrapper();
                    sw.load(line);
                    this.userId.setLongValue(sw.getStatus().getUser().getId());
                    this.date.setLongValue(sw.getTime());
                    this.name.setStringValue(sw.getStatus().getUser().getName().toLowerCase());
                    this.screenName.setStringValue(sw.getStatus().getUser().getScreenName());
                    String cleanedText = removeTrashParts(sw.getStatus().getText());
                    this.tweetText.setStringValue(cleanedText);
                    this.followers.setLongValue((long) sw.getStatus().getUser().getFollowersCount());

                    String mentionedPeople = "";
                    for (UserMentionEntity user : sw.getStatus().getUserMentionEntities()) {
                        mentionedPeople += user.getText() + " ";
                    }

                    this.mentioned.setStringValue(mentionedPeople.toLowerCase());

                    String hashtags = "";
                    for (HashtagEntity hashtag : sw.getStatus().getHashtagEntities()) {
                        hashtags += "#" + hashtag.getText() + " ";
                    }
                    this.hashtags.setStringValue(hashtags.toLowerCase());
                    
                    this.writer.addDocument(this.tweet);
                }
            }
            j++;
            this.writer.commit();
        }

        this.writer.close();
    }

    /**
     *
     * @param fieldName
     * @param fieldValues
     * @throws IOException
     */
    @Override
    public void build(String fieldName, ArrayList<String> fieldValues) throws IOException {
        setBuilderParams(indexPath);

        TweetsIndexManager tim = new TweetsIndexManager(sourcePath);

        ArrayList<Document> interestedTweets;

        for (String fieldValue : fieldValues) {

            interestedTweets = tim.searchForField(fieldName, fieldValue, 10000);
            System.out.println(fieldValue + " " + interestedTweets.size());

            for (Document tweet : interestedTweets) {
                this.userId.setLongValue(Long.parseLong(tweet.get("userId")));
                this.date.setLongValue(Long.parseLong(tweet.get("date")));
                this.name.setStringValue(tweet.get("name"));
                this.screenName.setStringValue(tweet.get("screenName"));
                this.tweetText.setStringValue(tweet.get("tweetText"));
                this.followers.setLongValue(Long.parseLong(tweet.get("followers")));
                this.hashtags.setStringValue(tweet.get("hashtags"));
                this.mentioned.setStringValue(tweet.get("mentioned"));
                this.writer.addDocument(this.tweet);
            }

            this.writer.commit();
        }
    }

    /**
     *
     * @param dirName
     * @throws IOException
     */
    @Override
    public void setBuilderParams(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));

        analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put("tweetText", new ItalianAnalyzer(LUCENE_41));
        wrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(LUCENE_41), analyzerPerField);

        this.cfg = new IndexWriterConfig(LUCENE_41, wrapper);
        this.writer = new IndexWriter(dir, cfg);
    }

    private String removeTrashParts(String uncleanedText) {
        String cleanedText = uncleanedText.replace("RT ", " ");
        cleanedText = cleanedText.replaceAll("http:\\/\\/\\S*", " ");
        cleanedText = cleanedText.replaceAll("http:\\/\\/\\S*$", " ");
        cleanedText = cleanedText.replaceAll("https:\\/\\/\\S*", " ");
        cleanedText = cleanedText.replaceAll("https:\\/\\/\\S*$", " ");
        cleanedText = cleanedText.replaceAll("#\\S*", " ");
        cleanedText = cleanedText.replaceAll("@\\S*", "");
        
        return cleanedText;
    }
}
