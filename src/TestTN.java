import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.sql.*;

public class TestTN {
  public static void main(String[] args) throws Exception {
    Class.forName("oracle.jdbc.driver.OracleDriver");
    Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@eaiwbmp01:1521:EAIP1", "L0213590", "L0213590");
    String sqlBizdocs = "select BIZDOC.doctimestamp, BIZDOC.docid, BIZDOC.userstatus, CONTENT " +
        "from WMTNMKTG_DEPOT.BIZDOC, WMTNMKTG_DEPOT.BIZDOCCONTENT, WMTNMKTG_DEPOT.BIZDOCTYPEDEF " +
        "where BIZDOCCONTENT.DOCID = BIZDOC.DOCID and userstatus != 'SUCCESS' " +
        "and BIZDOC.DOCTYPEID = BIZDOCTYPEDEF.TYPEID and TYPENAME = 'FR_DOC_TYPE_SCHEDULED_MOVEMENTS' " +
        "and BIZDOC.LASTMODIFIED > sysdate-1";
    PreparedStatement statement = conn.prepareStatement(sqlBizdocs);
    ResultSet results = statement.executeQuery();

    System.out.println("date;docid;status;truckLoadingAuth;loadingOrderFamily;shipmentNumber;commandNumber;bLNumber;terminalPlantCode;partnerCode");
    while (results.next()) {
      Date ts = results.getDate(1);
      String docid = results.getString(2);
      String status = results.getString(3);
      Blob source = results.getBlob(4);
      XPath xpath = XPathFactory.newInstance().newXPath();

      String truckLoadingAuth = getValue(source, xpath, "LoadingOrderCharacteristics/TruckLoadingAuthorizationNumber");

      String loadingOrderFamily = getValue(source, xpath, "LoadingOrderCharacteristics/LoadingOrderFamily");
      String shipmentNumber = getValue(source, xpath, "LoadingOrderCharacteristics/ShipmentNumber");
      String commandNumber = getValue(source, xpath, "LoadingOrderCharacteristics/CommandNumber");
      String bLNumber = getValue(source, xpath, "LoadingOrderCharacteristics/BLNumber");
      String terminalPlantCode = getValue(source, xpath, "TargetInformationSystem/TerminalPlantCode");
      String partnerCode = getValue(source, xpath, "Partner/PartnerCode");

//      System.out.println("docid = "+docid);
//      System.out.println("truckLoadingAuth = "+truckLoadingAuth);
//      System.out.println("loadingOrderFamily = "+loadingOrderFamily);
//      System.out.println("shipmentNumber = "+shipmentNumber);
//      System.out.println("commandNumber = "+commandNumber);
//      System.out.println("bLNumber = "+bLNumber);
//      System.out.println("terminalPlantCode = "+terminalPlantCode);
//      System.out.println("partnerCode = "+partnerCode);

      System.out.println(ts.toString()+";"+docid+";"+status+";"+truckLoadingAuth+";"+loadingOrderFamily+";"+shipmentNumber+";"+commandNumber+";"+bLNumber+";"+terminalPlantCode+";"+partnerCode);
    }
  }

  public static String getValue(Blob clob, XPath xpath, String expr) throws Exception {
    Node node = (Node) xpath.evaluate("/ScheduledMovements/Aggregate/"+expr, new InputSource(clob.getBinaryStream()), XPathConstants.NODE);
    xpath.reset();
    return node.getTextContent();
  }
}
