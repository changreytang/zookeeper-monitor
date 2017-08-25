package com.rtang;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooKeeperLatencyChecker {
  public static final Logger LOG = LoggerFactory.getLogger(ZooKeeperLatencyChecker.class);

  private static final int BASE_SLEEP_TIME_MS = 1000;
  private static final int MAX_RETRIES = 3;
  private static final String ZK_PATH = "/tmp/zookeeper_latency_monitor";
  private CuratorFramework _client;

  public ZooKeeperLatencyChecker(String connectionString) {
    LOG.info("Attempting to connect to following ZooKeeper servers: {}", connectionString);
    ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES);
    _client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
    _client.start();
  }

  public long getCreateOperationLatency() {
    if (nodeExists(ZK_PATH)) { // in case the path already exists
      try {
        _client.delete().guaranteed().forPath(ZK_PATH);
      } catch (Exception e) {
        LOG.error(e.toString());
        return -1;
      }
    }
    try {
      long startTime = System.currentTimeMillis();
      _client.create().creatingParentsIfNeeded().forPath(ZK_PATH);
      long endTime = System.currentTimeMillis();
      return endTime - startTime;
    } catch (Exception e) {
      LOG.error(e.toString());
      return -1;
    }
  }

  public long getReadOperationLatency() {
    if (!nodeExists(ZK_PATH)) { // in case the path doesn't exist
      try {
        _client.create().creatingParentsIfNeeded().forPath(ZK_PATH);
      } catch (Exception e) {
        LOG.error(e.toString());
        return -1;
      }
    }
    byte[] rawData = null;
    try {
      long startTime = System.currentTimeMillis();
      rawData = _client.getData().forPath(ZK_PATH);
      long endTime = System.currentTimeMillis();
      return endTime - startTime;
    } catch (Exception e) {
      LOG.error(e.toString());
      return -1;
    }
  }

  public long getDeleteOperationLatency() {
    if (!nodeExists(ZK_PATH)) { // in case the path doesn't exist
      try {
        _client.create().creatingParentsIfNeeded().forPath(ZK_PATH);
      } catch (Exception e) {
        LOG.error(e.toString());
        return -1;
      }
    }
    try {
      // the delete is guaranteed even if initial failure, curator will attempt to delete in background until successful
      long startTime = System.currentTimeMillis();
      _client.delete().guaranteed().forPath(ZK_PATH);
      long endTime = System.currentTimeMillis();
      return endTime - startTime;
    } catch (Exception e) {
      LOG.error(e.toString());
      return -1;
    }
  }

  private boolean nodeExists(String path) {
    try {
      Stat stat = _client.checkExists().forPath(path);
      if (stat == null) {
        return false;
      }
      return true;
    } catch (Exception e) {
      LOG.error(e.toString());
      return false;
    }
  }

  public void close() {
    _client.close();
  }
}

