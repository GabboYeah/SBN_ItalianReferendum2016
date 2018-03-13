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
import java.util.zip.GZIPInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.StatusWrapper;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class TweetsIndexBuilder extends IndexBuilder {

    //Document
    private Document tweet;
    //Twitter number id
    private LongField userId;
    //Tweet publication date
    private LongField date;
    //name
    private StringField name;
    //@name
    private StringField screenName;
    //Tweet text
    private TextField tweetText;
    //
    private TextField hashtags;
    //
    private LongField followers;

    private String tweetsSourcePath;
    private String polsSourcePath;

    public TweetsIndexBuilder(String sourcePath, String indexPath) {
        this.tweet = new Document();
        this.userId = new LongField("userId", 0L, Field.Store.YES);
        this.date = new LongField("date", 0L, Field.Store.YES);
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.tweetText = new TextField("tweetText", "", Field.Store.YES);
        this.hashtags = new TextField("hashtags", "", Field.Store.YES);
        this.followers = new LongField("followers", 0L, Field.Store.YES);

        this.tweet.add(this.userId);
        this.tweet.add(this.date);
        this.tweet.add(this.name);
        this.tweet.add(this.screenName);
        this.tweet.add(this.tweetText);
        this.tweet.add(this.hashtags);
        this.tweet.add(this.followers);

        this.sourcePath = sourcePath;
        this.indexPath = indexPath;
    }

    public TweetsIndexBuilder(String tweetsSourcePath, String polsSourcePath, String indexPath) {
        this.tweet = new Document();
        this.userId = new LongField("userId", 0L, Field.Store.YES);
        this.date = new LongField("date", 0L, Field.Store.YES);
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.tweetText = new TextField("tweetText", "", Field.Store.YES);
        this.hashtags = new TextField("hashtags", "", Field.Store.YES);
        this.followers = new LongField("followers", 0L, Field.Store.YES);

        this.tweet.add(this.userId);
        this.tweet.add(this.date);
        this.tweet.add(this.name);
        this.tweet.add(this.screenName);
        this.tweet.add(this.tweetText);
        this.tweet.add(this.hashtags);
        this.tweet.add(this.followers);

        this.tweetsSourcePath = tweetsSourcePath;
        this.polsSourcePath = polsSourcePath;
        this.indexPath = indexPath;
    }

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
                //System.out.println("----" + stream);

                FileInputStream fstream = new FileInputStream(stream.toString());
                GZIPInputStream gzstream = new GZIPInputStream(fstream);
                InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
                BufferedReader br = new BufferedReader(isr);

                String line;

                while ((line = br.readLine()) != null) {
                    sw = new StatusWrapper();
                    sw.load(line);
//                        System.out.println("--------" + sw.getTime()
//                                + " - " + sw.getStatus().getUser().getScreenName()
//                                + ", " + sw.getStatus().getUser().getId()
//                                + ", " + sw.getStatus().getUser().getName().toLowerCase()
//                                + ", " + sw.getStatus().getText());
                    this.userId.setLongValue(sw.getStatus().getUser().getId());
                    this.date.setLongValue(sw.getTime());
                    this.name.setStringValue(sw.getStatus().getUser().getName().toLowerCase());
                    this.screenName.setStringValue(sw.getStatus().getUser().getScreenName());
                    //System.out.println("--------");
                    //System.out.println(sw.getStatus().getText());

//                    String cleanedText = sw.getStatus().getText().replace("RT ", "");
//                    cleanedText = cleanedText.replaceAll("https:\\/\\/\\S*", "");
//                    cleanedText = cleanedText.replaceAll("https:\\/\\/\\S*$", "");
//                    cleanedText = cleanedText.replaceAll("http:\\/\\/\\S*", "");
//                    cleanedText = cleanedText.replaceAll("http:\\/\\/\\S*$", "");
//                    cleanedText = cleanedText.replaceAll("[^(\\w|\\d|\\s)]", "");
//                    cleanedText = cleanedText.replaceAll("@\\S*", "");
//                    cleanedText = cleanedText.replaceAll("#\\S*", "");

                    String cleanedText = sw.getStatus().getText().replace("RT ", " ");
                    cleanedText = cleanedText.replaceAll("http:\\/\\/\\S*", " ");
                    cleanedText = cleanedText.replaceAll("http:\\/\\/\\S*$", " ");
                    cleanedText = cleanedText.replaceAll("https:\\/\\/\\S*", " ");
                    cleanedText = cleanedText.replaceAll("https:\\/\\/\\S*$", " ");
                    cleanedText = cleanedText.replaceAll("[^(\\w|\\d|\\s)]", " ");
                    cleanedText = cleanedText.replaceAll("@\\S*", "");
                    //System.out.println(cleanedText);
                    //System.out.println("--------");
                    this.tweetText.setStringValue(cleanedText);
                    this.followers.setLongValue((long) sw.getStatus().getUser().getFollowersCount());
                    String hashtags = "";
                    for (HashtagEntity hashtag : sw.getStatus().getHashtagEntities()) {
                        hashtags += hashtag.getText() + " ";
                    }
                    this.hashtags.setStringValue(hashtags);
                    this.writer.addDocument(this.tweet);
                }
            }
            j++;
            this.writer.commit();
        }

        this.writer.close();

    }

    public void build(String fieldName, String fieldValue) throws IOException {
        setBuilderParams(indexPath);
        PoliticiansIndexManager pim = new PoliticiansIndexManager("stream", polsSourcePath);;
        TweetsIndexManager tim = new TweetsIndexManager("stream", tweetsSourcePath);;

        ArrayList<Document> politicians = pim.searchForField(fieldName, fieldValue, 10000);

        ArrayList<Document> politcianTweets;

        for (Document p : politicians) {
            //System.out.println("---------------------------");
            //System.out.println(p.get("name"));

            politcianTweets = tim.searchForField("screenName", p.get("screenName"), 10000);
            System.out.println(p.get("screenName") + " " + politcianTweets.size());

            //System.out.println(politcianTweets.size());
            for (Document tweet : politcianTweets) {
                this.userId.setLongValue(Long.parseLong(tweet.get("userId")));
                this.date.setLongValue(Long.parseLong(tweet.get("date")));
                this.name.setStringValue(tweet.get("name"));
                this.screenName.setStringValue(tweet.get("screenName"));
                this.tweetText.setStringValue(tweet.get("tweetText"));
                this.followers.setLongValue(Long.parseLong(tweet.get("followers")));
                this.hashtags.setStringValue(tweet.get("hashtags"));
                this.writer.addDocument(this.tweet);
            }

            this.writer.commit();
        }
    }
}
