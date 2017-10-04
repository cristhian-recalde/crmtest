package com.trilogy.app.crm.web.service;

/**
 * @author ltse
 *
 *	Thrown when service provider object not found for the imsiPrefix
 */
public class ServiceProviderNotFoundException 
	extends Exception {
	
	public ServiceProviderNotFoundException(String msg)
	{
		super(msg);
	}


}
