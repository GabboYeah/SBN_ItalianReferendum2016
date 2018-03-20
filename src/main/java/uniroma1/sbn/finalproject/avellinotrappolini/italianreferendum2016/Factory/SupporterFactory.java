/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Factory;

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
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetTerm;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.PoliticiansIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetWordBuilder;

/**
 *
 * @author Gabriele
 */
public class SupporterFactory {

    //HashMap<String, Supporter>
    public void generateTermByTerm(ArrayList<String> yesExpressions, ArrayList<String> noExpressions, String corePath) {

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
                supporters.put(supporter.getUserId(), supporter);
            }

            results = tim.searchTermsInAField(noPols, "screenName");
            for (ScoreDoc doc : results) {
                Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                supporter.setIsANoPol(Boolean.TRUE);
                supporters.put(supporter.getUserId(), supporter);
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
                    supporter.setYesPolsMentioned(supporter.getYesPolsMentioned()+ 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setYesPolsMentioned(1);
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            results = tim.searchTermsInAField(noPols, "mentioned");
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setNoPolsMentioned(supporter.getNoPolsMentioned()+ 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setNoPolsMentioned(1);
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            ObjectMapper mapper = new ObjectMapper();

            HashMap<String, ArrayList<String>> representativeWordsMap;
            representativeWordsMap = mapper.readValue(new File(corePath),
                    new TypeReference<HashMap<String, ArrayList<String>>>() {
            });

            ArrayList<String> yesTerms = representativeWordsMap.get("yes");
            ArrayList<String> noTerms = representativeWordsMap.get("no");
            System.out.println("Adding for terms:");
            ArrayList<String> yesWords = new ArrayList<String>();
            ArrayList<String> yesTags = new ArrayList<String>();
            for(String term : yesTerms){
                if(term.startsWith("#"))
                    yesTags.add(term);
                else
                    yesWords.add(term);
            }
            results = tim.searchTermsInAField(yesWords, "tweetText");
            results = (ScoreDoc[])ArrayUtils.addAll(results, tim.searchTermsInAField(yesTags, "hashtags"));
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setYesCostructionsUsed(supporter.getYesCostructionsUsed() + 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setYesCostructionsUsed(1);
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            ArrayList<String> noWords = new ArrayList<String>();
            ArrayList<String> noTags = new ArrayList<String>();
            for(String term : noTerms){
                if(term.startsWith("#"))
                    noTags.add(term);
                else
                    noWords.add(term);
            }
            results = tim.searchTermsInAField(noWords, "tweetText");
            results = (ScoreDoc[])ArrayUtils.addAll(results, tim.searchTermsInAField(noTags, "hashtags"));
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setNoCostructionsUsed(supporter.getNoCostructionsUsed() + 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setNoCostructionsUsed(1);
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            System.out.println("Adding for Expression:");
            results = tim.searchTermsInAField(yesExpressions, "hashtags");
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setYesExpressionsUsed(supporter.getYesExpressionsUsed() + 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setYesExpressionsUsed(1);
                    supporters.put(supporter.getUserId(), supporter);
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
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            mapper.writeValue(new File("output/termsSupporters.json"), supporters);

        } catch (IOException ex) {
            Logger.getLogger(SupporterFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void generateCoreByCore(ArrayList<String> yesExpressions, ArrayList<String> noExpressions, String corePath) {

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
                supporters.put(supporter.getUserId(), supporter);
            }

            results = tim.searchTermsInAField(noPols, "screenName");
            for (ScoreDoc doc : results) {
                Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                supporter.setIsANoPol(Boolean.TRUE);
                supporters.put(supporter.getUserId(), supporter);
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
                    supporter.setYesPolsMentioned(supporter.getYesPolsMentioned()+ 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setYesPolsMentioned(1);
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            results = tim.searchTermsInAField(noPols, "mentioned");
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setNoPolsMentioned(supporter.getNoPolsMentioned()+ 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setNoPolsMentioned(1);
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            ObjectMapper mapper = new ObjectMapper();

            HashMap<String, ArrayList<ArrayList<String>>> representativeWordsMap;
            representativeWordsMap = mapper.readValue(new File(corePath),
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
                    supporters.put(supporter.getUserId(), supporter);
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
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            System.out.println("Adding for Expression:");
            results = tim.searchTermsInAField(yesExpressions, "hashtags");
            for (ScoreDoc doc : results) {
                String userId = tim.ir.document(doc.doc).get("userId");
                if (supporters.containsKey(userId)) {
                    Supporter supporter = supporters.get(userId);
                    supporter.setYesExpressionsUsed(supporter.getYesExpressionsUsed() + 1);
                } else {
                    Supporter supporter = new Supporter(tim.ir.document(doc.doc).get("userId"), tim.ir.document(doc.doc).get("screenName"));
                    supporter.setYesExpressionsUsed(1);
                    supporters.put(supporter.getUserId(), supporter);
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
                    supporters.put(supporter.getUserId(), supporter);
                }
            }

            System.out.println("Total supporters: " + supporters.size());
            System.out.println("supporters added: " + (supporters.size() - addedRecently));
            addedRecently = supporters.size();

            mapper.writeValue(new File("output/ccSupporters.json"), supporters);

        } catch (IOException ex) {
            Logger.getLogger(SupporterFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
