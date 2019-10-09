package hms.kafka.streamming;

import java.util.ArrayList;

public class AccumulateStreamResponse<TItem> extends StreamResponse<java.util.ArrayList<TItem>> {
	public AccumulateStreamResponse(int timeout) {
		super(timeout);		
	}

	private int numberOfReceivedPackages = 0;	
	private ArrayList<TItem> accumulatedData = new ArrayList<TItem>();
	
	public int getNumberOfReceivedPackages() {
		return this.numberOfReceivedPackages;
	}
	
	public void collectData(ArrayList<TItem> arrayList, int expectedTotal) {
		if(arrayList!=null&&arrayList.size()>0) {
			this.accumulatedData.addAll(arrayList);
		}
		this.numberOfReceivedPackages += 1;
		if(this.numberOfReceivedPackages >= expectedTotal) {
			this.setData(this.accumulatedData);
		}
	}
}
