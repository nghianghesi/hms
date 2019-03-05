package hms;


import org.slf4j.Logger;

import hms.dto.Provider;
import hms.dto.ProviderTracking;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class HMSRESTClient{
	public interface HMSServiceIntegration{
		@POST("/provider/tracking")
		Call<ResponseBody> trackingProvider(@Body ProviderTracking tracking);
		@GET("/provider/clear")
		Call<ResponseBody> clearProvider();
		@POST("/provider/init")
		Call<ResponseBody> initProvider(@Body Provider provider);
	}
	
	private HMSServiceIntegration serviceIntegration;
	private Logger logger;
	private String serviceURL;
	private void buildIntegration() {
		Retrofit retrofit = new Retrofit.Builder()
			    .baseUrl(serviceURL)
			    .addConverterFactory(GsonConverterFactory.create())
			    .build();
		
		this.serviceIntegration = retrofit.create(HMSServiceIntegration.class);
	}
	
	private long maxResponseTime;
	private long timeLimits[] = new long[] {1000,1500,2000,2500,3000,5000,10000, 15000, 20000};
	private long coutingRequestByTimeLimits[] = new long[timeLimits.length] ;
	
	private void trackingMaxResponseTime(long elapsedTime) {
		synchronized(this) {
			maxResponseTime = Math.max(this.maxResponseTime, elapsedTime);
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
	
	public void trackingProvider(ProviderTracking tracking) {
		try {			      
			long startTime = System.currentTimeMillis();
			this.serviceIntegration.trackingProvider(tracking).execute().body().string();
			trackingMaxResponseTime(System.currentTimeMillis() - startTime);
		    
		} catch (Exception e) {
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

	public void clearProvider() {
		try {			
			this.serviceIntegration.clearProvider().execute().body().string();			
		} catch (Exception e) {
			logger.error("Tracking Provider", e);
		}
	}
	
	public String getStats() {
		String s = String.format("Response time: max %d", this.maxResponseTime);
		for(int i=0;i<this.timeLimits.length;i++) {
			s = String.format("%s, %d - %d", s, this.timeLimits[i], this.coutingRequestByTimeLimits[i]);
		}
		return s;
	}
}
