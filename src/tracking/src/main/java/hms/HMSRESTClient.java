package hms;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import hms.dto.Provider;
import hms.dto.ProviderTracking;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.lang.reflect.Type;
import java.time.Duration;


public class HMSRESTClient{
	public interface HMSServiceIntegration{
		@GET("/")
		Call<ResponseBody> initRequest();
		@POST("/provider/tracking")
		Call<ResponseBody> trackingProvider(@Body ProviderTracking tracking);
		
		@POST("/provider/clear")
		Call<ResponseBody> clearProvider();
		
		@POST("/provider/init")
		Call<ResponseBody> initProvider(@Body Provider provider);	
		
		@POST("/provider/get-by-zone")
		Call<ResponseBody> loadByZone(@Body String zone);		
	}
	
	public static class ClientStats{
		private long maxResponseTime;
		private long numOfRequests = 0;
		private long timeTotal = 0;
		private static long timeLimits[] = new long[] {0, 1000,1500,2000,2500,3000,5000,10000, 15000, 20000};
		private long coutingRequestByTimeLimits[] = new long[timeLimits.length] ;
		private long failedRequestCount = 0;
			
		private void trackingMaxResponseTime(long elapsedTime) {
			maxResponseTime = Math.max(this.maxResponseTime, elapsedTime);
			numOfRequests++;
			timeTotal+=elapsedTime;
					
			for(int i=timeLimits.length-1;i>=0;i--) {
				if(elapsedTime >= timeLimits[i]) {
					coutingRequestByTimeLimits[i] += 1;
					break;
				}
			}
		}		
		
		public void accumulateOtherStats(ClientStats other) {
			this.maxResponseTime = Math.max(this.maxResponseTime, other.maxResponseTime);
			this.numOfRequests+=other.numOfRequests;
			this.timeTotal+=other.timeTotal;

			for(int i=timeLimits.length-1;i>=0;i--) {
				this.coutingRequestByTimeLimits[i]+=other.coutingRequestByTimeLimits[i];
			}
			this.failedRequestCount+=other.failedRequestCount;
		}

		public String getStats() {
			String s = String.format("Response time: max %d, Faield %d, Total: %d, Time %d", this.maxResponseTime, failedRequestCount, this.numOfRequests, this.timeTotal);
			for(int i=0;i<timeLimits.length;i++) {
				s = String.format("%s, %d - %d", s, timeLimits[i], this.coutingRequestByTimeLimits[i]);
			}
			return s;
		}		
	}
	
	private HMSServiceIntegration serviceIntegration;
	private Logger logger;
	private String serviceURL;
	
	Type providerListType = new TypeToken<ArrayList<Provider>>(){}.getType();

	private com.google.gson.Gson gson = new com.google.gson.Gson();
	private void buildIntegration(int poolsize) {
		ConnectionPool pool = new ConnectionPool(poolsize, 1, TimeUnit.MINUTES);

		OkHttpClient client = new OkHttpClient.Builder()
									  .readTimeout(Duration.ofSeconds(20))
		                              .connectionPool(pool)
		                              .build();
		
		Retrofit retrofit = new Retrofit.Builder()
                .client(client)
			    .baseUrl(serviceURL)
			    .addConverterFactory(GsonConverterFactory.create())
			    .build();
		
		this.serviceIntegration = retrofit.create(HMSServiceIntegration.class);
	}
	
	public HMSRESTClient(String Url, Logger logger,int poolsize) {
		this.serviceURL = Url;
		this.logger = logger;
		buildIntegration(poolsize);
	}
	
	public void initRequest() {
		try {			      
			this.serviceIntegration.initRequest().execute().body().string();		    
		} catch (Exception e) {
			logger.error("Tracking Provider", e);
		}	
	}
	
	public void trackingProvider(ProviderTracking tracking, ClientStats stats) {
		try {			      
			long startTime = System.currentTimeMillis();
			Response<ResponseBody> body = this.serviceIntegration.trackingProvider(tracking).execute();
			if(body!=null && body.body()!=null && body.isSuccessful()) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				stats.trackingMaxResponseTime(elapsedTime);
				if(elapsedTime == stats.maxResponseTime) {
					logger.info("Response time: {}", stats.maxResponseTime);
				}				
			}else {
				logger.info("Empty response");
				stats.failedRequestCount+=1;
			}		    
		} catch (Exception e) {
			stats.failedRequestCount+=1;
			logger.error("Tracking Provider", e);
		}
	}	
	
	public void initProvider(Provider provider) {
		try {			
			this.serviceIntegration.initProvider(provider).execute().body().string();
		} catch (Exception e) {
			logger.error("Init Provider", e);
		}	
	}
	
	public List<Provider> loadProvidersByZone(String zone) {
		try {			
			String json = this.serviceIntegration.loadByZone(zone).execute().body().string();
			return gson.fromJson(json, providerListType);
		} catch (Exception e) {
			logger.error("Init Provider", e);
			return new ArrayList<Provider>();
		}	
	}	

	public void clearProvider() {
		try {			
			this.serviceIntegration.clearProvider().execute().body().string();			
		} catch (Exception e) {
			logger.error("Tracking Provider", e);
		}
	}	
}
