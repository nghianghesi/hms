# hms
An experimental of Micro-service to overcome challanges of tracking-query problem.
1. Techniques: Java, Playframework, MongoDb, Kafka, asynchronous, parrallel programing, Akka, KSQL, DlsJson
2. Solutions: 
  * Geo based sharding by Mongodb
  * Geo based sharding & memory tracking
3. Enviroment for testing:
 * Test data: tracking location of 60K objects (updated every 30s)
 * Geo based sharding & Memory tracking
     * 1 machine for Netty Server + Playframework
     * 1 machine for Kafka 
     * 2 machines for processing tracking & query
     * 2 machines to simulate clients
     * 1 machine for MongoDb
 * Geo based Mongo Sharding
     * 1 machine for Netty Server + Playframework
     * 1 machine for master
     * 2 machines for sharding nodes
     * 2 machines to simulate clients
4. Results: 
  * Able to scale processing to multiple machines, 
  * Able to handle more than 2K concurrent of tracking + more than 2K concurrent of query requests. 
   (simulated by ~4K threads, 2K on each of 2 client machines)
  * max time for request < 15s, avg 1.2K+ requests/s (tracking+query) 
