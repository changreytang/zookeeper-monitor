package com.rtang;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ZooKeeperLatencyMonitor {
  private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperLatencyMonitor.class);
  private static final ObjectMapper mapper;
  private ZooKeeperLatencyChecker latencyChecker;

  static {
    mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
    mapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
  }

  public ZooKeeperLatencyMonitor(String connectionString) {
    latencyChecker = new ZooKeeperLatencyChecker(connectionString);
  }

  public String getJsonLatencyMetrics() throws Exception {
    Map<String, Long> metrics = new TreeMap<String, Long>();

    Long createLatency = latencyChecker.getCreateOperationLatency();
    Long readLatency = latencyChecker.getReadOperationLatency();
    Long deleteLatency = latencyChecker.getDeleteOperationLatency();
    Long operationSuccess = 1L;
    if (createLatency < 0 || readLatency < 0 || deleteLatency < 0) operationSuccess = 0L;

    metrics.put("zk_create_latency_ms", createLatency);
    metrics.put("zk_read_latency_ms", readLatency);
    metrics.put("zk_delete_latency_ms", deleteLatency);
    metrics.put("zk_success", operationSuccess);

    Map<String, Object> jsonObject = new HashMap<String, Object>();
    jsonObject.put("metrics", metrics);

    return mapper.writeValueAsString(jsonObject);
  }

  public static void main(String[] args) throws Exception {
    OptionParser parser = getParser();
    OptionSet cmdArgs = parser.parse(args);

    if (cmdArgs.has("help")) {
      parser.printHelpOn(System.out);
      return;
    }

    if (!cmdArgs.has("zkEndpoint")) {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("zkEndpoint is required.");
    }

    ZooKeeperLatencyMonitor monitor = new ZooKeeperLatencyMonitor((String) cmdArgs.valueOf("zkEndpoint"));

    System.out.println(monitor.getJsonLatencyMetrics());
  }

  private static OptionParser getParser() {
    return new OptionParser() {
      {
        acceptsAll(Arrays.asList("z", "zkEndpoint"), "the endpoint for the zookeeper cluster you want to check latency for")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("zookeeper-host:2181");
        acceptsAll(Arrays.asList("h", "?", "help"), "display help").forHelp();
      }
    };
  }
}
