package hms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import hms.dto.ProviderTracking;

public class Client {
	private static final int NUM_OF_PROVIDER = 1000;
	
	private static final double MIN_LATITUDE = 33.587882;	
	private static final double MAX_LATITUDE = 34.185252;

	private static final double MIN_LONGITUDE = -118.178919;	
	private static final double MAX_LONGITUDE = -117.959664;
	
	private static final double LONGITUDE_MOVE = 0.01;
	private static final double LATITUDE_MOVE = 0.01;
	
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
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<ProviderTracking> list = new ArrayList<ProviderTracking>();
		HMSRESTClient client = new HMSRESTClient("http://localhost:9000/");

		for(int idx = 0; idx < NUM_OF_PROVIDER; idx++) {	
			ProviderTracking tracking = new ProviderTracking();
			tracking.id = UUID.randomUUID();	
			tracking.latitude = getRandomLatitude();
			tracking.longitude = getRandomLongitude();
			client.trackingProvider(tracking);
			list.add(tracking);
		}
		
		for(ProviderTracking tracking:list) {
			randomMove(tracking);
			client.trackingProvider(tracking);
		}
	}

}
