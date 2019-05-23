package hms;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import hms.dto.Provider;
import hms.dto.ProviderTracking;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;


public class HMSRESTClient{
	public interface HMSServiceIntegration{
		@POST("/provider/tracking")
		Call<ResponseBody> trackingProvider(@Body ProviderTracking tracking);
		
		@POST("/provider/clear")
		Call<ResponseBody> clearProvider();
		
		@POST("/provider/init")
		Call<ResponseBody> initProvider(@Body Provider provider);	
		
		@POST("/provider/get-by-zone")
		Call<ResponseBody> loadByZone(@Body String zone);		
	}
	
	private HMSServiceIntegration serviceIntegration;
	private Logger logger;
	private String serviceURL;
	
	Type providerListType = new TypeToken<ArrayList<Provider>>(){}.getType();

	private com.google.gson.Gson gson = new com.google.gson.Gson();
	private void buildIntegration() {
		Retrofit retrofit = new Retrofit.Builder()
			    .baseUrl(serviceURL)
			    .addConverterFactory(ScalarsConverterFactory.create())
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
	
	public String getStats() {
		String s = String.format("Response time: max %d", this.maxResponseTime);
		for(int i=0;i<this.timeLimits.length;i++) {
			s = String.format("%s, %d - %d", s, this.timeLimits[i], this.coutingRequestByTimeLimits[i]);
		}
		return s;
	}
}
