/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import static org.apache.lucene.util.Version.LUCENE_41;
import twitter4j.TwitterException;

/**
 *
 * @author Gabriele
 */
public abstract class IndexBuilder {
    public Directory dir;
    //Lucene paramethers
    public Analyzer analyzer;
    public IndexWriterConfig cfg;
    public IndexWriter writer;
    
    public String sourcePath;
    public String indexPath;
    
    public abstract void build() throws IOException, TwitterException;
    
    public void setBuilderParams(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));
        this.analyzer = new ItalianAnalyzer(LUCENE_41);
        this.cfg = new IndexWriterConfig(LUCENE_41, analyzer);
        this.writer = new IndexWriter(dir, cfg);
    }
}
