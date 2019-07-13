package hms.hub.entities;

import java.util.List;
import java.util.UUID;
import xyz.morphia.geo.Point;

public interface HubNodeEntity { 
	public UUID getHubid();
	public void setHubid(UUID hubid);
	public String getName();
	public void setName(String name);
	public String getZone();
	public void setZone(String name);
	public Point getLocation();
	public void setLocation(Point location);
	public double getLatitudeRange();
	public void setLatitudeRange(double latitudeRange);
	public double getLongitudeRange();
	public void setLongitudeRange(double longitudeRange);
	public double getMargin();
	public void setMargin(double margin);
	public List<HubSubEntity> getSubHubs();
}
