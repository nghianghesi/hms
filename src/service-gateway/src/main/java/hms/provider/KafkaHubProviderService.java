package hms.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import hms.IProcessingService;
import hms.KafkaHMSMeta;
import hms.dto.GeoQuery;
import hms.dto.HubProviderGeoQuery;
import hms.dto.Provider;
import hms.hub.IHubService;
import hms.kafka.streamming.SplitStreamRoot;
import hms.kafka.provider.KafkaProviderTopics;
import hms.kafka.provider.ZoneConfigClass;
import hms.kafka.streamming.MonoStreamRoot;
import hms.provider.IAsynProviderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class KafkaHubProviderService implements IAsynProviderService, IProcessingService{
	private static final Logger logger = LoggerFactory.getLogger(KafkaHubProviderService.class);
	
	@Autowired
	private Environment config;
	@Autowired
	private KafkaProviderTopics topicSettings;
	@Autowired
	private IHubService hubservice;
	
	
	MonoStreamRoot<hms.dto.HubProviderTracking, Boolean>  trackingProviderStream;
	SplitStreamRoot<hms.dto.HubProviderGeoQuery, hms.dto.Provider>  queryProvidersStream;
	String server, rootid;	
	
	private String applyHubIdTemplateToRepForTopic(String topic, Object value) {
		return topic.replaceAll("\\{hubid\\}", value!=null ? value.toString().replace('.', '_').replace('-', '_') : "");
	}
	
	private String getZoneByHubid(UUID hubid) {
		return this.hubservice.getZone(hubid);
	}
	
	public void start() {	
		if(!StringUtils.isEmpty(config.getProperty(KafkaHMSMeta.ServerConfigKey))
				&& !StringUtils.isEmpty(config.getProperty(KafkaHMSMeta.RootIdConfigKey))) {
			server = config.getProperty(KafkaHMSMeta.ServerConfigKey);
			rootid = config.getProperty(KafkaHMSMeta.RootIdConfigKey);
			trackingProviderStream = new MonoStreamRoot<hms.dto.HubProviderTracking, Boolean>(){
				@Override
				protected Logger getLogger() {
					return logger;
				}
				
				@Override
				protected String getGroupid() {
					return rootid;
				}

				@Override
				protected String applyTemplateToRepForTopic(String topic, Object value) {
					return applyHubIdTemplateToRepForTopic(topic, ((hms.dto.HubProviderTracking)value).getHubid());
				}		
				
				@Override
				protected String getStartTopic() {
					return topicSettings.getTrackingTopic()+"{hubid}";
				}				

				@Override
				protected String getReturnTopic() {
					return rootid + topicSettings.getTrackingTopic()+KafkaHMSMeta.ReturnTopicSuffix;
				}

				@Override
				protected Class<Boolean> getTConsumeManifest() {
					return Boolean.class;
				}

				@Override
				protected String getZone(hms.dto.HubProviderTracking data) {
					return getZoneByHubid(data.getHubid());
				}				
			};
			
			queryProvidersStream = new SplitStreamRoot<hms.dto.HubProviderGeoQuery, hms.dto.Provider>(){
				@SuppressWarnings("unchecked")
				private Class<? extends ArrayList<hms.dto.Provider>> template = (Class<? extends ArrayList<hms.dto.Provider>>)(new ArrayList<hms.dto.Provider>()).getClass();
				@Override
				protected Logger getLogger() {
					return logger;
				}
				
				@Override
				protected String getGroupid() {
					return rootid;
				}
				
				@Override
				protected String applyTemplateToRepForTopic(String topic, Object value) {
					return applyHubIdTemplateToRepForTopic(topic, ((hms.dto.HubProviderGeoQuery)value).getHubid());
				}		
				
				@Override
				protected String getStartTopic() {
					return topicSettings.getQueryTopic()+"{hubid}";
				}				
				
				@Override
				protected String getReturnTopic() {
					return rootid + topicSettings.getQueryTopic()+KafkaHMSMeta.ReturnTopicSuffix;
				}
				
				@Override
				protected Class<? extends ArrayList<Provider>> getTConsumeManifest() {
					return template;
				}

				@Override
				protected String getZone(hms.dto.HubProviderGeoQuery data) {
					return getZoneByHubid(data.getHubid());
				}
			};
			
			ObjectMapper objectMapper = new ObjectMapper();
			String zonesConfig = config.getProperty(KafkaHMSMeta.ZoneServerConfigKey);
			List<ZoneConfigClass> zones;
			try {
				zones = objectMapper.readValue(zonesConfig, new TypeReference<List<ZoneConfigClass>>(){});
				zones.forEach((cf)->{
					logger.info("Zone {} {}", cf.getName(), cf.getServer());				
					trackingProviderStream.addZones(cf.getName(), cf.getServer());
					queryProvidersStream.addZones(cf.getName(), cf.getServer());
				});				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			trackingProviderStream.run();
			queryProvidersStream.run();
		}else {
			logger.error("Missing {} {} configuration", KafkaHMSMeta.ServerConfigKey, KafkaHMSMeta.RootIdConfigKey);
			throw new Error(String.format("Missing {} {} configuration", 
					KafkaHMSMeta.ServerConfigKey,KafkaHMSMeta.RootIdConfigKey));
		}		
	}

	@Override
	public CompletableFuture<Boolean> asynTracking(hms.dto.ProviderTracking trackingdto) {
		UUID hubid = this.hubservice.getHostingHubId(trackingdto.getLatitude(), trackingdto.getLongitude());
		hms.dto.HubProviderTracking data = new hms.dto.HubProviderTracking(hubid,
				trackingdto.getProviderid(),
				trackingdto.getLatitude(), trackingdto.getLongitude());		
		return trackingProviderStream.startStream(data);
	}

	@Override
	public CompletableFuture<? extends List<hms.dto.Provider>> asynQueryProviders(GeoQuery query) {
		List<UUID> hubids = this.hubservice.getConveringHubs(query);
		List<HubProviderGeoQuery> querydata = new ArrayList<HubProviderGeoQuery>();
		for(UUID hid : hubids) {
			querydata.add(new HubProviderGeoQuery(hid, query.getLatitude(), query.getLongitude(), query.getDistance()));
		}		
		return queryProvidersStream.startStream(querydata);
	}			

	@Override
	public void close() throws IOException {
		this.trackingProviderStream.shutDown();		
		this.queryProvidersStream.shutDown();
	}	
}

