/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder;

import java.util.Arrays;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetTerm;

/**
 *
 * @author Gabriele
 */
public class TweetWordBuilder {

    int alphabetSize;
    double nThreshold;
    NormalAlphabet na;

    /**
     *
     * @param alphabetSize
     * @param nThreshold
     */
    public TweetWordBuilder(int alphabetSize, double nThreshold) {
        this.alphabetSize = alphabetSize;
        this.nThreshold = nThreshold;
        this.na = new NormalAlphabet();
        this.sp = new SAXProcessor();
    }
    SAXProcessor sp;

    /**
     *
     * @param word
     * @param type
     * @param timeSeries
     * @param frequency
     * @return
     * @throws SAXException
     */
    public TweetTerm build(String word, String type, double[] timeSeries, int frequency) throws SAXException {
        SAXRecords res = sp.ts2saxByChunking(timeSeries, timeSeries.length, na.getCuts(alphabetSize), nThreshold);
        String sax = res.getSAXString("");
        //System.out.println(word + ", " + frequency + ", " + sax + ", " + Arrays.toString(timeSeries));
        return new TweetTerm(word, type, frequency, sax, timeSeries);
    }
}
