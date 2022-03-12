package Server.Servlet;

import Model.SkierRequestBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import io.swagger.client.model.LiftRide;
import java.nio.charset.StandardCharsets;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import lombok.SneakyThrows;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
    private ObjectPool<Channel> pool;
    private static String QUEUE_NAME = "LiftRide";
    private Gson gson = new Gson();

    @Override
    public void init() {
        this.pool = new GenericObjectPool<>(new ChannelFactory());
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
        } else {
            setStatusAsInvalid(response);
            return;
        }
    }

    @SneakyThrows
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
              // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
              liftRecord.addProperty("SkierID", Integer.parseInt(urlParts[7]));
              liftRecord.addProperty("ResortID", Integer.parseInt(urlParts[1]));
              liftRecord.addProperty("SeasonID", Integer.parseInt(urlParts[3]));
              liftRecord.addProperty("DayID", Integer.parseInt(urlParts[5]));
              System.out.println(liftRecord);
              Channel channel = null;

              try {
                  channel = pool.borrowObject();
                  channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                  channel.basicPublish("", QUEUE_NAME, null,
                      liftRecord.toString().getBytes(StandardCharsets.UTF_8));
                  response.getWriter().write(String.valueOf(liftRecord));
                  setStatusAsCreated(response);
              } catch (Exception e) {
                  e.printStackTrace();
                  setStatusAsInvalid(response);
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

    private void setStatusAsValid(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Success!");
    }

    private void setStatusAsCreated(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write("Data stored!");
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

    private boolean isBodyRequestValid(SkierRequestBody skierRequestBody) {
        boolean timeValid = skierRequestBody.getTime() != null;
        boolean liftIDValid = skierRequestBody.getLiftID() != null;
        boolean waitTimeValid = skierRequestBody.getWaitTime() != null;
        return timeValid && liftIDValid && waitTimeValid;
    }
}
