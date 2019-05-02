package hms.kafka.streamming;

import java.util.ArrayList;

public class AccumulateStreamResponse<TItem> extends StreamResponse<java.util.List<TItem>> {
	private int numberOfReceivedPackages = 0;	
	
	@Override
	public void setData(java.util.List<TItem> data) {
		if(data==null) {
			this.data = new ArrayList<TItem>();
		}
		this.data.addAll(data);
		this.numberOfReceivedPackages += 1;
	}
	
	public boolean checkStatus(int expectedTotal) {
		if(this.numberOfReceivedPackages== expectedTotal) {
			this.status = STATUS.ready;
			return true;
		}else {
			return false;
		}
	}
}
