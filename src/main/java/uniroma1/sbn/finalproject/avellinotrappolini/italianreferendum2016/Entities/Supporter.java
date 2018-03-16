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
public class Supporter {
    private String userId;
    private int yesPolsMentioned;
    private int noPolsMentioned;
    private int yesTermsUsed;
    private int noTermsUsed;
    private int yesExpressionsUsed;
    private int noExpressionsUsed;
    private Boolean isAYesPol;
    private Boolean isANoPol;

    public Supporter() {
    }

    public Supporter(String userId, int yesPolsMentioned, int noPolsMentioned, int yesTermsUsed, int noTermsUsed, int yesExpressionsUsed, int noExpressionsUsed, Boolean isAYesPol, Boolean isANoPol) {
        this.userId = userId;
        this.yesPolsMentioned = yesPolsMentioned;
        this.noPolsMentioned = noPolsMentioned;
        this.yesTermsUsed = yesTermsUsed;
        this.noTermsUsed = noTermsUsed;
        this.yesExpressionsUsed = yesExpressionsUsed;
        this.noExpressionsUsed = noExpressionsUsed;
        this.isAYesPol = isAYesPol;
        this.isANoPol = isANoPol;
    }

    public String getUserId() {
        return userId;
    }

    public int getYesPolsMentioned() {
        return yesPolsMentioned;
    }

    public int getNoPolsMentioned() {
        return noPolsMentioned;
    }

    public int getYesTermsUsed() {
        return yesTermsUsed;
    }

    public int getNoTermsUsed() {
        return noTermsUsed;
    }

    public int getYesExpressionsUsed() {
        return yesExpressionsUsed;
    }

    public int getNoExpressionsUsed() {
        return noExpressionsUsed;
    }

    public Boolean getIsAYesPol() {
        return isAYesPol;
    }

    public Boolean getIsANoPol() {
        return isANoPol;
    }
    
    
}
