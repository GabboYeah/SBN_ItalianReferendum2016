/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.ScoreDoc;
import twitter4j.TwitterException;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.SupportersIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;

/**
 *
 * @author Gabriele
 */
public class SupportersIndexBuilder extends IndexBuilder {

    private ArrayList<String> yesExpressions;
    private ArrayList<String> noExpressions;

    private Document supporter;
    private StringField name;
    private StringField id;
    private IntField yesPolsMentioned;
    private IntField noPolsMentioned;
    private IntField yesConstructionsUsed;
    private IntField noConstructionsUsed;
    private IntField yesExpressionsUsed;
    private IntField noExpressionsUsed;
    private IntField isAYesPol;
    private IntField isANoPol;

    public SupportersIndexBuilder(String indexPath, String sourcePath, ArrayList<String> yesExpressions, ArrayList<String> noExpressions) {
        super();

        this.indexPath = indexPath;
        this.yesExpressions = yesExpressions;
        this.noExpressions = noExpressions;
        this.sourcePath = sourcePath;

        this.supporter = new Document();
        this.name = new StringField("name", "", Field.Store.YES);
        this.id = new StringField("id", "", Field.Store.YES);
        this.yesPolsMentioned = new IntField("yesPolsMentioned", 0, Field.Store.YES);
        this.noPolsMentioned = new IntField("noPolsMentioned", 0, Field.Store.YES);
        this.yesConstructionsUsed = new IntField("yesConstructionsUsed", 0, Field.Store.YES);
        this.noConstructionsUsed = new IntField("noConstructionsUsed", 0, Field.Store.YES);
        this.yesExpressionsUsed = new IntField("yesExpressionsUsed", 0, Field.Store.YES);
        this.noExpressionsUsed = new IntField("noExpressionsUsed", 0, Field.Store.YES);
        this.isAYesPol = new IntField("isAYesPol", 0, Field.Store.YES);
        this.isANoPol = new IntField("isANoPol", 0, Field.Store.YES);

        this.supporter.add(this.name);
        this.supporter.add(this.id);
        this.supporter.add(this.yesPolsMentioned);
        this.supporter.add(this.noPolsMentioned);
        this.supporter.add(this.yesConstructionsUsed);
        this.supporter.add(this.noConstructionsUsed);
        this.supporter.add(this.yesExpressionsUsed);
        this.supporter.add(this.noExpressionsUsed);
        this.supporter.add(this.isAYesPol);
        this.supporter.add(this.isANoPol);
    }

    @Override
    public void build() throws IOException, TwitterException {

        setBuilderParams(indexPath);

        HashMap<String, Supporter> supporters = collectIndexElements();
        
        for (String key : supporters.keySet()) {
            
            Supporter s = supporters.get(key);
            //System.out.println(s.toString());
            
            this.name.setStringValue(s.getName());
            this.id.setStringValue(s.getId());
            this.yesPolsMentioned.setIntValue(s.getYesPolsMentioned());
            this.noPolsMentioned.setIntValue(s.getNoPolsMentioned());
            this.yesConstructionsUsed.setIntValue(s.getYesCostructionsUsed());
            this.noConstructionsUsed.setIntValue(s.getNoCostructionsUsed());
            this.yesExpressionsUsed.setIntValue(s.getYesExpressionsUsed());
            this.noExpressionsUsed.setIntValue(s.getNoExpressionsUsed());
            if (s.getIsAYesPol()) {
                this.isAYesPol.setIntValue(1);
                this.isANoPol.setIntValue(0);
            } else if (s.getIsANoPol()) {
                this.isAYesPol.setIntValue(0);
                this.isANoPol.setIntValue(1);
            } else {
                this.isAYesPol.setIntValue(0);
                this.isANoPol.setIntValue(0);
            }
            this.writer.addDocument(this.supporter);
        }
        this.writer.commit();
    }

