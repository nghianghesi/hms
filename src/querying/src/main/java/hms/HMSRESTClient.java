package hms;


import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import hms.dto.Coordinate;
import hms.dto.Provider;
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

public class HMSRESTClient{
	public interface HMSServiceIntegration{
		@GET("/")
		Call<ResponseBody> initRequest();
		@POST("/api/provider/geoquery")
		Call<ResponseBody> queryProviders(@Body Coordinate tracking);
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
	private void buildIntegration(int poolsize) {
		ConnectionPool pool = new ConnectionPool(poolsize, 1, TimeUnit.MINUTES);

		OkHttpClient client = new OkHttpClient.Builder()
		                              .connectionPool(pool)
		                              .readTimeout(Duration.ofSeconds(20))
		                              .build();
		Retrofit retrofit = new Retrofit.Builder()
                .client(client)
			    .baseUrl(serviceURL)
			    .addConverterFactory(GsonConverterFactory.create())
			    .build();
		
		this.serviceIntegration = retrofit.create(HMSServiceIntegration.class);
	}
	
	private com.google.gson.Gson gson = new com.google.gson.Gson();
	Type providerListType = new TypeToken<ArrayList<Provider>>(){}.getType();


	public HMSRESTClient(String Url, Logger logger, int poolsize) {
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
	
	public List<Provider> queryProviders(Coordinate coordinate, ClientStats stats) {
		try {			      
			long startTime = System.currentTimeMillis();
			//logger.info("query providers {} {}", coordinate.getLatitude(), coordinate.getLongitude());
			Response<ResponseBody> body = this.serviceIntegration.queryProviders(coordinate).execute();
			if(body!=null && body.body()!=null && body.isSuccessful()) {
				String str = body.body().string();
				long elapsedTime = System.currentTimeMillis() - startTime;
				stats.trackingMaxResponseTime(elapsedTime);
				if(elapsedTime == stats.maxResponseTime) {
					logger.info("Response time: {}", stats.maxResponseTime);
				}				
				return gson.fromJson(str, providerListType);
			}else {
				stats.failedRequestCount+=1;
				logger.info("Empty response");
				return new ArrayList<Provider>();
			}            			
		} catch (Exception e) {
			logger.error("query Provider", e);
			stats.failedRequestCount+=1;
			return new ArrayList<Provider>();
		}
	}	
}
