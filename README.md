# hms
An experiment of Micro-service to overcome challenges of tracking-query problem.
1. Goals: Able to handle requests at massive volumn
2. Techniques: Java, Playframework, MongoDb, Kafka, asynchronous, parrallel programing, Akka, KSQL, DlsJson
3. Solutions: 
  * Geo based sharding by Mongodb
  * Geo based sharding & memory tracking
4. Enviroment for testing:
 * Test data: tracking location of 60K objects (updated every 30s)
 * Geo based sharding & in-Memory tracking
     * 1 machine for Netty Server + REST api server
     * 1 machine for Kafka 
     * 2 machines for processing tracking & query
     * 2 machines to simulate clients
     * 1 machine for MongoDb
 * Geo based Mongo Sharding
     * 1 machine for Netty Server + REST api server
     * 1 machine for master
     * 2 machines for sharding nodes
     * 2 machines to simulate clients
5. Results: 
  * Able to scale processing to multiple machines, 
  * Able to handle more than 2K concurrent of tracking requests + more than 2K concurrent of query requests. 
   (simulated by ~4K threads, 2K on each of 2 client machines)
  * max time for processing one request ~ 8s
  * avg 2K+ requests/s (tracking+query) 
