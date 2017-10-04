package com.trilogy.app.crm.esbconnect;

import java.util.ArrayList;
import com.trilogy.app.crm.esbconnect.http.GenericHttpClient;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.esbconnect.ESBMessageResult;
import com.trilogy.app.crm.bean.Subscriber;
/**
 * Service Genric Http Connection Config Install logic.
 * 
 * 
 * @author anuradha.malvadkar@redknee.com @9.7.2
 *
 */
public interface ESBGenericConnectionGateway {
	public GenericHttpClient getClient(Context ctx)	throws Exception;
 
	public ESBMessageResult getDatefromEsb(Context ctx,Subscriber subscriber,int spid) throws Exception;

}
