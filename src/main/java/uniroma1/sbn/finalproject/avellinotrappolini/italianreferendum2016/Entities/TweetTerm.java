/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities;

/**
 *
 * @author Gabriele
 */
public class TweetTerm implements Comparable<TweetTerm>{
    private String word;
    private String type;
    private int frequency;
    private String saxRep;
    private double[] timeSeries;
    private double[] binaryRep;

    /**
     *
     * @param word
     * @param type
     * @param frequency
     * @param saxRep
     * @param timeSeries
     */
    public TweetTerm(String word, String type, int frequency, String saxRep, double[] timeSeries) {
        this.word = word;
        this.type = type;
        this.frequency = frequency;
        this.saxRep = saxRep;
        this.timeSeries = timeSeries;
        toBinary();
    }

    /**
     *
     * @return
     */
    public String getWord() {
        return word;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param word
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     *
     * @return
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     *
     * @param frequency
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     *
     * @return
     */
    public String getSaxRep() {
        return saxRep;
    }

    /**
     *
     * @param saxRep
     */
    public void setSaxRep(String saxRep) {
        this.saxRep = saxRep;
    }

    /**
     *
     * @return
     */
    public double[] getTimeSeries() {
        return timeSeries;
    }

    /**
     *
     * @param timeSeries
     */
    public void setTimeSeries(double[] timeSeries) {
        this.timeSeries = timeSeries;
    }

    /**
     *
     * @return
     */
    public double[] getBinaryRep() {
        return binaryRep;
    }

    /**
     *
     * @param binaryRep
     */
    public void setBinaryRep(double[] binaryRep) {
        this.binaryRep = binaryRep;
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public int compareTo(TweetTerm t) {
        return t.getFrequency() - this.frequency;
    }
    
    private void toBinary(){
        int i;
        this.binaryRep = new double[saxRep.length()]; 
        for(i = 0; i < saxRep.length(); i++){
            if(saxRep.charAt(i) == 'a')
                binaryRep[i] = 0;
            else
                binaryRep[i] = 1;
        }
    }
    
    
}
