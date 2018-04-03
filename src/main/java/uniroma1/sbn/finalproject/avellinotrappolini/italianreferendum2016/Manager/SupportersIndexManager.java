package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.SupportersIndexBuilder;

/**
 * This class manage the creation and the comunication with indices of supporters
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
}
