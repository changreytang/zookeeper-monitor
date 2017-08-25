# zookeeper-monitor

#### Info
Simple project that checks the latency of create, read, and delete operations to your ZooKeeper cluster which can utilized
to perform a "deep health check" on your ZK cluster.

#### Motivation/Background
The main motivation for this monitor is that ZK's "ruok" 4-letter-word monitoring command doesn't require ZK to actually
be functional in order to return "imok". It simply needs ZK's thread that handles the 4-letter-word commands to be alive.
It is possible for the core data handling threads can be completely deadlocked and ZK will still claim "imok".

#### How To Run
- Download the release jar
- Run `java -jar zookeeper-monitor-jar-with-dependencies.jar --zkEndpoint=kafka-<zookeeper-host:port>`

#### Example Output
```javascript
{"metrics":{"zk_create_latency_ms":"158","zk_delete_latency_ms":"155","zk_read_latency_ms":"153","zk_success":"1"}}
```