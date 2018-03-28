/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.AnalyticalTools;

import it.stilo.g.structures.WeightedGraph;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Gabriele
 */
public class ComunityLPA implements Runnable {

    private static final Logger logger = LogManager.getLogger(ComunityLPA.class);

    private static Random rnd;
    private WeightedGraph g;

    private int chunk;
    private int runner;
    private CountDownLatch barrier;

    private int[] labels;
    private int[] list = null;

    private ComunityLPA(WeightedGraph g, CountDownLatch cb, int[] labels, int chunk, int runner) {
        this.g = g;
        this.runner = runner;
        this.barrier = cb;
        this.labels = labels;
        this.chunk = chunk;
    }

    private boolean initList() {
        if (list == null) {
            // Partitioning over worker
            list = new int[(g.in.length / runner) + runner];

            int j = 0;

            for (int i = chunk; i < g.in.length; i += runner) {

                if (g.in[i] != null) {

                    list[j] = i;
                    j++;
                } else if (g.out[i] == null) {
                    labels[i] = -1;
                    System.out.println("NODO INUTILE!!!");;
                }
            }

            list = Arrays.copyOf(list, j);

            //Shuffle
            for (int i = 0; i < list.length; i++) {
                for (int z = 0; z < 10; z++) {
                    int randomPosition = rnd.nextInt(list.length);
                    int temp = list[i];
                    list[i] = list[randomPosition];
                    list[randomPosition] = temp;
                }
            }

            return true;
        }
        return false;
    }

    public void run() {
        if (!initList()) {
            for (int i = 0; i < list.length; i++) {
                int[] near = g.in[list[i]];
                int[] nearLabs = new int[near.length];
                for (int x = 0; x < near.length; x++) {
                    nearLabs[x] = labels[near[x]];
                }
                if(bestLabel(nearLabs) != -1)
                    labels[list[i]] = bestLabel(nearLabs);
            }
        }
        barrier.countDown();
    }

//    public static int bestLabel(int[] neighborhood) {
//        Arrays.sort(neighborhood);
//        int best = -1;
//        int maxCount = -1;
//        int counter = 0;
//        int last = -1;
//        for (int i = 0; i < neighborhood.length; i++) {
//            if (maxCount > (neighborhood.length - i)) {
//                break;
//            }
//
//            if (neighborhood[i] == last) {
//                counter++;
//                if (counter > maxCount && last != 0) {
//                    maxCount = counter;
//                    best = last;
//                }
//            } else {
//                counter = 0;
//                last = neighborhood[i];
//            }
//        }
//
//        return best;
//    }
    public static int bestLabel(int[] neighborhood) {
        int yes = 0;
        int no  = 0;
        
        for (int i = 0; i < neighborhood.length; i++) {
            if (neighborhood[i] == 1) {
                yes++;
            } else if(neighborhood[i] == 2){
                no++;
            }
        }

        if (yes == 0 && no == 0) {
            return -1;
        } else {
            if(yes > no)
                return yes;
            else
                return no;
        }
    }

    public static int[] compute(final WeightedGraph g, double threshold, int runner, int[] initLabels) {

        ComunityLPA.rnd = new Random(System.currentTimeMillis());

        int[] labels = initLabels;
        int[] newLabels = labels;
        int iter = 0;

        long time = System.nanoTime();
        CountDownLatch latch = null;

        ComunityLPA[] runners = new ComunityLPA[runner];

        for (int i = 0; i < runner; i++) {
            runners[i] = new ComunityLPA(g, latch, labels, i, runner);
        }

        ExecutorService ex = Executors.newFixedThreadPool(runner);

        do {
            iter++;
            labels = newLabels;
            newLabels = Arrays.copyOf(labels, labels.length);
            latch = new CountDownLatch(runner);

            //Label Propagation
            for (int i = 0; i < runner; i++) {
                runners[i].barrier = latch;
                runners[i].labels = newLabels;
                ex.submit(runners[i]);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.debug(e);
            }

        } while (smoothEnd(labels, newLabels, iter, threshold));

        ex.shutdown();

        logger.info(((System.nanoTime() - time) / 1000000000d) + "\ts");
        return labels;
    }

    private static boolean smoothEnd(int[] labels, int[] newLabels, int iter, double threshold) {
        if (iter < 2) {
            return true;
        }

        int k = 3;

        if (iter > k) {
            int equality = 0;

            for (int i = 0; i < labels.length; i++) {
                if (labels[i] == newLabels[i]) {
                    equality++;
                }
            }
            double currentT = (equality / ((double) labels.length));

            return !(currentT >= threshold);
        }
        return !Arrays.equals(labels, newLabels);
    }
}
