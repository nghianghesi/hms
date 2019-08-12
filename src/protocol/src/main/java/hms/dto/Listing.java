package hms.dto;

public class Listing {
	private int page;	
	private int itemPerPage;
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getItemPerPage() {
		return itemPerPage;
	}
	public void setItemPerPage(int itemPerPage) {
		this.itemPerPage = itemPerPage;
	}
}
