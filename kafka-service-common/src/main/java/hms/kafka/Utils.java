package hms.kafka;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.lang.reflect.Type;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;
public final class Utils {
	private static final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());


	public static  byte[] convertObjecttoByteArray(Object data) throws IOException {
		JsonWriter writer = dslJson.newWriter();
		dslJson.serialize(writer, data);
		return writer.getByteBuffer();
	}
	
	public static Object convertByteArrayToObject(final Type manifest, byte[] data) throws IOException{
		return dslJson.deserialize(manifest, data, data.length);
	}	
	
	public static byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}	

}
