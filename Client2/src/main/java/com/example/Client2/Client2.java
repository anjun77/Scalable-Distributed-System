package com.example.Client2;

import javax.persistence.criteria.CriteriaBuilder.In;

public class Client2 {

  //maximum number of threads to run (numThreads - max 1024)
    private int numThreads;
    //number of skier to generate lift rides for (numSkiers - max 100000), This is effectively the
    // skier’s ID (skierID)
    private int numSkiers;
    //number of ski lifts (numLifts - range 5-60, default 40)
    private int numLifts;
    //mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
    private int numRuns;
    //IP/port address of the server
    private String serverAddress;
    private static final int MaxNumThreads = 1024;
    private static final int MaxNumSkiers = 100000;
    private static final int DefaultNumLifts = 40;
    private static final int MinNumLifts = 5;
    private static final int MaxNumLifts = 60;
    private static final int DefaultNumRuns = 10;
    private static final int MaxNumRuns = 20;

    public Client2() {
      this.numThreads = 0;
      this.numSkiers = 0;
      this.numLifts = DefaultNumLifts;
      this.numRuns = DefaultNumRuns;
      this.serverAddress = "";
    }

  public void parameters(String[] args) {
      int index = 0;
      int val;
      Client2 client = new Client2();

      while (true) {
        if (index >= args.length) {
          break;
        }
        String parameter = args[index];
        String value = args[index + 1];

        switch(parameter) {
          case "numThreads":
            val = Integer.valueOf(value);
            if (val > MaxNumThreads) {
              val = MaxNumThreads;
            }
            this.numThreads = val;
            index += 2;
            break;
          case "numSkiers":
            val = Integer.valueOf(value);
            if (val > MaxNumSkiers) {
              val = MaxNumSkiers;
            }
            this.numSkiers = val;
            index += 2;
            break;
          case "numLifts":
            val = Integer.valueOf(value);
            if (val > MaxNumLifts || val < MinNumLifts) {
              val = DefaultNumLifts;
            }
            this.numLifts = val;
            index += 2;
            break;
          case "numRuns":
            val = Integer.valueOf(value);
            if (val > MaxNumRuns) {
              val = DefaultNumRuns;
            }
            this.numRuns = val;
            index += 2;
            break;
          case "serverAddress":
            this.serverAddress = value;
            index += 2;
            break;
        }
      }
    }

    public int getNumThreads() {
      return numThreads;
    }

    public int getNumSkiers() {
      return numSkiers;
    }

    public int getNumLifts() {
      return numLifts;
    }

    public int getNumRuns() {
      return numRuns;
    }

    public String getServerAddress() {
      return serverAddress;
    }
}
