package hms.kafka.streamming;

import java.util.ArrayList;

public class AccumulateStreamResponse<TItem> extends StreamResponse<java.util.List<TItem>> {
	private int numberOfReceivedPackages = 0;	
	
	@Override
	public void setData(java.util.List<TItem> moredata) {
		if(this.data==null) {
			this.data = new ArrayList<TItem>();
		}
		if(moredata!=null&&moredata.size()>0) {
			this.data.addAll(moredata);
		}
		this.numberOfReceivedPackages += 1;
	}
	
	public boolean checkStatus(int expectedTotal) {
		if(this.numberOfReceivedPackages >= expectedTotal) {
			this.status = STATUS.ready;
			return true;
		}else {
			return false;
		}
	}
}
