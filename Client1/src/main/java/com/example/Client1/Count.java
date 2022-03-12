package com.example.Client1;

public class Count {

    private int success;
    private int unsuccessful;

    public Count() {
      this.success = 0;
      this.unsuccessful = 0;
    }

    public synchronized void incrementSuccess(int num) {
      this.success += num;
    }

    public synchronized void incrementUnsuccessful(int num) {
      this.unsuccessful += num;
    }

    public int getSuccess() {
      return success;
    }

    public int getUnsuccessful() {
      return unsuccessful;
    }
}
