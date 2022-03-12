package Server.Servlet;

import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "ResortServlet", value="/resorts/*")

public class ResortServlet extends HttpServlet {
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
            setStatusAsValid(response);
            String jsonData = gson.toJson("resort 7");
            out.write(jsonData);
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isGetUrlValid(urlParts)) {
            setStatusAsInvalid(response);
            return;
        } else {
            setStatusAsValid(response);
            String jsonData = gson.toJson("resort 7");
            out.write(jsonData);
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
}
