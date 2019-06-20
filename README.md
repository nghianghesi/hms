# hms
An experiment of Micro-service to overcome challenges of tracking-query problem.
1. Goals: Able to handle requests at massive volume
2. Techniques: Java, Playframework, MongoDb, Kafka, asynchronous, parrallel programing, Akka, KSQL, DlsJson
3. Solutions: 
  * Geo based sharding by Mongodb
  * Geo based sharding & memory tracking
4. Enviroment for testing (all machines using Core I5, 12G ram, Windows 10 64bit):
 * Test data: tracking location of 240K objects (updated every 30s)
 * Geo based sharding & in-Memory tracking
     * 1 machine for gobetween load balancer + Netty Server for REST api server
     * 1 machine for Netty Server + REST api server (weight 2)
     * 1 machine for Kafka 
     * 2 machines for processing tracking & query
     * 2 machines to simulate clients
     * 1 machine for MongoDb
 * Geo based Mongo Sharding
     * 1 machine for gobetween load balancer + Netty Server for REST api server
     * 1 machine for Netty Server + REST api server (weight 2)
     * 1 machine for master
     * 2 machines for sharding nodes
     * 2 machines to simulate clients
5. Results 
 * Geo based sharding by Mongodb
     * Able to scale rest api out to multiple machines, 
     * Able to handle 3K concurrent of tracking requests + 3K concurrent of query requests. 
      (simulated by ~6K threads, 3K on each of 2 client machines)
     * max time for processing one request ~15s
     * avg ~2K requests/s (tracking+query) 
 * Geo based sharding & in-Memory tracking
     * Able to scale processing, rest api out to multiple machines, no more bottle neck
     * Able to handle 3K concurrent of tracking requests + 3K concurrent of query requests. 
      (simulated by ~6K threads, 3K on each of 2 client machines)
     * max time for processing one request ~ 10s
     * avg 4.5K+++ requests/s (tracking+query) - can get more because of scaling out, no more bottle neck
