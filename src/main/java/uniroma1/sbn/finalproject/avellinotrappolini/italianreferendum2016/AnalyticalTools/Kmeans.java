/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.TweetWord;

/**
 *
 * @author giova
 */
public class Kmeans {

    public static int[] computeKmeans(ArrayList<TweetWord> wordsInfo, int k, int maxIt) {

        if (k < 2) {
            throw new IllegalArgumentException("Invalid number of Clusters: " + k);
        }

        if (maxIt <= 0) {
            throw new IllegalArgumentException("Invalid number of iterations: " + maxIt);
        }

        int n = wordsInfo.size(); // number of points
        int m = wordsInfo.get(0).getTimeSeries().length; // number of dimensions

        double[][] data = getData(wordsInfo);
        double[][] centroids = initializeCentroidspp(data, k, m);
        int[] memberships = new int[n];

        int i = 0;
        boolean areCentroidsChanged = Boolean.TRUE; // poi rimuovilo, lo tengo ora cosi non rompe la palle
        while (areCentroidsChanged && i < maxIt) {

            memberships = assignMembership(data, centroids);
            double[][] newCentroids = updateCentroids(data, memberships, k, m);

            areCentroidsChanged = computeMagnitudeUpdate(newCentroids, centroids);
            centroids = newCentroids.clone();

            i++;

        }
        if (i >= maxIt) {
            System.out.println("MaxIter reached!");
        } else {
            System.out.println("Eps reached. Num iter = " + i);
        }

        return memberships;

    }

    private static double[][] getData(ArrayList<TweetWord> wordsInfo) {

        int n = wordsInfo.size();
        int m = wordsInfo.get(0).getTimeSeries().length;
        double[][] data = new double[n][m];

        int i = 0;
        for (TweetWord elem : wordsInfo) {

            data[i] = elem.getBinaryRep();
            i++;

        }
        return data;
    }

    private static double[][] initializeCentroids(double[][] data, int k, int m) {

        double[][] centroids = new double[k][m];

        Random rand = new Random();
        rand.setSeed(123);
        int[] indices = new int[k];

        for (int index = 0; index < k; index++) {

            int newIndex = rand.nextInt(data.length);
            while (ArrayUtils.contains(indices, newIndex)) {
                newIndex = rand.nextInt(data.length);
            }

            indices[index] = newIndex;
            centroids[index] = data[newIndex];
        }

        return centroids;

    }

    private static double[][] initializeCentroidspp(double[][] data, int k, int m) {

        double[][] centroids = new double[k][m];

        double[] weights = null;
        Arrays.fill(weights, 1);

        for (int i = 0; i < k; i++) {

            // pick a point in a weighted fashion
            int newIndex = selectRandomWeightedIndex(weights);
            // add it to the set of centroids
            centroids[i] = data[newIndex];

            for (int idx = 0; idx < data.length; idx++) {

                double maxDist = 1000000;

                for (int c = 0; c < i + 1; c++) {

                    double centrDist = computeDistance(data[idx], centroids[c]);
                    if (centrDist < maxDist) {

                        maxDist = centrDist;

                    }
                    weights[idx] = Math.pow(maxDist, 2);

                }

            }

        }

        return centroids;

    }

    private static int selectRandomWeightedIndex(double[] weights) {

        Random rand = new Random();
        rand.setSeed(123);

        // Compute the total weight of all items together
        double totalWeight = 0.0d;
        for (double i : weights) {

            totalWeight += i;
        }
        // Now choose a random item
        int randomIndex = -1;
        double random = rand.nextDouble() * totalWeight;
        for (int i = 0; i < weights.length; ++i) {
            random -= weights[i];
            if (random <= 0.0d) {
                randomIndex = i;
                break;
            }
        }

        return randomIndex;

    }

    private static int[] assignMembership(double[][] data, double[][] centroids) {

        int[] memberships = new int[data.length];

        for (int i = 0; i < data.length; i++) {

            double maxDist = 10000; // numero esageratamente largo, non è molto elegante come cosa però
            int bestCentroid = 0;

            for (int centroid = 0; centroid < centroids.length; centroid++) {
                double centrDist = computeDistance(data[i], centroids[centroid]);
                if (centrDist < maxDist) {
                    maxDist = centrDist;
                    bestCentroid = centroid;
                }

            }
            memberships[i] = bestCentroid;
        }

        return memberships;

    }

    private static double computeDistance(double[] vec1, double[] vec2) {

        int n = vec1.length;
        double dist = 0;

        for (int i = 0; i < n; i++) {

            dist += Math.pow(vec1[i] - vec2[i], 2);

        }

        dist = Math.sqrt(dist);

        return dist;

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

    private static double[] addVectors(double[] vec1, double[] vec2) {

        int n = vec1.length;
        double[] result = new double[n];

        for (int i = 0; i < n; i++) {

            result[i] = vec1[i] + vec2[i];

        }

        return result;
    }

    private static double[] averageVec(double[] vec, int divisor) {

        for (int i = 0; i < vec.length; i++) {

            vec[i] /= (double) divisor;
        }
        return vec;
    }

    private static boolean computeMagnitudeUpdate(double[][] newCentroids, double[][] oldCentroids) {

        double magnitude = 0;
        for (int i = 0; i < newCentroids.length; i++) {

            magnitude += computeDistance(newCentroids[i], oldCentroids[i]);

            if (magnitude > 0) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
