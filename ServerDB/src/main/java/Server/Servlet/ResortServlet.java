package Server.Servlet;

import Model.LiftRide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@WebServlet(name = "ResortServlet", value="/resorts/*")

public class ResortServlet extends HttpServlet {

    private static String REDIS_HOST = "172.31.27.17";
    private static int REDIS_PORT = 6379;
    private static String REDIS_PASSWORD = "password";
    private static JedisPoolConfig config = new JedisPoolConfig();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //resorts
        //resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
        //resorts/{resortID}/seasons
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        String urlPath = request.getPathInfo();

        //check we have a URL
        if (urlPath == null || urlPath.isEmpty()) {
            setStatusAsNotFound(response);
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isGetUrlValid(urlParts)) {
            setStatusAsInvalid(response);
            return;
        } else {
            if (handleNumOfSkiersAtAResortSeasonDay(urlParts)) {
                // resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
                // get number of unique skiers at resort/season/day
                JedisPool jedisPool = new JedisPool(config, REDIS_HOST, REDIS_PORT, Protocol.DEFAULT_TIMEOUT, REDIS_PASSWORD);
                String resortID = urlParts[1];
                String seasonID = urlParts[3];
                String dayID = urlParts[5];
                long uniqueSkier = getUniqueSkier(jedisPool, resortID, seasonID, dayID);
                response.getWriter().write("There are " + uniqueSkier + " skiers at resort " + resortID
                    + " during season " + seasonID + " day " + dayID);
            } else {
                setStatusAsValid(response);
                String jsonData = gson.toJson("resort 7");
                out.write(jsonData);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //resorts/{resortID}/seasons
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();

        //check we have a URL
        if (urlPath == null || urlPath.isEmpty()) {
            setStatusAsNotFound(response);
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isGetUrlValid(urlParts)) {
            setStatusAsInvalid(response);
            return;
        } else {
            setStatusAsValid(response);
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

    private void setStatusAsValid(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Success!");
    }

    private boolean isGetUrlValid(String[] urlParts) {
        //resorts
        //resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
        //resorts/{resortID}/seasons
        if (urlParts.length == 1) {

            return true;
        } else if (urlParts.length == 3) {
            return handleAListOfSeasonsForAResort(urlParts);
        } else if (urlParts.length == 7) {
            return handleNumOfSkiersAtAResortSeasonDay(urlParts);
        } else {
            return false;
        }
    }

    private boolean isPostUrlValid(String[] urlParts) {
        //resorts/{resortID}/seasons
        return handleAListOfSeasonsForAResort(urlParts);
    }

    private boolean handleAListOfSeasonsForAResort(String[] urlParts) {
        if (urlParts.length == 3 && urlParts[2].equals("seasons")
            && IntHelper.isInteger(urlParts[1])) {
            return true;
        } else {
            return false;
        }
    }

    private boolean handleNumOfSkiersAtAResortSeasonDay(String[] urlParts) {
        boolean wordsMatch = ( urlParts[2].equals("seasons")
            && urlParts[4].equals("day") && urlParts[6].equals("skiers"));
        boolean intMatch = IntHelper.isInteger(urlParts[1]) && IntHelper.isInteger(urlParts[3])
            && IntHelper.isInteger(urlParts[5]);
        return wordsMatch && intMatch;
    }

    public long getUniqueSkier(JedisPool pool, String resortID, String seasonID, String dayID) {
        try {
            Jedis jedis = pool.getResource();
            String key = resortID + "/" + seasonID + "/" + dayID;
            long uniqueCount = jedis.scard(key);
            return uniqueCount;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
