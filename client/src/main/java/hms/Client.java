package hms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hms.dto.ProviderTracking;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class Client {
	private static final int NUM_OF_PROVIDER = 1000;
	
	private static final double MIN_LATITUDE = 33.587882;	
	private static final double MAX_LATITUDE = 34.185252;

	private static final double MIN_LONGITUDE = -118.178919;	
	private static final double MAX_LONGITUDE = -117.959664;
	
	private static final double LONGITUDE_MOVE = 0.01;
	private static final double LATITUDE_MOVE = 0.01;
	
	private static final int NUM_OF_LOOP = 10;
	private static final int NUM_OF_THREAD = 10;
	private static final int THREAD_DELAY = 100;
	private static final int ITEM_PER_THREAD=NUM_OF_PROVIDER/NUM_OF_THREAD;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    
    

	private static double getRandomLatitude() {
		return MIN_LATITUDE + ThreadLocalRandom.current().nextDouble(0.0, MAX_LATITUDE - MIN_LATITUDE);
	}
	
	private static double getRandomLongitude() {
		return MIN_LONGITUDE + ThreadLocalRandom.current().nextDouble(0.0, MAX_LONGITUDE - MIN_LONGITUDE);
	}
	
	private static void randomMove(ProviderTracking tracking) {
		double latDiff = ThreadLocalRandom.current().nextDouble(0,1) > 0.5 ? LATITUDE_MOVE : -LATITUDE_MOVE;
		double longDiff = ThreadLocalRandom.current().nextDouble(0,1) > 0.5 ? LONGITUDE_MOVE : -LONGITUDE_MOVE;
		tracking.latitude += latDiff;
		tracking.longitude += longDiff;
	}
	
	private static void initProvider(HMSRESTClient client, List<ProviderTracking> list) {
		for(int idx = 0; idx < NUM_OF_PROVIDER; idx++) {	
			ProviderTracking tracking = new ProviderTracking();
			tracking.id = UUID.randomUUID();	
			tracking.latitude = getRandomLatitude();
			tracking.longitude = getRandomLongitude();
			client.trackingProvider(tracking);
			list.add(tracking);
		}
	}
	
	

	private static Runnable buildEndGroupRunnable(int groupidx) {
		return new Runnable(){	
			@Override
			public void run() {		
				logger.info("End group {}", groupidx);
			}
		};
	}

	private static Runnable buildUpdateProviderRunnable(
			HMSRESTClient client, 
			List<ProviderTracking> list, int groupidx) {
		return new Runnable(){			
			@Override
			public void run() {				
				int startidx = groupidx * ITEM_PER_THREAD;
				int endidx = (groupidx + 1) * ITEM_PER_THREAD;
				
				for(int loop = 0; loop < NUM_OF_LOOP; loop++) {									
					logger.info("Running group {}, loop {}", groupidx, loop);
					for(int idx = startidx; idx < endidx && idx < list.size(); idx++) {					
						ProviderTracking tracking = list.get(idx);
						randomMove(tracking);	
						try {
							client.trackingProvider(tracking);
						} catch (Exception e) {
							logger.error("Error call service: group {}, loop {}", groupidx, loop);
						}		
					}
					
					try {
						Thread.sleep(THREAD_DELAY);
					} catch (Exception e) {
						logger.error("Error Looping: group {}, loop {}", groupidx, loop);
					}					
				}
			}	
		};	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<ProviderTracking> list = new ArrayList<ProviderTracking>();
		String serviceUrl = "http://localhost:9000/";
		HMSRESTClient client = new HMSRESTClient(serviceUrl, logger);

		client.clearProvider();
		initProvider(client, list);

		ForkJoinPool myPool = new ForkJoinPool(NUM_OF_THREAD);
		List<CompletableFuture<Void>> groupRunners = new ArrayList<CompletableFuture<Void>>();
		
		for(int groupidx = 0; groupidx < NUM_OF_THREAD; groupidx++) { 
			groupRunners.add(CompletableFuture.runAsync(buildUpdateProviderRunnable(client, list, groupidx), myPool));				
		}
		
		for (int groupidx = 0; groupidx < groupRunners.size(); groupidx++) {
			try {
				groupRunners.get(groupidx).thenRun(buildEndGroupRunnable(groupidx)).get(10, TimeUnit.HOURS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				logger.error("End group");
			}		
		}
	}

}
