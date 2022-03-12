package com.example.Client1;

import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        Client client = new Client();
        client.parameters(args);

        Count count = new Count();

        long start = System.currentTimeMillis();
        int numLifts = client.getNumLifts();
        int numSkiers = client.getNumSkiers();

        //phase 1 start
        int threadsForPhase1 = client.getNumThreads() / 4;
        //int numSkiersId = (int) (client.getNumSkiers() / threadsForPhase1);
        int postRequestsForEachThread1 = (int) ((client.getNumRuns() * 0.2) * (client.getNumSkiers() / (threadsForPhase1)));
        double percentCompleted1 = 0.2;
        CountDownLatch latch1 = new CountDownLatch(
            (int) (threadsForPhase1 * percentCompleted1));
        Phase phase1 = new Phase(1, threadsForPhase1, numSkiers, numLifts, 1, 90, postRequestsForEachThread1,
            latch1, count, client.getServerAddress());
        phase1.startPhase();
        //phase1.waitTime();

        //phase 2 start
        int threadsForPhase2 = client.getNumThreads();
        //numSkiersId = (int) (client.getNumSkiers() / threadsForPhase2);
        int postRequestsForEachThread2 = (int) (client.getNumRuns() * 0.6) * (client.getNumSkiers() / threadsForPhase2);
        double percentCompleted2 = 0.2;
        CountDownLatch latch2 = new CountDownLatch(
            (int) ((threadsForPhase2) * percentCompleted2));
        Phase phase2 = new Phase(2, threadsForPhase2, numSkiers, numLifts, 91, 360, postRequestsForEachThread2,
            latch2, count, client.getServerAddress());

        latch1.await();
        phase2.startPhase();
        //phase2.waitTime();

        //phase 3 start
        int threadsForPhase3 = (int) (client.getNumThreads() * 0.1);
        //numSkiersId = (int) (client.getNumSkiers() / threadsForPhase3);
        int postRequestsForEachThread3 = (int) (0.1 * client.getNumRuns());
        double percentCompleted3 = 0.1;
        CountDownLatch latch3 = new CountDownLatch((int) (threadsForPhase3 * percentCompleted3));
        Phase phase3 = new Phase(3, threadsForPhase3, numSkiers, numLifts, 361, 420, postRequestsForEachThread3,
            latch3, count, client.getServerAddress());

        latch2.await();
        phase3.startPhase();
        //phase3.waitTime();

        //wait
        phase1.waitTime();
        phase2.waitTime();
        phase3.waitTime();

        //print the summary
        long end = System.currentTimeMillis();

        System.out.println("Threads: " + client.getNumThreads());
        PrintOut print = new PrintOut(count, start, end);
        print.Print();
    }
}
