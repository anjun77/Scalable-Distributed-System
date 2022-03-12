package com.example.Client2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main2 {

    public static void main(String[] args) throws InterruptedException {

        Client2 client = new Client2();
        client.parameters(args);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger unsuccessful = new AtomicInteger(0);
        int numLifts = client.getNumLifts();
        int numSkiers = client.getNumSkiers();
        Queue<PostRecord> recordList = new ConcurrentLinkedDeque<>();
        long start = System.currentTimeMillis();

        //phase 1 start
        int threadsForPhase1 = client.getNumThreads() / 4;
        //int numSkiersId = (int) (client.getNumSkiers() / threadsForPhase1);
        int postRequestsForEachThread = (int) ((client.getNumRuns() * 0.2) * (client.getNumSkiers() / (threadsForPhase1)));
        double percentCompleted1 = 0.2;
        CountDownLatch latch1 = new CountDownLatch(
            (int) (threadsForPhase1 * percentCompleted1));
        Phase2 phase1 = new Phase2(1, threadsForPhase1, numSkiers, numLifts, 1, 90, postRequestsForEachThread,
            latch1, success, unsuccessful, client.getServerAddress(), recordList);
        phase1.startPhase();
//        phase1.waitTime();

        //phase 2 start
        int threadsForPhase2 = client.getNumThreads();
        //numSkiersId = (int) (client.getNumSkiers() / threadsForPhase2);
        postRequestsForEachThread = (int) (client.getNumRuns() * 0.6) * (client.getNumSkiers() / threadsForPhase2);
        double percentCompleted2 = 0.2;
        CountDownLatch latch2 = new CountDownLatch(
            (int) ((threadsForPhase2) * percentCompleted2));
        Phase2 phase2 = new Phase2(2, threadsForPhase2, numSkiers, numLifts, 91, 360, postRequestsForEachThread,
            latch2, success, unsuccessful, client.getServerAddress(), recordList);

        latch1.await();
        phase2.startPhase();
//        phase2.waitTime();

        //phase 3 start
        int threadsForPhase3 = (int) (client.getNumThreads() * 0.1);
        //numSkiersId = (int) (client.getNumSkiers() / threadsForPhase3);
        postRequestsForEachThread = (int) (0.1 *client.getNumRuns());
        double percentCompleted3 = 0.1;
        CountDownLatch latch3 = new CountDownLatch((int) (threadsForPhase3 * percentCompleted3));
        Phase2 phase3 = new Phase2(3, threadsForPhase3, numSkiers, numLifts, 361, 420, postRequestsForEachThread,
            latch3, success, unsuccessful, client.getServerAddress(), recordList);

        latch2.await();
        phase3.startPhase();
//        phase3.waitTime();

        //wait
        phase1.waitTime();
        phase2.waitTime();
        phase3.waitTime();

        //print the summary
        long end = System.currentTimeMillis();

        System.out.println("Threads: " + client.getNumThreads());
        PrintOut2 print = new PrintOut2(success, unsuccessful, start, end);
        print.Print();
        print.PrintFinalCalculation(recordList);

        //write to CSV
        List<PostRecord> list = new ArrayList<>();
        list.addAll(phase1.getRecordList());
        list.addAll(phase2.getRecordList());
        list.addAll(phase3.getRecordList());

        WriteCSV writeCSV = new WriteCSV();
        try {
//            writeCSV.WriteToCSV("PostRequestRecord", recordList);
            writeCSV.WriteToCSV("PostRequestRecord", list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
