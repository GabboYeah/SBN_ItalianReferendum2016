/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Factory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.Supporter;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetTerm;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager.TweetsIndexManager;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.TweetWordBuilder;

/**
 *
 * @author Gabriele
 */
public class SupporterFactory {

    public HashMap<String, Supporter> generate(ArrayList<String> yesPoliticiansScreenNames, ArrayList<String> yesTerms,
            ArrayList<String> yesExpressions, ArrayList<String> noPoliticiansScreenNames,
            ArrayList<String> noTerms, ArrayList<String> noExpressions) throws IOException {

        TweetsIndexManager tim = new TweetsIndexManager("index/AllTweetsIndex");
        tim.setReader("index/AllTweetsIndex");
        HashMap<String, Supporter> Supporters = new HashMap<String, Supporter>();

        Fields fields = MultiFields.getFields(tim.ir);
        String[] relevantFields = {"userId"};

        for (String rel : relevantFields) {
            Terms terms = fields.terms(rel);

            TermsEnum termsEnum = terms.iterator(null);

            while (termsEnum.next() != null) {

                String id;
                int yesPolsMentioned = 0;
                int noPolsMentioned = 0;
                int yesTermsUsed = 0;
                int noTermsUsed = 0;
                int yesExpressionsUsed = 0;
                int noExpressionsUsed = 0;
                Boolean isAYesPol = Boolean.FALSE;
                Boolean isANoPol = Boolean.FALSE;
                
                BytesRef byteRef = termsEnum.term();
                long longRep = NumericUtils.prefixCodedToLong(byteRef);
                id = Long.toString(longRep);
                
                ArrayList<Document> relDocs = tim.searchForField("userId", id, 1000000);
                System.out.println(relDocs.size());
                for (Document doc : relDocs) {
                    
                    for (String mentioned : doc.get("mentioned").split(" ")) {
                        if (yesPoliticiansScreenNames.contains(mentioned)) {
                            yesPolsMentioned++;
                        } else if (noPoliticiansScreenNames.contains(mentioned)) {
                            noPolsMentioned++;
                        }
                    }

                    for (String token : doc.get("tweetText").split(" ")) {
                        if (yesTerms.contains(token)) {
                            yesTermsUsed++;
                        } else if (noTerms.contains(token)) {
                            noTermsUsed++;
                        }
                    }

                    for (String token : doc.get("hashtags").split(" ")) {
                        if (yesTerms.contains(token)) {
                            yesTermsUsed++;
                        } else if (yesExpressions.contains(token)){
                            yesExpressionsUsed++;
                        } else if (noTerms.contains(token)) {
                            noTermsUsed++;
                        } else if (noExpressions.contains(token)){
                            noExpressionsUsed++;
                        }
                    }
                }
                
                if(yesPoliticiansScreenNames.contains(relDocs.get(0).get("screenName")))
                    isAYesPol = Boolean.TRUE;
                else if(noPoliticiansScreenNames.contains(relDocs.get(0).get("screenName")))
                    isANoPol = Boolean.TRUE;
                
                if(yesPolsMentioned + noPolsMentioned + yesTermsUsed + 
                        noTermsUsed + yesExpressionsUsed + noExpressionsUsed > 0){
                    Supporter s = new Supporter(id, yesPolsMentioned, noPolsMentioned,
                            yesTermsUsed, noTermsUsed, yesExpressionsUsed, noExpressionsUsed, 
                            isAYesPol, isANoPol);
                    Supporters.put(id, s);
                    System.out.println("-" + relDocs.get(0).get("screenName") + " " + s.getUserId());
                }
            }
        }
        
        return Supporters;
    }
}
