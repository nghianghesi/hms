# hms
An experimental of Micro-service to overcome challanges of tracking-query problem.
1. Techniques: Java, Playframework, MongoDb, Kafka, asynchronous, parrallel programing, Akka, KSQL, DlsJson
2. Solutions: 
  * Geo based sharding by Mongodb
  * Geo based sharding & memory tracking
3. Results: 
  * Able to scale processing to multiple machines, 
  * Able to handle more than 2K concurrent of tracking + more than 2K concurrent of query requests. 
   (simulated by ~4K threads, 2K on each of 2 client machines)
  * max time for request < 15s, avg 1K+ requests/s (tracking+query) 
