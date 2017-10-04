package com.trilogy.app.crm.util;

import java.util.Collection;
import java.util.HashSet;
import com.trilogy.app.crm.bean.ServiceFee2ID;



public class SubscriberServicesUtil {
	
	public static final String DEFAULT_PATH = "-";
	
	public static String getKeyForSubscriberServices(Long Id, String path) {
		if (path == null || path.isEmpty()) {
			path = "-";
		}
		return Id + path;
	}
	
	/**
	 * Returns <code>true</code> if the Collection<ServiceFee2ID> contains the
	 * specified serviceId.
	 * 
	 * @param serviceFee2IDs
	 * @param serviceId
	 * @return boolean
	 */
	public static boolean containsServiceId(Collection<ServiceFee2ID> serviceFee2IDs, long serviceId) {
		if (serviceFee2IDs != null && serviceFee2IDs.size() > 0) {
			for (ServiceFee2ID serviceFee2ID : serviceFee2IDs) {
				if (serviceFee2ID != null && serviceFee2ID.getServiceId() == serviceId) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns a Collection of serviceIds from the Collection of serviceFee2IDs
	 * input. Returns <code>null</code> if serviceFee2IDs input is null or empty.
	 * 
	 * @param serviceFee2IDs
	 * @return Collection<Long>
	 */
	public static Collection<Long> getServiceIds(Collection<ServiceFee2ID> serviceFee2IDs) {
		Collection<Long> serviceIds = new HashSet<Long>();
		if (serviceFee2IDs != null && serviceFee2IDs.size() > 0) {
			for (ServiceFee2ID serviceFee2ID : serviceFee2IDs) {
				serviceIds.add(serviceFee2ID.getServiceId());
			}
		}
		return serviceIds;
	}
}
