package Server.Servlet;

public class IntHelper {

    public static boolean isInteger(String s) {
      try {
        Integer.parseInt(s);
      } catch(NumberFormatException e) {
        return false;
      }
      return true;
    }
}
