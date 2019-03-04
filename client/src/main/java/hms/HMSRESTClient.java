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
	
	public HMSRESTClient(String Url, Logger logger) {
		this.serviceURL = Url;
		this.logger = logger;
		buildIntegration();
	}
	
	public void trackingProvider(ProviderTracking tracking) {
		try {			
			//this.serviceIntegration.trackingProvider(tracking).execute().body().string();
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
}
