package hms;
public class AppStart {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*Thread t = new Thread(hms.AppStarter::start);
		t.start();
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		AppStarter.start();
	}

}
