package Server.Servlet;

import Dal.SkierDao;
import Model.LiftRide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
    private ObjectPool<Channel> pool;
    //private BlockingDeque<Channel> pool;
    private static String EXCHANGE_NAME = "LiftRide";
    private static String QUEUE_NAME_RESORT = "ResortQueue";
    private static String QUEUE_NAME_SKIER = "SkierQueue";

    private static int NUM_CHANNELS = 128;
    private EventCountCircuitBreaker eventCountCircuitBreaker;
    private Gson gson = new Gson();

    private static String REDIS_HOST = "172.31.15.7";
    private static int REDIS_PORT = 6379;
    private static String REDIS_PASSWORD = "password";
    private static JedisPoolConfig config = new JedisPoolConfig();

    @Override
    public void init() {
        eventCountCircuitBreaker = new EventCountCircuitBreaker(800, 1, TimeUnit.SECONDS, 600);
        GenericObjectPoolConfig<Channel> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(NUM_CHANNELS);
        config.setMaxIdle(NUM_CHANNELS);
        this.pool = new GenericObjectPool<>(new ChannelFactory(), config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        // /skiers/{skierID}/vertical
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();
        //check we have a URL
        if (urlPath == null || urlPath.isEmpty()) {
            setStatusAsNotFound(response);
            return;
        }
        //check if URL is valid
        String[] urlParts = urlPath.split("/");
        if (isGetUrlValid(urlParts)) {
            setStatusAsValid(response);
            JedisPool jedisPool = new JedisPool(config, REDIS_HOST, REDIS_PORT, Protocol.DEFAULT_TIMEOUT, REDIS_PASSWORD);
            if (handleDayVerticalForASkier(urlParts)) {
                // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
                // get the total vertical for the skier for the specified ski day
                String resortID = urlParts[1];
                String seasonID = urlParts[3];
                String dayID = urlParts[5];
                String skierID = urlParts[7];
                int dayVertical = getDayVertical(jedisPool, resortID, seasonID, dayID, skierID);
                response.getWriter().write("The total vertical for skier " + skierID
                    + " for the specified ski day " + dayID + " is " + dayVertical + " meters.");
            } else {
                // /skiers/{skierID}/vertical
                // get the total vertical for the skier the specified resort.
                // If no season is specified, return all seasons
                int skierID = Integer.parseInt(urlParts[1]);
                int totalVertical = getTotalVertical(jedisPool, skierID);
                response.getWriter().write("The total vertical for skier " + skierID + " is "
                    + totalVertical + " meters.");
            }
        } else {
            setStatusAsInvalid(response);
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();
        //check we have a URL
        if (urlPath == null || urlPath.isEmpty()) {
            setStatusAsNotFound(response);
            return;
        }
        //check if URL is valid
        String[] urlParts = urlPath.split("/");

        if (!isPostUrlValid(urlParts)) {
            setStatusAsInvalid(response);
            return;
        } else {
          try {
              JsonObject liftRecord = new JsonObject();
              LiftRide liftRide = gson.fromJson(request.getReader(), LiftRide.class);
              // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
              liftRecord.addProperty("SkierID", Integer.parseInt(urlParts[7]));
              liftRecord.addProperty("ResortID", Integer.parseInt(urlParts[1]));
              liftRecord.addProperty("SeasonID", Integer.parseInt(urlParts[3]));
              liftRecord.addProperty("DayID", Integer.parseInt(urlParts[5]));
              liftRecord.addProperty("LiftID", liftRide.getLiftID());
              liftRecord.addProperty("waitTime", liftRide.getWaitTime());
              liftRecord.addProperty("time", liftRide.getTime());
              liftRecord.addProperty("vertical", liftRide.getLiftID()*10);
              Channel channel = null;

              try {
                  channel = pool.borrowObject();
                  channel.queueDeclare(QUEUE_NAME_SKIER, false, false, false, null);
                  channel.queueDeclare(QUEUE_NAME_RESORT, false, false, false, null);
                  channel.basicPublish("", QUEUE_NAME_SKIER, null,
                      liftRecord.toString().getBytes(StandardCharsets.UTF_8));
                  channel.basicPublish("", QUEUE_NAME_RESORT, null,
                      liftRecord.toString().getBytes(StandardCharsets.UTF_8));
                  response.getWriter().write(String.valueOf(liftRecord));
                  setStatusAsCreated(response);
              } catch (Exception e) {
                  e.printStackTrace();
                  setStatusAsFailed(response);
              } finally {
                  if (null != channel) {
                    pool.returnObject(channel);
                  }
              }
          } catch (Exception e) {
              e.printStackTrace();
          }
        }
    }

    private void setStatusAsNotFound(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("Not found");
    }

    private void setStatusAsInvalid(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("Invalid parameter");
    }

    private void setStatusAsFailed(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("Failed to connect to RMQ");
    }

    private void setStatusAsValid(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setStatusAsCreated(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    private boolean isGetUrlValid(String[] urlParts) {
        // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        // /skiers/{skierID}/vertical
        if (urlParts.length == 3) {
            boolean wordsMatch = urlParts[2].equals("vertical");
            boolean intMatch = IntHelper.isInteger(urlParts[1]);
            return wordsMatch && intMatch;
        } else if (urlParts.length == 8) {
            return handleDayVerticalForASkier(urlParts);
        } else {
            return false;
        }
    }

    private boolean isPostUrlValid(String[] urlParts) {
        // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        return handleDayVerticalForASkier(urlParts);
    }

    private boolean handleDayVerticalForASkier(String[] urlParts) {
        // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        if (urlParts.length == 8) {
            boolean wordsMatch = (urlParts[2].equals("seasons")
                && urlParts[4].equals("days") && urlParts[6].equals("skiers"));
            boolean intMatch = IntHelper.isInteger(urlParts[1]) && IntHelper.isInteger(urlParts[3])
                && IntHelper.isInteger(urlParts[5]) && IntHelper.isInteger(urlParts[7]);
            return wordsMatch && intMatch;
        } else {
            return false;
        }
    }

    private int getDayVertical(JedisPool pool, String resortID, String seasonID, String dayID, String skierID) {
        try (Jedis jedis = pool.getResource()) {
            long length = jedis.llen(skierID);
            int dayVertical = 0;
            for (long index = 0; index < length; index++) {
                String liftRide = jedis.lindex(skierID, index);
                String[] parts = liftRide.split(",");
                if (resortID.equals(parts[0]) && seasonID.equals(parts[1])
                    && dayID.equals(parts[2])) {
                    dayVertical += Integer.valueOf(parts[6]);
                }
            }
            return dayVertical;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getTotalVertical(JedisPool pool, int skierID) {
        int totalVertical = 0;
        String keyString = String.valueOf(skierID);
        try (Jedis jedis = pool.getResource()) {
            long length = jedis.llen(keyString);
            for (long index = 0; index < length; index++) {
                String liftRide = jedis.lindex(keyString, index);
                String[] parts = liftRide.split(",");
                totalVertical += Integer.valueOf(parts[6]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalVertical;
    }
}
