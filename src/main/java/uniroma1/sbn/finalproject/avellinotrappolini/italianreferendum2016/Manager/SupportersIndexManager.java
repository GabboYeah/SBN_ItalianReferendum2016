/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetTerm;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.SupportersIndexBuilder;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetWordBuilder;

/**
 *
 * @author Gabriele
 */
public class SupportersIndexManager extends IndexManager {
    private ArrayList<String> yesExpressions;
    private ArrayList<String> noExpressions;

    public SupportersIndexManager(String indexPath, ArrayList<String> yesExpressions, ArrayList<String> noExpressions) {
        super(indexPath);
        
        this.yesExpressions = yesExpressions;
        this.noExpressions = noExpressions;
    }

    @Override
    public void create(String sourcePath) {
        try {
            SupportersIndexBuilder sib = new SupportersIndexBuilder(indexPath, sourcePath, yesExpressions, noExpressions);
            sib.build();
            
        } catch (IOException ex) {
            Logger.getLogger(SupportersIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TwitterException ex) {
            Logger.getLogger(SupportersIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void create(String sourcePath, String fieldName, ArrayList<String> fieldValues) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
