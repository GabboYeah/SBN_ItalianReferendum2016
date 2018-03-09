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
public class TweetWord implements Comparable<TweetWord>{
    private String word;
    private int frequency;
    private String saxRep;
    private double[] timeSeries;
    private double[] binaryRep;

    public TweetWord(String word, int frequency, String saxRep, double[] timeSeries) {
        this.word = word;
        this.frequency = frequency;
        this.saxRep = saxRep;
        this.timeSeries = timeSeries;
        toBinary();
    }
    
    public int getSize() {
        
        return this.timeSeries.length;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getSaxRep() {
        return saxRep;
    }

    public void setSaxRep(String saxRep) {
        this.saxRep = saxRep;
    }

    public double[] getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(double[] timeSeries) {
        this.timeSeries = timeSeries;
    }

    public double[] getBinaryRep() {
        return binaryRep;
    }

    public void setBinaryRep(double[] binaryRep) {
        this.binaryRep = binaryRep;
    }

    @Override
    public int compareTo(TweetWord t) {
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
