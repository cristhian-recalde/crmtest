package com.trilogy.app.crm.esbconnect;


import java.util.ArrayList;

import com.trilogy.app.crm.bean.GenericHTTPConnectionConfig;
import com.trilogy.app.crm.esbconnect.http.GenericHttpClient;
import com.trilogy.app.crm.esbconnect.http.GenericHttpConnectionConstants;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.Subscriber;

public abstract class AbstractESBGenericConnectionGateway implements ESBGenericConnectionGateway {
	
	 public  GenericHttpClient getClient(Context ctx)	throws Exception
	 {
		  GenericHttpClient client_ = null;
		 if ( client_ == null )
			{	
				
			 GenericHTTPConnectionConfig config = (GenericHTTPConnectionConfig) ctx
						.get(GenericHttpConnectionConstants.ESB_HTTP_CONFIG_KEY);
				client_ = new GenericHttpClient(ctx, config);
				client_.open();
			}
			
			return client_; 
	 }
	 public abstract ESBMessageResult getDatefromEsb(Context ctx,Subscriber subscriber,int spid) throws Exception;
	 
}