    @Override
    public void build(String fieldName, ArrayList<String> fieldValues) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private HashMap<String, Supporter> collectIndexElements() {
        TweetsIndexManager tim = new TweetsIndexManager("index/AllTweetsIndex");
        PoliticiansIndexManager pim = new PoliticiansIndexManager("index/AllPoliticiansIndex");
        HashMap<String, Supporter> supporters = new HashMap<String, Supporter>();

        try {
            tim.setReader("index/AllTweetsIndex");
            ArrayList<String> yesPols = pim.getFieldValuesList(pim.searchForField("vote", "si", 100000000), "screenName");
            ArrayList<String> noPols = pim.getFieldValuesList(pim.searchForField("vote", "no", 100000000), "screenName");

            int addedRecently = 0;

            System.out.println("Adding Politicians:");
            ScoreDoc[] results = tim.searchTermsInAField(yesPols, "screenName");
            for (ScoreDoc doc : results) {
                Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                supporter.setIsAYesPol(Boolean.TRUE);
                supporters.put(supporter.getId(), supporter);
            }

            results = tim.searchTermsInAField(noPols, "screenName");
            for (ScoreDoc doc : results) {
                Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                supporter.setIsANoPol(Boolean.TRUE);
                supporters.put(supporter.getId(), supporter);
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            System.out.println("Adding for mentioned:");
            results = tim.searchTermsInAField(yesPols, "mentioned");
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setYesPolsMentioned(supporter.getYesPolsMentioned() + 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setYesPolsMentioned(1);
                    supporters.put(supporter.getId(), supporter);
                }
            }

            results = tim.searchTermsInAField(noPols, "mentioned");
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setNoPolsMentioned(supporter.getNoPolsMentioned() + 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setNoPolsMentioned(1);
                    supporters.put(supporter.getId(), supporter);
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            if (sourcePath == "output/relWords.json") {
                ObjectMapper mapper = new ObjectMapper();

                HashMap<String, ArrayList<String>> representativeWordsMap;
                representativeWordsMap = mapper.readValue(new File(sourcePath),
                        new TypeReference<HashMap<String, ArrayList<String>>>() {
                });

                ArrayList<String> yesTerms = representativeWordsMap.get("yes");
                ArrayList<String> noTerms = representativeWordsMap.get("no");
                System.out.println("Adding for terms:");
                ArrayList<String> yesWords = new ArrayList<String>();
                ArrayList<String> yesTags = new ArrayList<String>();
                for (String term : yesTerms) {
                    if (term.startsWith("#")) {
                        yesTags.add(term);
                    } else {
                        yesWords.add(term);
                    }
                }
                results = tim.searchTermsInAField(yesWords, "tweetText");
                results = (ScoreDoc[]) ArrayUtils.addAll(results, tim.searchTermsInAField(yesTags, "hashtags"));
                for (ScoreDoc doc : results) {
                    String userId = tim.ir.document(doc.doc).get("userId");
                    if (supporters.containsKey(userId)) {
                        Supporter supporter = supporters.get(userId);
                        supporter.setYesCostructionsUsed(supporter.getYesCostructionsUsed() + 1);
                    } else {
                        Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                        supporter.setYesCostructionsUsed(1);
                        supporters.put(supporter.getId(), supporter);
                    }
                }

                ArrayList<String> noWords = new ArrayList<String>();
                ArrayList<String> noTags = new ArrayList<String>();
                for (String term : noTerms) {
                    if (term.startsWith("#")) {
                        noTags.add(term);
                    } else {
                        noWords.add(term);
                    }
                }
                results = tim.searchTermsInAField(noWords, "tweetText");
                results = (ScoreDoc[]) ArrayUtils.addAll(results, tim.searchTermsInAField(noTags, "hashtags"));
                for (ScoreDoc doc : results) {
                    String userId = tim.ir.document(doc.doc).get("userId");
                    if (supporters.containsKey(userId)) {
                        Supporter supporter = supporters.get(userId);
                        supporter.setNoCostructionsUsed(supporter.getNoCostructionsUsed() + 1);
                    } else {
                        Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                        supporter.setNoCostructionsUsed(1);
                        supporters.put(supporter.getId(), supporter);
                    }
                }
            } else {
                ObjectMapper mapper = new ObjectMapper();

                HashMap<String, ArrayList<ArrayList<String>>> representativeWordsMap;
                representativeWordsMap = mapper.readValue(new File(sourcePath),
                        new TypeReference<HashMap<String, ArrayList<ArrayList<String>>>>() {
                });

                ArrayList<ArrayList<String>> yesCores = representativeWordsMap.get("yes");
                ArrayList<ArrayList<String>> noCores = representativeWordsMap.get("no");
                System.out.println("Adding for Cores:");
                results = tim.searchORANDCondInAField(yesCores);
                for (ScoreDoc doc : results) {
                    String userId = tim.ir.document(doc.doc).get("userId");
                    if (supporters.containsKey(userId)) {
                        Supporter supporter = supporters.get(userId);
                        supporter.setYesCostructionsUsed(supporter.getYesCostructionsUsed() + 1);
                    } else {
                        Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                        supporter.setYesCostructionsUsed(1);
                        supporters.put(supporter.getId(), supporter);
                    }
                }

                results = tim.searchORANDCondInAField(noCores);
                for (ScoreDoc doc : results) {
                    String userId = tim.ir.document(doc.doc).get("userId");
                    if (supporters.containsKey(userId)) {
                        Supporter supporter = supporters.get(userId);
                        supporter.setNoCostructionsUsed(supporter.getNoCostructionsUsed() + 1);
                    } else {
                        Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                        supporter.setNoCostructionsUsed(1);
                        supporters.put(supporter.getId(), supporter);
                    }
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            System.out.println("Adding for Expressions:");
            results = tim.searchTermsInAField(yesExpressions, "hashtags");
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setYesExpressionsUsed(supporter.getYesExpressionsUsed() + 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setYesExpressionsUsed(1);
                    supporters.put(supporter.getId(), supporter);
                }
            }

            results = tim.searchTermsInAField(noExpressions, "hashtags");
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setNoExpressionsUsed(supporter.getNoExpressionsUsed() + 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setNoExpressionsUsed(1);
                    supporters.put(supporter.getId(), supporter);
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            return supporters;

        } catch (IOException ex) {
            Logger.getLogger(SupportersIndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
