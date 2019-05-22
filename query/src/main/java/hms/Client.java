package hms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Int;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class Client {
	private static final int NUM_OF_CUSTOMERS = 50000;
	
	private static final double MAX_LATITUDE = 90;		
	private static final double MIN_LATITUDE = -90;	
	
	private static final double MAX_LONGITUDE = 180;		
	private static final double MIN_LONGITUDE = -180;
	
	private static final double START_RANGE_LATITUDE = 33.587882;	
	private static final double END_RANGE_LATITUDE = 34.185252;

	private static final double START_RANGE_LONGITUDE = -118.178919;	
	private static final double END_RANGE_LONGITUDE = -117.959664;
	
	private static final double LONGITUDE_MOVE = 0.01;
	private static final double LATITUDE_MOVE = 0.01;
	
	private static final int NUM_OF_LOOP = Int.MaxValue();
	private static final int NUM_OF_THREAD = 1000;
	private static final int THREAD_DELAY = 100;
	private static final int ITEM_PER_THREAD=NUM_OF_CUSTOMERS/NUM_OF_THREAD;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    
    private static boolean shutdown = false;

	private static double getRandomLatitude() {
		return START_RANGE_LATITUDE + ThreadLocalRandom.current().nextDouble(0.0, END_RANGE_LATITUDE - START_RANGE_LATITUDE);
	}
	
	private static double getRandomLongitude() {
		return START_RANGE_LONGITUDE + ThreadLocalRandom.current().nextDouble(0.0, END_RANGE_LONGITUDE - START_RANGE_LONGITUDE);
	}
	
	private static void randomMove(ProviderQueryBuilder queryBuilder) {
		double latDiff = ThreadLocalRandom.current().nextDouble(0,1) > 0.5 ? LATITUDE_MOVE : -LATITUDE_MOVE;
		double longDiff = ThreadLocalRandom.current().nextDouble(0,1) > 0.5 ? LONGITUDE_MOVE : -LONGITUDE_MOVE;
		queryBuilder.setLatitude(queryBuilder.getLatitude()+ latDiff);
		if(queryBuilder.getLatitude() < MIN_LATITUDE) {
			queryBuilder.setLatitude(MIN_LATITUDE);
		}
		
		if(queryBuilder.getLatitude() > MAX_LATITUDE) {
			queryBuilder.setLatitude(MAX_LATITUDE);
		}
		
		queryBuilder.setLongitude(queryBuilder.getLongitude()+ longDiff);
		if(queryBuilder.getLongitude() < MIN_LONGITUDE) {
			queryBuilder.setLongitude( MIN_LONGITUDE);
		}
		
		if(queryBuilder.getLongitude() > MAX_LONGITUDE) {
			queryBuilder.setLongitude(MAX_LONGITUDE);
		}		
	}
	
	private static void initCoordinates(List<ProviderQueryBuilder> list) {		
		for(int idx = 0; idx < NUM_OF_CUSTOMERS; idx++) {	
			ProviderQueryBuilder queryBuilder = new ProviderQueryBuilder();
			queryBuilder.setLatitude(getRandomLatitude());
			queryBuilder.setLongitude(getRandomLongitude());
			//client.trackingProvider(trackingBuilder.buildTracking());
			list.add(queryBuilder);
		}
	}	

	private static Runnable buildEndGroupRunnable(int groupidx) {
		return () -> {		
				logger.info("End group {}", groupidx);
		};
	}
	
	private static void sleepWithoutException(int durationInMilisecond) {		
		try {
			Thread.sleep(THREAD_DELAY);
		} catch (Exception e) {
			logger.error("Sleep Error {}", e);
		}			
	}

	private static Runnable buildQueryProvidersRunnable(HMSRESTClient client, List<ProviderQueryBuilder> list, int groupidx) {
		return () -> {				
				int startidx = groupidx * ITEM_PER_THREAD;
				int endidx = (groupidx + 1) * ITEM_PER_THREAD;
				
				for(int loop = 0; loop < NUM_OF_LOOP && !shutdown; loop++) {									
					logger.info("Running group {}, loop {}", groupidx, loop);
					for(int idx = startidx; idx < endidx && idx < list.size(); idx++) {					
						ProviderQueryBuilder position = list.get(idx);
						randomMove(position);	
						try {
							client.queryProviders(position.build());
						} catch (Exception e) {
							logger.error("Error call service: group {}, loop {}", groupidx, loop);
						}		
						
						sleepWithoutException(1+ThreadLocalRandom.current().nextInt()%10);
					}	
					
					sleepWithoutException(THREAD_DELAY);
				}
		};	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<ProviderQueryBuilder> list = new ArrayList<ProviderQueryBuilder>();
		String serviceUrl = args.length>0?args[0]:"http://localhost:9000/";
		HMSRESTClient client = new HMSRESTClient(serviceUrl, logger);

		ForkJoinPool myPool = new ForkJoinPool(NUM_OF_THREAD);
		List<CompletableFuture<Void>> groupRunners = new ArrayList<CompletableFuture<Void>>();
		
		initCoordinates(list);
		for(int groupidx = 0; groupidx < NUM_OF_THREAD; groupidx++) { 
			groupRunners.add(CompletableFuture.runAsync(buildQueryProvidersRunnable(client, list, groupidx), myPool));				
		}		
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		shutdown = true;		
		
		for (int groupidx = 0; groupidx < groupRunners.size(); groupidx++) {
			groupRunners.get(groupidx).thenRun(buildEndGroupRunnable(groupidx)).join();
		}
		
		logger.info(client.getStats());
	}
}
