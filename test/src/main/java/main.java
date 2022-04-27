import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class main {

  private static String REDIS_HOST = "54.227.103.86";
  private static int REDIS_PORT = 6379;
  private static String REDIS_PASSWORD = "password";
  private static JedisPoolConfig config = new JedisPoolConfig();
  private static String resortID = "10";
  private static String seasonID = "2021";
  private static String dayID = "7";

  public static void main(String[] args) throws InterruptedException {
    JedisPool jedisPool = new JedisPool(config, REDIS_HOST, REDIS_PORT, Protocol.DEFAULT_TIMEOUT, REDIS_PASSWORD);
    try {
      Jedis jedis = jedisPool.getResource();
      String key = resortID + "/" + seasonID + "/" + dayID;
      long uniqueCount = jedis.scard(key);
      System.out.println(uniqueCount);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
