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
import java.util.zip.GZIPInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import static org.apache.lucene.util.Version.LUCENE_41;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO.StatusWrapper;

/**
 *
 * @author Gabriele
 */
public class TweetsIndexBuilder {
    
    //Dir were is saved the stream
    private Directory dir;
    //Lucene paramethers
    private Analyzer analyzer;
    private IndexWriterConfig cfg;
    private IndexWriter writer;
    
    // Document params
    // Document used to add elements to the index
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
    private StringField hashtags;
    //
    private LongField followers;

    
    public TweetsIndexBuilder() {
        this.tweet = new Document();
        this.userId = new LongField("userId", 0L, Field.Store.YES);
        this.date = new LongField("date", 0L, Field.Store.YES);
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.tweetText = new TextField("tweetText", "", Field.Store.YES);
        this.hashtags = new StringField("hashtags", "", Field.Store.YES);
        this.followers = new LongField("followers", 0L, Field.Store.YES);
        
        this.tweet.add(this.userId);
        this.tweet.add(this.date);
        this.tweet.add(this.name);
        this.tweet.add(this.screenName);
        this.tweet.add(this.tweetText);  
        this.tweet.add(this.hashtags);
        this.tweet.add(this.followers);
    }
    
    
        
    public void create(Path streamDirPath, String dirName) {

        try {
            setBuilderParams(dirName);

            DirectoryStream<Path> dailyStreamPaths = Files.newDirectoryStream(streamDirPath);

            StatusWrapper sw;
            int j = 1;
            for (Path streamDay : dailyStreamPaths) {
                System.out.println(streamDay);

                DirectoryStream<Path> streamFiles = Files.newDirectoryStream(streamDay);
                int i = 1;
                int n = new File(streamDay.toString()).listFiles().length;
                for (Path stream : streamFiles) {
                    
                    System.out.println(j +") " + i + "/" + n);
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
                        this.tweetText.setStringValue(sw.getStatus().getText());
                        this.followers.setLongValue((long)sw.getStatus().getUser().getFollowersCount());
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
        } catch (IOException e) {
            System.out.println("Impossibile Creare la cartella desiderate per l'index!");
            e.printStackTrace();
        } catch (TwitterException e) {
            System.out.println("Problemi con la lattura dello stream twitter!");
            e.printStackTrace();
        }
    }

    private void setBuilderParams(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));
        this.analyzer = new ItalianAnalyzer(LUCENE_41);
        this.cfg = new IndexWriterConfig(LUCENE_41, analyzer);
        this.writer = new IndexWriter(dir, cfg);
    }
}
