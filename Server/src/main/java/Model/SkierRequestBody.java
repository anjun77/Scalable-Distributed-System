package Model;

public class SkierRequestBody {

  private Integer time;
  private Integer liftID;
  private Integer waitTime;

  public SkierRequestBody(Integer time, Integer liftID, Integer waitTime) {
    this.time = time;
    this.liftID = liftID;
    this.waitTime = waitTime;
  }

  public Integer getTime() {
    return time;
  }

  public void setTime(Integer time) {
    this.time = time;
  }

  public Integer getLiftID() {
    return liftID;
  }

  public void setLiftID(Integer liftID) {
    this.liftID = liftID;
  }

  public Integer getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(Integer waitTime) {
    this.waitTime = waitTime;
  }

  @Override
  public String toString() {
    return "SkierRequestBody: {" +
        "time is " + time +
        ", liftID is " + liftID +
        ", waitTime is " + waitTime +
        "}";
  }
}
