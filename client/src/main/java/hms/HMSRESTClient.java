package hms;

import hms.dto.ProviderTracking;
import hms.interfaces.IHMSClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HMSRESTClient implements IHMSClient{
	public HMSRESTClient(String Url) {
		Retrofit retrofit = new Retrofit.Builder()
			    .baseUrl("https://api.github.com")
			    .addConverterFactory(GsonConverterFactory.create())
			    .build();
	}
	
	public void TrackingProvider(ProviderTracking tracking) {
		
	}
}
