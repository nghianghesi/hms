# hms
An experimental of Micro-service to overcome challanges of tracking-query problem.
1. Techniques: Java, Playframework, MongoDb, Kafka, asynchronous, parrallel programing, Akka, KSQL, DlsJson
2. Solutions: 
  * Geo based sharding by Mongodb
  * Geo based sharding & memory tracking
3. Test setup:
 * 1 machine for Netty Server + Playframework
 * 1 machine for Kafka 
 * 2 machines for processing tracking & query
 * 2 machines to simulate clients
 * 1 machine for MongoDb
 * tracking location of 60K objects (updated every 30s)
4. Results: 
  * Able to scale processing to multiple machines, 
  * Able to handle more than 2K concurrent of tracking + more than 2K concurrent of query requests. 
   (simulated by ~4K threads, 2K on each of 2 client machines)
  * max time for request < 15s, avg 1.5K+ requests/s (tracking+query) 
