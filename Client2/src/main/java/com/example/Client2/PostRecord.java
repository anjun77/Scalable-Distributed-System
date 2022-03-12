package com.example.Client2;

public class PostRecord {

    private long startTime;
    private RequestType type;
    private long latency;
    private int responseCode;

    public PostRecord(long startTime, RequestType type, long latency, int responseCode) {
      this.startTime = startTime;
      this.type = type;
      this.latency = latency;
      this.responseCode = responseCode;
    }

    public long getStartTime() {
      return startTime;
    }

    public void setStartTime(long startTime) {
      this.startTime = startTime;
    }

    public RequestType getType() {
      return type;
    }

    public void setType(RequestType type) {
      this.type = type;
    }

    public long getLatency() {
      return latency;
    }

    public void setLatency(long latency) {
      this.latency = latency;
    }

    public int getResponseCode() {
      return responseCode;
    }

    public void setResponseCode(int responseCode) {
      this.responseCode = responseCode;
    }

    @Override
    public String toString() {
      return this.startTime + "," + this.getType() + "," + this.latency + "," + this.responseCode;
    }
}
