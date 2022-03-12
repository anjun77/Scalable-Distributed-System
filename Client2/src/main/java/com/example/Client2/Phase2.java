package com.example.Client2;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Phase2 {

    private int phaseNumber;
    private int numThreads;
    private int numSkiers;
    private int numLifts;
    private int startTime;
    private int endTime;
    private int numPostReq;
    private CountDownLatch latch;
    private CountDownLatch curLatch;
    private AtomicInteger successCount;
    private AtomicInteger unsuccessfulCount;
    private String serverAddress;
    private Queue<PostRecord> recordList;

    public Phase2(int phaseNumber, int numThreads, int numSkiers, int numLifts, int startTime, int endTime,
        int numPostReq, CountDownLatch latch, AtomicInteger successCount, AtomicInteger unsuccessfulCount,
        String serverAddress, Queue<PostRecord> recordList) {
      this.phaseNumber = phaseNumber;
      this.numThreads = numThreads;
      this.numSkiers = numSkiers;
      this.numLifts = numLifts;
      this.startTime = startTime;
      this.endTime = endTime;
      this.numPostReq = numPostReq;
      this.latch = latch;
      this.curLatch = new CountDownLatch(numThreads);
      this.successCount = successCount;
      this.unsuccessfulCount = unsuccessfulCount;
      this.serverAddress = serverAddress;
      this.recordList = recordList;
    }

    public void startPhase() {
      int skierIdRange = numSkiers / numThreads;
      for (int i = 0; i < numThreads; i++) {
        int startSkierId = i * skierIdRange + 1;
        int endSkierId;
        if (i == numThreads - 1) {
          endSkierId = numSkiers;
        } else {
          endSkierId = skierIdRange * (i + 1);
        }
        Thread thread = new NewThread2(phaseNumber, startSkierId, endSkierId, numLifts, startTime,
            endTime, numPostReq, serverAddress, successCount, unsuccessfulCount,
            latch, curLatch, recordList);
        thread.start();
      }
    }

    public void waitTime() throws InterruptedException {
      curLatch.await();
    }

    public Queue<PostRecord> getRecordList() {
      return this.recordList;
    }
}
