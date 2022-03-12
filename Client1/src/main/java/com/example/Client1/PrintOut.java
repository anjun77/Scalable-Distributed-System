package com.example.Client1;

public class PrintOut {

  private Count count;
  private long startTime;
  private long endTime;

  public PrintOut(Count count, long startTime, long endTime) {
    this.count = count;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public void Print() {
    System.out.println("==========================================");
    System.out.println("Summary for Client Part 1");
    System.out.println("All threads from all phases are completed!");
    System.out.println("Number of successful requests sent: " + count.getSuccess());
    System.out.println("Number of unsuccessful requests sent: " + count.getUnsuccessful());
    System.out.println("Wall time: " + (endTime - startTime));
    System.out.println("The total throughput in requests per second (total number of requests / wall time) is : " +
        (count.getSuccess() + count.getUnsuccessful()) / (double) (endTime - startTime) * 1000);
  }
}

