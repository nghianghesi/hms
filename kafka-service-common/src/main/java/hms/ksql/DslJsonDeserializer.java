package hms.ksql;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;

public class DslJsonDeserializer<T> 
implements org.apache.kafka.common.serialization.Deserializer<T>{
private static final DslJson<Object> dslJson = new DslJson<>(
		Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private static final Logger logger = LoggerFactory.getLogger(DslJsonDeserializer.class);

	private Class<T> manifest;
	public DslJsonDeserializer(Class<T> manifest) {
		this.manifest = manifest;
	}
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}
	@Override
	public T deserialize(String topic, byte[] data) {
		try {
			return dslJson.deserialize(manifest, data, data.length);
		} catch (IOException e) {
			logger.error("Deserialize error {}", e.getMessage());
			return null;
		}
	}
	
	@Override
	public void close() {
	}
}
