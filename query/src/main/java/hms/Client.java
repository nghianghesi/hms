package hms;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import scala.Int;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class Client {
	private static int NUM_OF_CUSTOMERS = 50000;
	
	private static final double MAX_LATITUDE = 90;		
	private static final double MIN_LATITUDE = -90;	
	
	private static final double MAX_LONGITUDE = 180;		
	private static final double MIN_LONGITUDE = -180;
	
	private static double START_RANGE_LATITUDE = 33.587882;	
	private static double END_RANGE_LATITUDE = 34.185252;

	private static double START_RANGE_LONGITUDE = -118.178919;	
	private static double END_RANGE_LONGITUDE = -117.959664;
	
	private static final double LONGITUDE_MOVE = 0.01;
	private static final double LATITUDE_MOVE = 0.01;
	
	private static final int NUM_OF_LOOP = Int.MaxValue();
	private static int NUM_OF_THREAD = 1000;
	private static int THREAD_DELAY = 200;
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
			Thread.sleep(durationInMilisecond);
		} catch (Exception e) {
			logger.error("Sleep Error {}", e);
		}			
	}

	private static Runnable buildQueryProvidersRunnable(HMSRESTClient client, List<ProviderQueryBuilder> list, int groupidx) {
		return () -> {				
				int ITEM_PER_THREAD = (int)((NUM_OF_CUSTOMERS + NUM_OF_THREAD-1) / NUM_OF_THREAD);
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
						
						sleepWithoutException(1+(ThreadLocalRandom.current().nextInt()& Integer.MAX_VALUE)%10);
					}	
					
					sleepWithoutException(THREAD_DELAY);
				}
		};	
	}
	
	private static Scanner inScanner = new Scanner(System.in);
	private static void waitingforEnter() {
		inScanner.nextLine();
	}
	
	public static void main(String[] args) {
		Config conf = ConfigFactory.load();
		
		if(conf.hasPath("num-of-customer")) {
			NUM_OF_CUSTOMERS = conf.getInt("num-of-customer");
		}	
		
		if(conf.hasPath("thread-delay")) {
			THREAD_DELAY = conf.getInt("thread-delay");
		}	
		
		if(conf.hasPath("num-of-thread")) {
			NUM_OF_THREAD = conf.getInt("num-of-thread");
		}
		
		if(conf.hasPath("area")) {
			START_RANGE_LATITUDE = conf.getDouble("area.start-lat");	
			END_RANGE_LATITUDE = conf.getDouble("area.end-lat");

			START_RANGE_LONGITUDE = conf.getDouble("area.start-long");
			END_RANGE_LONGITUDE = conf.getDouble("area.end-long");
		}
		
		
		// TODO Auto-generated method stub
		List<ProviderQueryBuilder> list = new ArrayList<ProviderQueryBuilder>(NUM_OF_CUSTOMERS);
		String serviceUrl = args.length>0?args[0]:"http://localhost:9000/";
		HMSRESTClient client = new HMSRESTClient(serviceUrl, logger);
		client.initRequest();
		ForkJoinPool myPool = new ForkJoinPool(NUM_OF_THREAD);
		List<CompletableFuture<Void>> groupRunners = new ArrayList<CompletableFuture<Void>>();
		
		initCoordinates(list);
		for(int groupidx = 0; groupidx < NUM_OF_THREAD; groupidx++) { 
			groupRunners.add(CompletableFuture.runAsync(buildQueryProvidersRunnable(client, list, groupidx), myPool));				
		}		
		
		waitingforEnter();
		shutdown = true;		
		
		for (int groupidx = 0; groupidx < groupRunners.size(); groupidx++) {
			groupRunners.get(groupidx).thenRun(buildEndGroupRunnable(groupidx)).join();
		}
		
		logger.info(client.getStats());
	}
}
