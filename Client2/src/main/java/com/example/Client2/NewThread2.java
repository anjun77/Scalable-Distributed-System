package com.example.Client2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class NewThread2 extends Thread{

    public ApiClient apiClient;
    public SkiersApi skiersApi;
    private static final int MaxAttempt = 5;
    private int phaseNum;
    private int startSkierId;
    private int endSkierId;
    private int liftId;
    private int startTime;
    private int endTime;
    private int postRequestsForEachThread;
    private String serverAddress;
    private AtomicInteger success;
    private AtomicInteger unsuccessful;
    private CountDownLatch latch;
    private CountDownLatch curLatch;

  private Queue<PostRecord> recordList;
    private static final int RESORT_ID = 1;
    private static final String SEASON_ID = "2021";
    private static final String DAY_ID = "7";


    public NewThread2(int phaseNum, int startSkierId, int endSkierId, int liftId, int startTime,
        int endTime, int postRequestsForEachThread, String serverAddress, AtomicInteger success,
        AtomicInteger unsuccessful, CountDownLatch latch, CountDownLatch curLatch, Queue<PostRecord> recordList) {
      this.phaseNum = phaseNum;
      this.startSkierId = startSkierId;
      this.endSkierId = endSkierId;
      this.liftId = liftId;
      this.startTime = startTime;
      this.endTime = endTime;
      this.postRequestsForEachThread = postRequestsForEachThread;
      this.serverAddress = serverAddress;
      this.success = success;
      this.unsuccessful = unsuccessful;
      this.latch = latch;
      this.curLatch = curLatch;
      this.recordList = recordList;
    }

  @Override
    public void run() {
      apiClient = new ApiClient();
      //apiClient.setBasePath(this.serverAddress + "/CS6650-A1_war/");
      apiClient.setBasePath(this.serverAddress + "/Server_war/");

      skiersApi = new SkiersApi(apiClient);

      for (int i = 0; i < postRequestsForEachThread; i++) {
        LiftRide liftRide = new LiftRide();
        int randomLiftId = ThreadLocalRandom.current().nextInt(1, this.liftId + 1);
        int randomTime = ThreadLocalRandom.current().nextInt(this.startTime, this.endTime + 1);
        int randomSkierId = ThreadLocalRandom.current()
            .nextInt(this.startSkierId, this.endSkierId + 1);
        liftRide.setLiftID(randomLiftId);
        liftRide.setTime(randomTime);

        for (int attempt = 0; attempt < MaxAttempt; attempt++) {
          long startTimeStamp = System.currentTimeMillis();
          try {
//            ApiResponse<Void> apiResponse = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, this.RESORT_ID, this.SEASON_ID,
//                this.DAY_ID, randomSkierId);
            skiersApi.writeNewLiftRideWithHttpInfo(liftRide, this.RESORT_ID, this.SEASON_ID,
                this.DAY_ID, randomSkierId);
            this.success.getAndIncrement();
            long endTimeStamp = System.currentTimeMillis();
            recordList
                .add(new PostRecord(startTimeStamp, RequestType.POST, endTimeStamp - startTimeStamp,
                    200));
            break;
          } catch (ApiException e) {
            long endTimeStamp = System.currentTimeMillis();
            e.printStackTrace();
            this.unsuccessful.getAndIncrement();
            recordList
                .add(new PostRecord(startTimeStamp, RequestType.POST, endTimeStamp - startTimeStamp,
                    e.getCode()));
          }
        }
      }
      latch.countDown();
      curLatch.countDown();
  }
}
