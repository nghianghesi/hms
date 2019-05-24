package hms;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import hms.dto.Coordinate;
import hms.dto.Provider;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class HMSRESTClient{
	public interface HMSServiceIntegration{
		@POST("/provider/geoquery")
		Call<ResponseBody> queryProviders(@Body Coordinate tracking);
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
	private com.google.gson.Gson gson = new com.google.gson.Gson();
	Type providerListType = new TypeToken<ArrayList<Provider>>(){}.getType();

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
	
	public List<Provider> queryProviders(Coordinate coordinate) {
		try {			      
			long startTime = System.currentTimeMillis();
			
			ResponseBody body = this.serviceIntegration.queryProviders(coordinate).execute().body();
			List<Provider> res = null;
			if(body!=null) {
				String str = body.string();
				res = 	gson.fromJson(str, providerListType);				
				if(res!=null && res.size()>0) {
					logger.info("providers {}", res.size());
					for(int i=0;i<res.size()&&i<3;i++) {
						logger.info("providers {} {}", res.get(i).getProviderid(), res.get(i).getZone());
					}
				}else {
					logger.info("Empty providers");
				}
			}else {
				logger.info("Empty providers");
			}            
			trackingMaxResponseTime(System.currentTimeMillis() - startTime);
		    return res;
		} catch (Exception e) {
			logger.error("query Provider", e);
			return new ArrayList<Provider>();
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
