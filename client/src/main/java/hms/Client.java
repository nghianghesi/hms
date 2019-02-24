package hms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import hms.dto.ProviderTracking;

import java.util.concurrent.CompletableFuture;

public class Client {
	private static final int NUM_OF_PROVIDER = 1000;
	
	private static final double MIN_LATITUDE = 33.587882;	
	private static final double MAX_LATITUDE = 34.185252;

	private static final double MIN_LONGITUDE = -118.178919;	
	private static final double MAX_LONGITUDE = -117.959664;
	
	private static final double LONGITUDE_MOVE = 0.01;
	private static final double LATITUDE_MOVE = 0.01;
	
	private static final int NUM_OF_THREAD = 100;
	private static final int THREAD_DELAY = 100;
	
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
	

	private static Runnable buildUpdateProviderRunnable(HMSRESTClient client, List<ProviderTracking> list, int groupidx) {
		return new Runnable(){	
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int startidx= groupidx * NUM_OF_THREAD;
				int endidx = (groupidx + 1) * NUM_OF_THREAD;
				for(int idx = startidx; idx < endidx && idx < list.size(); idx++) {
					ProviderTracking tracking = list.get(idx);
					randomMove(tracking);
					client.trackingProvider(tracking);
					//System.out.println(""+idx);
					try {
						Thread.sleep(THREAD_DELAY);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}			
				}
			}	
		};	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<ProviderTracking> list = new ArrayList<ProviderTracking>();
		HMSRESTClient client = new HMSRESTClient("http://localhost:9000/");

		initProvider(client, list);

		Map<Integer, CompletableFuture<Void>> groupThreads = new HashMap<Integer,CompletableFuture<Void>>();
		for(int loop = 0;loop < 10; loop+=1) {
			for(int groupidx = 0; groupidx < (NUM_OF_PROVIDER + NUM_OF_THREAD - 1) / NUM_OF_THREAD; groupidx++) {
				if(groupThreads.containsKey(groupidx)) {
					groupThreads.get(groupidx).thenRunAsync(buildUpdateProviderRunnable(client, list, groupidx));
				}else {
					groupThreads.put(groupidx, CompletableFuture.runAsync(buildUpdateProviderRunnable(client, list, groupidx)));
				}
			}
		}

		for (CompletableFuture<Void> thread : groupThreads.values()) {
			thread.join();
		}
	}

}
