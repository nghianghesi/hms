package hms.ksql;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class DslJsonSerDes<T> implements Serde<T> {

	private Serializer<T> ser;
	private Deserializer<T> des;
	public DslJsonSerDes(Class<T> manifest) {
		ser = new DslJsonSerializer<>();
		des = new DslJsonDeserializer<>(manifest);
	}
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		
	}

	@Override
	public void close() {
	
	}

	@Override
	public Serializer<T> serializer() {		
		return this.ser;
	}

	@Override
	public Deserializer<T> deserializer() {
		return this.des;
	}

}
