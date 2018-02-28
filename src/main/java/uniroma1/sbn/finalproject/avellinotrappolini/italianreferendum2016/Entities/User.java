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
public class User {
    private String name;
    private String surname;
    private String vote;
    private String politicGroup;
    private String twitterId;

    public User(String name, String surname, String politicGroup, String vote, String twitterId) {
        this.name = name;
        this.surname = surname;
        this.politicGroup = politicGroup;
        this.twitterId = twitterId;
        if(vote.equals("yes") || vote.equals("no"))
            this.vote = vote;
    }
    
    
}
