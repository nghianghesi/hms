package hms;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import hms.dto.Provider;

public class Client {
	
	private static String ZONE="";
	private static final int UPDATE_INTERVAL = 5000;//30s;
	private static int NUM_OF_PROVIDER = 500;
	private static int POOL_SIZE = 50;
	
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
	
	private static final int NUM_OF_LOOP = Integer.MAX_VALUE;
	private static int NUM_OF_THREAD = 500;
	private static String SERVICE_URL= "http://localhost:9000/";
	private static boolean INIT_PROVIDERS = false;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    
    private static boolean shutdown = false;
    private static long countLongerThanInterval = 0;
	private static long startTest = 0;
	private static double getRandomLatitude() {
		return START_RANGE_LATITUDE + ThreadLocalRandom.current().nextDouble(0.0, END_RANGE_LATITUDE - START_RANGE_LATITUDE);
	}
	
	private static double getRandomLongitude() {
		return START_RANGE_LONGITUDE + ThreadLocalRandom.current().nextDouble(0.0, END_RANGE_LONGITUDE - START_RANGE_LONGITUDE);
	}
	
	
	private static void initProvider(HMSRESTClient client, List<ProviderTrackingBuilder> list, ForkJoinPool myPool) {		
		logger.info("Init Providers:");
		client.clearProvider();
		for(int idx=0;idx<NUM_OF_PROVIDER;idx++){
			list.add(null);
		}
		List<CompletableFuture<Void>>tasks = new ArrayList<>();
		for(int groupid_loop= 0;groupid_loop<NUM_OF_THREAD;groupid_loop++) {
			final int groupid = groupid_loop;
			final int split = (NUM_OF_PROVIDER + NUM_OF_THREAD - 1)/NUM_OF_THREAD;
			tasks.add(CompletableFuture.runAsync(() ->{
				for(int idx = groupid*split; idx<(groupid+1)*split && idx < NUM_OF_PROVIDER; idx++) {	
					ProviderTrackingBuilder trackingBuilder = new ProviderTrackingBuilder(ZONE,
							LATITUDE_MOVE, LONGITUDE_MOVE,
							MIN_LATITUDE, MAX_LATITUDE, MIN_LONGITUDE, MAX_LONGITUDE);
					trackingBuilder.setProviderid(UUID.randomUUID());	
					trackingBuilder.setLatitude(getRandomLatitude());
					trackingBuilder.setLongitude(getRandomLongitude());
					trackingBuilder.setName("Provider "+idx);
					client.initProvider(trackingBuilder.buildProvider());
					//client.trackingProvider(trackingBuilder.buildTracking());
					list.set(idx, trackingBuilder);
				}
			}));
		}
		
		for(CompletableFuture<Void> t: tasks) {
			t.join();
		}
	}	

	private static List<ProviderTrackingBuilder> loadProviders(HMSRESTClient client) 		
	{
		logger.info("Loading providers");
		List<ProviderTrackingBuilder> list = new ArrayList<ProviderTrackingBuilder>();
		List<Provider> providers = client.loadProvidersByZone(ZONE);
		for(Provider p : providers) {
			ProviderTrackingBuilder trackingBuilder = new ProviderTrackingBuilder(ZONE,
					LATITUDE_MOVE, LONGITUDE_MOVE,
					MIN_LATITUDE, MAX_LATITUDE, MIN_LONGITUDE, MAX_LONGITUDE);
			trackingBuilder.setProviderid(p.getProviderid());	
			trackingBuilder.setLatitude(getRandomLatitude());
			trackingBuilder.setLongitude(getRandomLongitude());
			trackingBuilder.setName(p.getName());
			list.add(trackingBuilder);		
		}
		return list;
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
			HMSRESTClient client, HMSRESTClient.ClientStats clientStats,
			List<ProviderTrackingBuilder> list, int groupidx) {
		return new Runnable(){			
			@Override
			public void run() {			
				int ITEM_PER_THREAD = (int)((NUM_OF_PROVIDER+NUM_OF_THREAD-1)/NUM_OF_THREAD);

				int startidx = groupidx * ITEM_PER_THREAD;
				int endidx = (groupidx + 1) * ITEM_PER_THREAD;
				long start = 0;
				for(int loop = 0; loop < NUM_OF_LOOP && !shutdown; loop++) {									
					logger.info("Running group {}, loop {}", groupidx, loop);
					start = System.currentTimeMillis();
					for(int idx = startidx; idx < endidx && idx < list.size()&& !shutdown; idx++) {					
						ProviderTrackingBuilder tracking = list.get(idx);
						tracking.randomMove();	
						try {
							client.trackingProvider(tracking.buildTracking(),clientStats);
						} catch (Exception e) {
							logger.error("Error call service: group {}, loop {}", groupidx, loop);
						}		
					}	
					
					long delay = UPDATE_INTERVAL - (System.currentTimeMillis() - start);
					if(delay<0) {
						logger.info("******************* longer than interval *********");
						countLongerThanInterval+=1;
					}
				}
			}	
		};	
	}
	
