# hms
Hierarchical geo-based micro-services to overcome challenges of tracking-searching problem
1. Goals: Able to handle massive volume of requests
2. Techniques: Java, Playframework, MongoDb, Kafka, asynchronous, parrallel programing, Akka, KSQL, DlsJson
3. Solutions: 
  * Hierarchical geo-based sharding by Mongodb
  * Hierarchical geo-based sharding by Micro-services (memory storage)
4. Enviroment for testing (all machines using Core I5, 12G ram, Windows 10 64bit):
 * Test data: tracking location of 240K objects (updated every 30s)
 * Hierarchical geo-based sharding by Mongodb
     * 2 machines for Netty Server + REST api server
     * 1 machine for master
     * 2 machines for sharding nodes     
 * Hierarchical geo-based sharding by Micro-services (memory storage)
     * 1 machine for REST api on Netty to handle tracking requests
     * 1 machine for REST api on Netty to handle searching requests
     * 1 machine for Mongo Db (store & serve provider info only)
     * 2 machines for Kafka & service nodes
5. Results 
 * Hierarchical geo-based sharding by Mongodb
     * Able to scale rest api out to multiple machines, 
     * Able to handle 3K concurrent of tracking requests + 3K concurrent of 
     requests. 
      (simulated by ~6K threads, 3K on each of 2 client machines)
     * Max time for processing one request ~15s
     * Avg ~2K requests/s (tracking+searching) 
 * Hierarchical geo-based sharding by Micro-services (memory storage)
     * Able to scale processing, rest api out to multiple machines, no more bottle neck
     * Able to handle 6K concurrent request(3K concurrent of tracking + 3K concurrent of searching, simulated by ~6K threads, 3K on each of 2 client machines)
     * Max time for processing one request ~ 10s
     * 4.5K+++ requests/s (tracking+searching) - can get more because of indefinitely scaling out, no more bottle neck
