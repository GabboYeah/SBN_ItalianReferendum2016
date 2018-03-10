/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools;

import java.util.ArrayList;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetWord;

/**
 *
 * @author giova
 */
public class Kmeans {

    public static int[] ComputeKmeans(ArrayList<TweetWord> wordsInfo, int k, int maxIt) {
        if (k < 2) {
            throw new IllegalArgumentException("Invalid number of Clusters: " + k);
        }

        if (maxIt <= 0) {
            throw new IllegalArgumentException("Invalid number of iterations: " + maxIt);
        }

        int n = wordsInfo.size(); // number of points
        int m = wordsInfo.get(0).getSize(); // number of dimensions

        double[][] data = getData(wordsInfo);
        double[][] centroids = initializeCentroids(data, k, m);
        int[] memberships = new int[n];

        int i = 0;
        double diff = 1; // poi rimuovilo, lo tengo ora cosi non rompe la palle
        while (diff != 0 || i < maxIt) {

            memberships = assignMembership(data, centroids);
            double[][] newCentroids = updateCentroids(data, memberships, k, m);
            
            diff = computeMagnitudeUpdate(newCentroids, centroids);
            centroids = newCentroids.clone();
            
            i++;

        }
        

        return memberships;

    }
    
    private static double computeMagnitudeUpdate(double[][] newCentroids, double[][] oldCentroids){
        
        double magnitude = 0;
        for(int i = 0; i < newCentroids.length; i++){
            
            magnitude += computeDistance(newCentroids[i], oldCentroids[i]);
            
        }
        
        return magnitude;
        
    }

    private static double[][] updateCentroids(double[][] data, int[] membership, int k, int m) {

        double[][] centroids = new double[k][m];

        for (int c = 0; c < k; c++) {

            double[] centroid = new double[m];
            int numerosity = 0;
            for (int i = 0; i < data.length; i++) {

                if (membership[i] == c) {

                    numerosity++;
                    centroid = addVectors(centroid, data[i]);

                }

            }
            centroid = averageVec(centroid, numerosity);
            centroids[c] = centroid;
        }
        return centroids;

    }

    private static double[] averageVec(double[] vec, int divisor) {

        for (int i = 0; i < vec.length; i++) {

            vec[i] /= (double) divisor;
        }
        return vec;
    }

    private static double[] addVectors(double[] vec1, double[] vec2) {

        int n = vec1.length;
        double[] result = new double[n];

        for (int i = 0; i < n; i++) {

            result[i] = vec1[i] + vec2[i];

        }

        return result;
    }

    private static int[] assignMembership(double[][] data, double[][] centroids) {

        int[] memberships = new int[data.length];

        for (int i = 0; i < data.length; i++) {

            double dist = 10000; // numero esageratamente largo, non è molto elegante come cosa però
            int minDist = 0;

            for (int j = 0; j < centroids.length; j++) {
                double newDist = computeDistance(data[i], centroids[j]);
                if (newDist < dist) {
                    dist = newDist;
                    minDist = j;
                }

            }
            memberships[i] = minDist;
        }

        return memberships;

    }

    private static double[][] initializeCentroids(double[][] data, int k, int m) {

        double[][] centroids = new double[k][m];

        Random rand = new Random();
        int[] indices = new int[k];

        for (int index = 0; index < k; index++) {

            int newIndex = rand.nextInt(data.length);
            while (ArrayUtils.contains(indices, newIndex)) {
                newIndex = rand.nextInt(data.length);
            }

            indices[index] = newIndex;
        }

        for (int i = 0; i < k; i++) {

            centroids[i] = data[indices[i]];

        }

        return centroids;

    }

    private static double[][] getData(ArrayList<TweetWord> wordsInfo) {

        int n = wordsInfo.size();
        int m = wordsInfo.get(0).getSize();
        double[][] data = new double[n][m];

        int i = 0;
        for (TweetWord elem : wordsInfo) {

            data[i] = elem.getBinaryRep();
            i++;

        }
        return data;
    }

    private static double computeDistance(double[] vec1, double[] vec2) {

        int n = vec1.length;
        double dist = 0;

        for (int i = 0; i < n; i++) {

            dist += Math.pow(vec1[i] - vec1[i], 2);

        }

        dist = Math.sqrt(dist);

        return dist;

    }

}
