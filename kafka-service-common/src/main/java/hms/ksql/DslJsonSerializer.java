package hms.ksql;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;

public class DslJsonSerializer<T> 
	implements org.apache.kafka.common.serialization.Serializer<T>{
	private static final DslJson<Object> dslJson = new DslJson<>(
			Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private static final Logger logger = LoggerFactory.getLogger(DslJsonSerializer.class);
	
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {	
	}

	@Override
	public byte[] serialize(String topic, T data) {
		if (data != null) {
			JsonWriter writer = dslJson.newWriter();
			try {
				dslJson.serialize(writer, data);
			} catch (IOException e) {
				logger.error("Serialize error {}", e.getMessage());
				return null;
			}
			return writer.getByteBuffer();
		} else {
			return null;
		}
	}

	@Override
	public void close() {	
	}

}
