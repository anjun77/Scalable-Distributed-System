public class LiftRide {
    private int resortID;
    private int seasonID;
    private int dayID;
    private int skierID;
    private int liftID;
    private int time;
    private int waitTime;
    private int vertical;

    public LiftRide(int resortID, int seasonID, int dayID, int skierID, int liftID, int time,
        int waitTime, int vertical) {
      this.resortID = resortID;
      this.seasonID = seasonID;
      this.dayID = dayID;
      this.skierID = skierID;
      this.liftID = liftID;
      this.time = time;
      this.waitTime = waitTime;
      this.vertical = vertical;
    }

    public int getResortID() {
      return resortID;
    }

    public int getSeasonID() {
      return seasonID;
    }

    public int getDayID() {
      return dayID;
    }

    public int getSkierID() {
      return skierID;
    }

    public int getLiftID() {
      return liftID;
    }

    public int getTime() {
      return time;
    }

    public int getWaitTime() {
      return waitTime;
    }

    public int getVertical() {
      return vertical;
    }

    @Override
    public String toString() {
      return "Dal.LiftRide{" +
          "resortID=" + resortID +
          ", seasonID=" + seasonID +
          ", dayID=" + dayID +
          ", skierID=" + skierID +
          ", liftID=" + liftID +
          ", time=" + time +
          ", waitTime=" + waitTime +
          ", vertical=" + vertical +
          '}';
    }
}
