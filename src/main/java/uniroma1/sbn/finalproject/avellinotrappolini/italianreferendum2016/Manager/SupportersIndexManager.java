package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.SupportersIndexBuilder;

/**
 * This class manage the creation and the comunication with indices of
 * supporters
 *
 * @author Gabriele Avellino
 * @author Giovanni Trappolini
 */
public class SupportersIndexManager extends IndexManager {

    private ArrayList<String> yesExpressions;
    private ArrayList<String> noExpressions;

    public SupportersIndexManager(String indexPath, ArrayList<String> yesExpressions, ArrayList<String> noExpressions) {
        super(indexPath);

        this.yesExpressions = yesExpressions;
        this.noExpressions = noExpressions;
    }

    public SupportersIndexManager(String indexPath) {
        super(indexPath);

        this.yesExpressions = new ArrayList<>();
        this.noExpressions = new ArrayList<>();
    }

    @Override
    public void create(String sourcePath) {
        try {
            // Create a new supporters index builder
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
        // Not implemented version
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long getAllSupportersTweets() {
        TweetsIndexManager tim = new TweetsIndexManager("index/AllTweetsIndex");
        ArrayList<String> supportersId = new ArrayList<>();
        try {
            this.setReader(this.indexPath);

            for (int i = 0; i < ir.numDocs(); i++) {
                supportersId.add(ir.document(i).get("id"));
            }
            
            System.out.println("prima");
            System.out.println("+++ Total Number of Supporters Tweets: "
                    + tim.searchForField("userId", supportersId, 10000).size());
            
            return tim.searchForField("userId", supportersId, 10000).size();
            
        } catch (IOException ex) {
            Logger.getLogger(SupportersIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
}
