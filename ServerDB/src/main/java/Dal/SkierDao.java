package Dal;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SkierDao {
    private static String REDIS_HOST = "172.31.21.25";
    private static int REDIS_PORT = 6379;
    private static JedisPool jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);

    public SkierDao() {
    }

    public int getDayVertical(int resortID, int seasonID, int dayID, int skierID) {
        int dayVertical = 0;
        String keyString = String.valueOf(skierID);
        try (Jedis jedis = jedisPool.getResource()) {
            long length = jedis.llen(keyString);
            for (long index = 0; index < length; index++) {
                String liftRide = jedis.lindex(keyString, index);
                String[] parts = liftRide.split(",");
                if (resortID == Integer.valueOf(parts[0]) && seasonID == Integer.valueOf(parts[1])
                    && dayID == Integer.valueOf(parts[2])) {
                    dayVertical += Integer.valueOf(parts[-1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dayVertical;
    }

    public int getTotalVertical(int skierID) {
        int totalVertical = 0;
        String keyString = String.valueOf(skierID);
        try (Jedis jedis = jedisPool.getResource()) {
            long length = jedis.llen(keyString);
            for (long index = 0; index < length; index++) {
                String liftRide = jedis.lindex(keyString, index);
                String[] parts = liftRide.split(",");
                totalVertical += Integer.valueOf(parts[-1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalVertical;
    }
}
