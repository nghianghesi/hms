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
		@POST("/provider/geoquery")
		Call<ResponseBody> queryProviders(@Body Coordinate tracking);
	}
	
	private HMSServiceIntegration serviceIntegration;
	private Logger logger;
	private String serviceURL;
	private void buildIntegration() {
		ConnectionPool pool = new ConnectionPool(1000, 1, TimeUnit.MINUTES);

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
	
	private long maxResponseTime;
	private long numOfRequests = 0;
	private long timeTotal = 0;
	private long timeLimits[] = new long[] {0, 1000,1500,2000,2500,3000,5000,10000, 15000, 20000};
	private long coutingRequestByTimeLimits[] = new long[timeLimits.length] ;

	private long failedRequestCount = 0;	
	private com.google.gson.Gson gson = new com.google.gson.Gson();
	Type providerListType = new TypeToken<ArrayList<Provider>>(){}.getType();

	private void trackingMaxResponseTime(long elapsedTime) {
		synchronized(this) {
			maxResponseTime = Math.max(this.maxResponseTime, elapsedTime);
			numOfRequests++;
			timeTotal+=elapsedTime;
		}
		
		if(elapsedTime == maxResponseTime) {
			logger.info("Response time: {}", maxResponseTime);
		}
		
		for(int i=timeLimits.length-1;i>=0;i--) {
			if(elapsedTime >= timeLimits[i]) {
				coutingRequestByTimeLimits[i] += 1;
				logger.info("Response time: max {}, elapsed {}, limit {}, count {}",maxResponseTime, elapsedTime, timeLimits[i], coutingRequestByTimeLimits[i]);
				break;
			}
		}
	}
	
	public HMSRESTClient(String Url, Logger logger) {
		this.serviceURL = Url;
		this.logger = logger;
		buildIntegration();
	}
	
	public void initRequest() {
		try {			      
			this.serviceIntegration.initRequest().execute().body().string();		    
		} catch (Exception e) {
			logger.error("Tracking Provider", e);
		}	
	}
	
	public List<Provider> queryProviders(Coordinate coordinate) {
		try {			      
			long startTime = System.currentTimeMillis();
			logger.info("query providers {} {}", coordinate.getLatitude(), coordinate.getLongitude());
			Response<ResponseBody> body = this.serviceIntegration.queryProviders(coordinate).execute();
			if(body!=null && body.body()!=null && body.isSuccessful()) {
				String str = body.body().string();
				trackingMaxResponseTime(System.currentTimeMillis() - startTime);				
				return gson.fromJson(str, providerListType);
			}else {
				failedRequestCount+=1;
				logger.info("Empty response");
				return new ArrayList<Provider>();
			}            			
		} catch (Exception e) {
			logger.error("query Provider", e);
			failedRequestCount+=1;
			return new ArrayList<Provider>();
		}
	}	
	
	public String getStats() {
		String s = String.format("Response time: max %d, Faield %d, Total: %d, Time %d", this.maxResponseTime, failedRequestCount, this.numOfRequests, this.timeTotal);
		for(int i=0;i<this.timeLimits.length;i++) {
			s = String.format("%s, %d - %d", s, this.timeLimits[i], this.coutingRequestByTimeLimits[i]);
		}
		return s;
	}
}
