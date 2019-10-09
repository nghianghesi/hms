package hms.provider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
public interface IProviderInitializingService {
	CompletableFuture<List<hms.dto.Provider>> loadByZone(String zone);
	CompletableFuture<Boolean> clearByZone(String zone);
	CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto);
}
