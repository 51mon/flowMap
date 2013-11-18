package flowMap.view;

import com.wm.net.HttpHeader;
import flowMap.API;

public class Test {
  public static void main(String[] args) {

    Rest rest = new Rest(new API("oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@eaiwbmd01:1530:EAID1", "WMIS", "WMIS"));
    rest.render(HttpHeader.GET, "/rest/Node", "", System.out);
  }
}
