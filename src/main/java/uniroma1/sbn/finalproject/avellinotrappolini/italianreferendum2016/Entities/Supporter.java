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
    private String name;
    private int yesPolsMentioned;
    private int noPolsMentioned;
    private int yesCostructionsUsed;
    private int noCostructionsUsed;
    private int yesExpressionsUsed;
    private int noExpressionsUsed;
    private Boolean isAYesPol;
    private Boolean isANoPol;

    public Supporter(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.yesPolsMentioned = 0;
        this.noPolsMentioned = 0;
        this.yesCostructionsUsed =0;
        this.noCostructionsUsed = 0;
        this.yesExpressionsUsed = 0;
        this.noExpressionsUsed = 0;
        this.isAYesPol = Boolean.FALSE;
        this.isANoPol = Boolean.FALSE;
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

    public int getYesCostructionsUsed() {
        return yesCostructionsUsed;
    }

    public int getNoCostructionsUsed() {
        return noCostructionsUsed;
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

    public String getName() {
        return name;
    }

    public void setYesPolsMentioned(int yesPolsMentioned) {
        this.yesPolsMentioned = yesPolsMentioned;
    }

    public void setNoPolsMentioned(int noPolsMentioned) {
        this.noPolsMentioned = noPolsMentioned;
    }

    public void setYesCostructionsUsed(int yesCostructionsUsed) {
        this.yesCostructionsUsed = yesCostructionsUsed;
    }

    public void setNoCostructionsUsed(int noCostructionsUsed) {
        this.noCostructionsUsed = noCostructionsUsed;
    }

    public void setYesExpressionsUsed(int yesExpressionsUsed) {
        this.yesExpressionsUsed = yesExpressionsUsed;
    }

    public void setNoExpressionsUsed(int noExpressionsUsed) {
        this.noExpressionsUsed = noExpressionsUsed;
    }

    public void setIsAYesPol(Boolean isAYesPol) {
        this.isAYesPol = isAYesPol;
    }

    public void setIsANoPol(Boolean isANoPol) {
        this.isANoPol = isANoPol;
    }
}