	private static Scanner inScanner = new Scanner(System.in);
	private static void waitingforEnter() {
		inScanner.nextLine();
	}
	
	private static void loadConfigs(Config conf) {
		ZONE = conf.getString("zone");
		
		if(conf.hasPath("num-of-provider")) {
			NUM_OF_PROVIDER = conf.getInt("num-of-provider");
		}		
		
		if(conf.hasPath("area")) {
			START_RANGE_LATITUDE = conf.getDouble("area.start-lat");	
			END_RANGE_LATITUDE = conf.getDouble("area.end-lat");

			START_RANGE_LONGITUDE = conf.getDouble("area.start-long");
			END_RANGE_LONGITUDE = conf.getDouble("area.end-long");
		}
		
		if(conf.hasPath("num-of-thread")) {
			POOL_SIZE = NUM_OF_THREAD = conf.getInt("num-of-thread");
		}
		
		if(conf.hasPath("service-url")) {
			SERVICE_URL=conf.getString("service-url");
		}			
		if(conf.hasPath("init-providers")) {
			INIT_PROVIDERS=conf.getBoolean("init-providers");
		}				
		
		if(conf.hasPath("pool-size")) {
			POOL_SIZE=conf.getInt("pool-size");
		}		
	}
	
	public static void main(String[] args) {

		Config conf = ConfigFactory.load();
		loadConfigs(conf);
		
		// TODO Auto-generated method stub
		List<ProviderTrackingBuilder> list = new ArrayList<ProviderTrackingBuilder>(NUM_OF_PROVIDER);
				
		HMSRESTClient client = new HMSRESTClient(SERVICE_URL, logger, POOL_SIZE);
		client.initRequest();
		ForkJoinPool myPool = new ForkJoinPool(NUM_OF_THREAD);
		List<CompletableFuture<Void>> groupRunners = new ArrayList<CompletableFuture<Void>>();
		List<HMSRESTClient.ClientStats> clientStats = new ArrayList<HMSRESTClient.ClientStats>();

		if(INIT_PROVIDERS) {
			initProvider(client, list, myPool);
		}else {
			list = loadProviders(client);
			NUM_OF_PROVIDER = list.size();
		}
		
		
		logger.info("Tracking Providers: {}, threads {}", list.size(), NUM_OF_THREAD);
		waitingforEnter();
		startTest = System.currentTimeMillis();

		for(int groupidx = 0; groupidx < NUM_OF_THREAD; groupidx++) {
			HMSRESTClient.ClientStats stats = new HMSRESTClient.ClientStats();
			clientStats.add(stats);
			groupRunners.add(CompletableFuture.runAsync(buildUpdateProviderRunnable(client,stats, list, groupidx), myPool));				
		}
		
		waitingforEnter();
		shutdown = true;
		for (int groupidx = 0; groupidx < groupRunners.size(); groupidx++) {
			groupRunners.get(groupidx).thenRun(buildEndGroupRunnable(groupidx)).join();
		}
		long testDuration = System.currentTimeMillis() - startTest;
		

		HMSRESTClient.ClientStats finalstats = new HMSRESTClient.ClientStats();
		for(int groupidx = 0; groupidx < NUM_OF_THREAD; groupidx++) {
			finalstats.accumulateOtherStats(clientStats.get(groupidx));
		}
		logger.info("{}, Long Interval {}, Duration {}", finalstats.getStats(), countLongerThanInterval, testDuration );
	}
}
