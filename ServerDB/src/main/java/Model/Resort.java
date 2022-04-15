package Model;

public class Resort {
    private String seasonID;
    private int totalVertical;

    public Resort(String seasonID, int totalVertical) {
      this.seasonID = seasonID;
      this.totalVertical = totalVertical;
    }

    public String getSeasonID() {
      return seasonID;
    }

    public int getTotalVertical() {
      return totalVertical;
    }
}
