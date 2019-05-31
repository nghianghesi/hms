package hms.kafka.streamming;

import java.util.ArrayList;
import java.util.List;

public class AccumulateStreamResponse<TItem> extends StreamResponse<java.util.List<TItem>> {
	public AccumulateStreamResponse(int timeout) {
		super(timeout);		
	}

	private int numberOfReceivedPackages = 0;	
	private List<TItem> accumulatedData = new ArrayList<TItem>();
	
	public boolean collectData(java.util.List<TItem> moredata, int expectedTotal) {
		if(moredata!=null&&moredata.size()>0) {
			this.accumulatedData.addAll(moredata);
		}
		this.numberOfReceivedPackages += 1;
		if(this.numberOfReceivedPackages >= expectedTotal) {
			this.setData(this.accumulatedData);
			return true;
		}else {
			return false;
		}
	}
}
