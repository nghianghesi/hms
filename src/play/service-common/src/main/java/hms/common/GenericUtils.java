package hms.common;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

public final class GenericUtils {
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<T> c, T[] a) {
	    return c.size()>a.length ?
	        c.toArray((T[])Array.newInstance(a.getClass().getComponentType(), c.size())) :
	        c.toArray(a);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<T> list) {
	    T[] toR = (T[]) java.lang.reflect.Array.newInstance(list.get(0)
	                                           .getClass(), list.size());
	    for (int i = 0; i < list.size(); i++) {
	        toR[i] = list.get(i);
	    }
	    return toR;
	}	
}
