import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class Consumer {
    private static Map<Integer, List<JsonObject>> map;
    private static String HOST = "172.31.18.40";
    private static int PORT = 5672;
    private static String USERNAME = "user";
    private static String PASSWORD = "password";
    private static String QUEUE_NAME = "LiftRide";
    private static Gson gson;
    private static int ThreadsNum = 10;

  public static void main(String[] args) throws IOException, TimeoutException {
      map = new ConcurrentHashMap<>();
      gson = new Gson();
      ConnectionFactory connectionFactory = new ConnectionFactory();
      connectionFactory.setHost(HOST);
      connectionFactory.setPort(PORT);
      connectionFactory.setUsername(USERNAME);
      connectionFactory.setPassword(PASSWORD);
      Connection connection = connectionFactory.newConnection();

      Runnable runnable = new Runnable() {
          @Override
          public void run() {
              try {
                  Channel channel = connection.createChannel();
                  channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                  channel.basicQos(1);
                  DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                      String message = new String(delivery.getBody(), "UTF-8");
                      //get skierId and put lift ride into map
                      JsonObject liftRecord = gson.fromJson(message, JsonObject.class);
                      Integer skierID = Integer.valueOf(String.valueOf(liftRecord.get("SkierID")));
                      if (!map.containsKey(skierID)) {
                        map.put(skierID, new ArrayList<>());
                      }
                      map.get(skierID).add(liftRecord);
                      channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                  };
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
                } catch (Exception e) {
                    e.printStackTrace();
              }
            }
          };

          for (int i = 0; i < ThreadsNum; i++) {
              Thread thread = new Thread(runnable);
              thread.start();
          }
        }
}
