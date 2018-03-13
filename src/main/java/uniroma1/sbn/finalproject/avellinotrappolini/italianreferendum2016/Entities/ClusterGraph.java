/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities;

import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.CoreDecomposition;
import it.stilo.g.structures.Core;
import it.stilo.g.structures.WeightedUndirectedGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriele
 */
public class ClusterGraph {

    private WeightedUndirectedGraph g;
    private Set<Set<Integer>> comps;
    private Core core;
    private ArrayList<String> labels;

    public ClusterGraph(WeightedUndirectedGraph g, ArrayList<String> labels) {

        this.g = g;
        
        this.labels = labels;
        
        int worker = (int) (Runtime.getRuntime().availableProcessors());

        int[] all = new int[g.size];
        for (int i = 0; i < g.size; i++) {
            all[i] = i;
        }

        try {
            this.comps = ConnectedComponents.rootedConnectedComponents(g, all, worker);

            this.core = CoreDecomposition.getInnerMostCore(g, worker);
        } catch (InterruptedException ex) {
            this.core = null;
            ex.printStackTrace();
        }
    }
    
    public ArrayList<String> getWords(ArrayList<Integer> nodes){
        ArrayList<String> nodeNames = new ArrayList<String>();
        
        for(int node : nodes){
            nodeNames.add(labels.get(node));
        }
        
        return nodeNames;
    }

    public WeightedUndirectedGraph getG() {
        return g;
    }

    public Set<Set<Integer>> getComps() {
        return comps;
    }

    public Core getCore() {
        return core;
    }
    
    
}
