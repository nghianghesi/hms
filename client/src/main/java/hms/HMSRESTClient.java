package hms;

import java.io.IOException;

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
	}
	
	private HMSServiceIntegration serviceIntegration;
	public HMSRESTClient(String Url) {
		Retrofit retrofit = new Retrofit.Builder()
			    .baseUrl(Url)
			    .addConverterFactory(GsonConverterFactory.create())
			    .build();
		this.serviceIntegration = retrofit.create(HMSServiceIntegration.class);
	}
	
	public void trackingProvider(ProviderTracking tracking) {
		try {			
			this.serviceIntegration.trackingProvider(tracking).execute().body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	public void clearProvider() {
		try {			
			this.serviceIntegration.clearProvider().execute().body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
