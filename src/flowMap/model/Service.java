package flowMap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.flow.Element;

@DatabaseTable(tableName = "services")
public class Service extends AdapterItem {
  public enum Type { FLOW, JAVA, ADAPTER }
  public static final String TYPE_COL = "type";
  @DatabaseField(columnName = TYPE_COL)
  Type type;

  // Java (extends BaseService)
  @DatabaseField(columnName = "java_class_name")
  String className;
  @DatabaseField(columnName = "java_method_name")
  String methodName;

  // FlowSvcImpl (extends BaseService)
  public static final String FLOW_ROOT_ID_COL = "flow_root_id";
  @DatabaseField(columnName = FLOW_ROOT_ID_COL, foreign = true)
  Element flowRoot;


  @ForeignCollectionField(foreignFieldName = "service", eager = true)
  ForeignCollection<Element> flows;

  // AdapterServiceNode (extends ARTNSService extends BaseService)

  // NSService
  @ForeignCollectionField(foreignFieldName = "serviceInput", eager = true)
  ForeignCollection<Record> inputs;
  @ForeignCollectionField(foreignFieldName = "serviceOutput", eager = true)
  ForeignCollection<Record> outputs;

  // BaseService (extends NSService)
  @DatabaseField(columnName = "is_available", format = "integer")
  boolean isAvailable;
  @DatabaseField(columnName = "is_stateless", format = "integer")
  boolean isStateless;
  @DatabaseField(columnName = "is_cache_enabled", format = "integer")
  boolean isCacheEnabled;
  @DatabaseField(columnName = "cache_TTL")
  int cacheTTL;
  @DatabaseField(columnName = "is_prefetch_enabled", format = "integer")
  boolean isPrefetchEnabled;
  @DatabaseField(columnName = "prefetch_level")
  int prefetchLevel;
  @DatabaseField(columnName = "description")
  String description;
  @DatabaseField(columnName = "binding")
  String binding;
  @DatabaseField(columnName = "template")
  String template;
  @DatabaseField(columnName = "template_type")
  String templateType;
  @DatabaseField(columnName = "package_name")
  String packageName;
  @DatabaseField(columnName = "load_err")
  String loadErr;
  @DatabaseField(columnName = "audit_level")
  int auditLevel;
  @DatabaseField(columnName = "is_system_service", format = "integer")
  boolean isSystemService;
  @DatabaseField(columnName = "retry_max")
  int retryMax;
  @DatabaseField(columnName = "retry_interval")
  long retryInterval;
  @DatabaseField(columnName = "max_retry_period")
  int maxRetryPeriod;

  public Service() {}
  public Service(Node node) {
    this.node = node;
  }
  public Service(Node node, Type type) {
    this.type = type;
    this.node = node;
  }

  public ForeignCollection<Element> getFlows() {
    return flows;
  }

  public void setFlows(ForeignCollection<Element> flows) {
    this.flows = flows;
  }

  public boolean isAvailable() {
    return isAvailable;
  }

  public void setAvailable(boolean available) {
    isAvailable = available;
  }

  public boolean isStateless() {
    return isStateless;
  }

  public void setStateless(boolean stateless) {
    isStateless = stateless;
  }

  public boolean isCacheEnabled() {
    return isCacheEnabled;
  }

  public void setCacheEnabled(boolean cacheEnabled) {
    isCacheEnabled = cacheEnabled;
  }

  public int getCacheTTL() {
    return cacheTTL;
  }

  public void setCacheTTL(int cacheTTL) {
    this.cacheTTL = cacheTTL;
  }

  public boolean isPrefetchEnabled() {
    return isPrefetchEnabled;
  }

  public void setPrefetchEnabled(boolean prefetchEnabled) {
    isPrefetchEnabled = prefetchEnabled;
  }

  public int getPrefetchLevel() {
    return prefetchLevel;
  }

  public void setPrefetchLevel(int prefetchLevel) {
    this.prefetchLevel = prefetchLevel;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBinding() {
    return binding;
  }

  public void setBinding(String binding) {
    this.binding = binding;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getTemplateType() {
    return templateType;
  }

  public void setTemplateType(String templateType) {
    this.templateType = templateType;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getLoadErr() {
    return loadErr;
  }

  public void setLoadErr(String loadErr) {
    this.loadErr = loadErr;
  }

  public int getAuditLevel() {
    return auditLevel;
  }

  public void setAuditLevel(int auditLevel) {
    this.auditLevel = auditLevel;
  }

  public boolean isSystemService() {
    return isSystemService;
  }

  public void setSystemService(boolean systemService) {
    isSystemService = systemService;
  }

  public int getRetryMax() {
    return retryMax;
  }

  public void setRetryMax(int retryMax) {
    this.retryMax = retryMax;
  }

  public long getRetryInterval() {
    return retryInterval;
  }

  public void setRetryInterval(long retryInterval) {
    this.retryInterval = retryInterval;
  }

  public int getMaxRetryPeriod() {
    return maxRetryPeriod;
  }

  public void setMaxRetryPeriod(int maxRetryPeriod) {
    this.maxRetryPeriod = maxRetryPeriod;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public ForeignCollection<Record> getInputs() {
    return inputs;
  }

  public void setInputs(ForeignCollection<Record> inputs) {
    this.inputs = inputs;
  }

  public ForeignCollection<Record> getOutputs() {
    return outputs;
  }

  public void setOutputs(ForeignCollection<Record> outputs) {
    this.outputs = outputs;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Element getFlowRoot() {
    return flowRoot;
  }

  public void setFlowRoot(Element flowRoot) {
    this.flowRoot = flowRoot;
  }
}
