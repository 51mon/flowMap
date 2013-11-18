package flowMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
  public static void main(String[] args) {
    //Test test = new Test();
    //System.out.println("aze;1;1".replaceFirst(";\\d+;\\d+$", ""));
    //System.out.println(test.parseServiceName("sdf")[0] + test.parseServiceName("sdf")[1]);
    //System.out.println(test.parseServiceName("path:service")[0] + test.parseServiceName("path:service")[1]);
    Pattern pattern = Pattern.compile("^([^\\[;]*)(?:\\[(\\d+)\\])?(?:\\[(\\d+)\\])?;(\\d+);(\\d+)(.*)");
    Matcher m = pattern.matcher("/useless;1;0");
    boolean b = m.matches();
    //System.out.println(b);
    System.out.println(m.group(1));
    System.out.println(m.groupCount());
    for (int i = 1; i <= m.groupCount(); i++)
      System.out.println(m.group(i));
    //System.out.println(m.group(2));
  }
  public String[] parseServiceName(String fullName) {
    String[] split = fullName.split(":");
    if (split.length == 1)
      split = new String[]{"", fullName};
    return split;
  }
}
