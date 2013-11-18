package flowMap;

public class ServiceDef {
  public String className;
  public String methodName;
  public String[] args;
  public ServiceDef(String className, String methodName) {
    this.className = className;
    this.methodName = methodName;
    this.args = new String[]{};
  }
  public ServiceDef(String className, String methodName, String[] args) {
    this.className = className;
    this.methodName = methodName;
    this.args = args;
  }
}
