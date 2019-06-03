# hms
An experimental of Micro-service to overcome challanges of tracking-query problem.
1. Techniques: Java, Playframework, MongoDb, Asynchronous, parrallel programing, Kafka, Akka, DlsJson
2. Solutions: 
  *Geo based sharding by Mongodb
  *Geo based sharding & memory tracking
3. Results: 
  * Able to scale processing to multiple machines, 
  * Able to handle more than 2K concurrent of tracking + more than 2K concurrent of query requests. less thang 15s/requests, avg 1K+ requests/s (tracking+query) 
  * (simulated by ~4K threads, 2K on each of 2 client machines)
