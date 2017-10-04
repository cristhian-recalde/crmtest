package com.trilogy.app.crm.client;

import com.trilogy.app.transferfund.corba.provision.BlackWhiteListProvisionRequest;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListProvisionResponse;
import com.trilogy.app.transferfund.corba.provision.BlackWhiteListQueryResponse;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * @author kabhay
 *
 */
public interface ITFAAuxiliaryServiceClient 
{

	
	public BlackWhiteListProvisionResponse[]  addWhiteListBlackList(final Context ctx,BlackWhiteListProvisionRequest[] request)  throws TFAAuxiliarServiceClientException;
	public BlackWhiteListQueryResponse[] queryWhiteBlackList(final Context ctx,String[] msisdn) throws TFAAuxiliarServiceClientException;
	public boolean uploadWhiteBlackList (final Context ctx,String[] msisdns)  throws TFAAuxiliarServiceClientException;
	public BlackWhiteListProvisionResponse[] removeWhiteBlackList(final Context ctx, BlackWhiteListProvisionRequest[] request) throws TFAAuxiliarServiceClientException;
	public boolean changeMsisdn(final Context ctx, String oldMsisdn, String newMsisdn) throws TFAAuxiliarServiceClientException;
}
