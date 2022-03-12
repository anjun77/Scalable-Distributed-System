package com.example.Client2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class PrintOut2 {

    private AtomicInteger successCount;
    private AtomicInteger unsuccessfulCount;
    private long startTime;
    private long endTime;

  public PrintOut2(AtomicInteger successCount,
      AtomicInteger unsuccessfulCount, long startTime, long endTime) {
    this.successCount = successCount;
    this.unsuccessfulCount = unsuccessfulCount;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public void Print() {
      System.out.println("==========================================");
      System.out.println("All threads from all phases are completed!");
      System.out.println("Number of successful requests sent: " + successCount);
      System.out.println("Number of unsuccessful requests sent: " + unsuccessfulCount);
      System.out.println("Wall time: " + (endTime - startTime));
      System.out.println("The total throughput in requests per second (total number of requests / wall time) is : " +
          (successCount.get() + unsuccessfulCount.get()) / (double) (endTime - startTime) * 1000);
  }

  public void PrintFinalCalculation(Queue<PostRecord> postRecordList) {
    // Print out the calculated results
    List<Long> response = sortResponse(postRecordList);

    long mean = getMean(response);
    long median = getMedian(response);
    long p99 = getPercentile(response, 99);
    long min = getMin(response);
    long max = getMax(response);
    int numRequests = successCount.get() + unsuccessfulCount.get();
    long wallTime = endTime - startTime;


    System.out.println("==========================================");
    System.out.println("Summary for Client Part 2");
    System.out.println("Mean response time is: " + mean);
    System.out.println("Median response time is: " + median);
    System.out.println("The total throughput in requests per second (total number of requests / wall time) is : " +
        1000 * numRequests / (double) wallTime);
    System.out.println("99th percentile response time: " + p99);
    System.out.println("Min response time: " + min);
    System.out.println("Max response time: " + max);
  }

  private List<Long> sortResponse(Queue<PostRecord> postRecordList) {
    List<Long> responseList = new ArrayList<>();
    for (PostRecord postRecord : postRecordList) {
      responseList.add(postRecord.getLatency());
    }
    Collections.sort(responseList);
    return responseList;
  }

  private long getMean(List<Long> responseList) {
    long totalSum = 0;
    for (long responseTime : responseList) {
      totalSum += responseTime;
    }
    long mean = totalSum / responseList.size();
    return mean;
  }

  private long getMedian(List<Long> responseList) {
    return getPercentile(responseList, 50);
  }

  private long getPercentile(List<Long> responseList, int percent) {
    int index = responseList.size() * percent / 100;
    return responseList.get(index);
  }

  private long getMin(List<Long> responseList) {
    return responseList.get(0);
  }

  private long getMax(List<Long> responseList) {
    return responseList.get(responseList.size() - 1);
  }
}
