import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class Consumer {
    private static String RMQ_HOST = "172.31.1.65";
    private static int RMQ_PORT = 5672;
    private static String USERNAME = "user";
    private static String PASSWORD = "password";
    private static String QUEUE_NAME_RESORT = "ResortQueue";
    private static String REDIS_HOST = "172.31.0.129";
    private static int REDIS_PORT = 6379;
    private static Gson gson;
    private static int ThreadsNum = 256;
    private static JedisPool pool;


  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
      gson = new Gson();
      ConnectionFactory connectionFactory = new ConnectionFactory();
      connectionFactory.setHost(RMQ_HOST);
      connectionFactory.setPort(RMQ_PORT);
      connectionFactory.setUsername(USERNAME);
      connectionFactory.setPassword(PASSWORD);
      Connection connection = connectionFactory.newConnection();

//      JedisPoolConfig config = new JedisPoolConfig();
//      config.setMaxTotal(512);
//      config.setMaxWait(Duration.ofMillis(2000));
//      config.setTestOnBorrow(true);
//      config.setBlockWhenExhausted(true);
//
      pool = new JedisPool(REDIS_HOST, REDIS_PORT);

      Runnable runnable = new Runnable() {
          @Override
          public void run() {
              try (Jedis jedis = pool.getResource()) {
                  Channel channel = connection.createChannel();
                  channel.queueDeclare(QUEUE_NAME_RESORT, false, false, false, null);
                  channel.basicQos(1);

                  DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                      String message = new String(delivery.getBody(), "UTF-8");
                      JsonObject liftRecord = gson.fromJson(message, JsonObject.class);
                      String skierID = String.valueOf(liftRecord.get("SkierID"));
                      String seasonID = String.valueOf(liftRecord.get("SeasonID"));
                      String dayID = String.valueOf(liftRecord.get("DayID"));
                      String resortID = String.valueOf(liftRecord.get("ResortID"));
                      String liftID = String.valueOf(liftRecord.get("LiftID"));
                      String time = String.valueOf(liftRecord.get("time"));
                      String waitTime = String.valueOf(liftRecord.get("waitTime"));
                      String vertical = String.valueOf(liftRecord.get("vertical"));

                      String value =
                          skierID + "," + liftID + "," + resortID + "," + seasonID + ","
                              + time + "," + waitTime + "," + vertical;
                      jedis.lpush(dayID, value);
//                      try {
//                          TimeUnit.SECONDS.sleep(10);
//                      } catch (InterruptedException e) {
//                          e.printStackTrace();
//                      }
                      channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                  };
                  channel.basicConsume(QUEUE_NAME_RESORT, false, deliverCallback, consumerTag -> {
                  });
              } catch (IOException e) {
                  Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, e);
              }
          }
      };

      Thread[] threads = new Thread[ThreadsNum];
      for (int i = 0; i < ThreadsNum; i++) {
          threads[i] = new Thread(runnable);
      }
      for (int i = 0; i < ThreadsNum; i++) {
          threads[i].start();
      }
  }
}
