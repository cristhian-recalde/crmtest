package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

public class HlrServicePredicate implements Predicate 
{
	public HlrServicePredicate( final SubscriberTypeEnum type,  final boolean pflage){
		this.sub_type = type; 
		this.pflage = pflage; 
	}
	
	public boolean f(final Context ctx, final Object obj) throws AbortVisitException
	{
	    final Long id = (Long) obj;
	    try {
	        final Service service = ServiceSupport.getService(ctx, id.longValue()); 
	        if ( sub_type.equals(SubscriberTypeEnum.POSTPAID)){
	            if (pflage)
	            {
	                return service.getProvisionConfigs() != null &&  service.getProvisionConfigs().trim().length() > 0; 
	            }
	            else 
				{
	                return service.getUnprovisionConfigs() != null &&  service.getUnprovisionConfigs().trim().length() > 0; 
				}
	        } else {
	            if (pflage)
				{
	                return service.getPrepaidProvisionConfigs() != null &&  service.getPrepaidProvisionConfigs().trim().length() > 0; 
				}
	            else 
				{
	                return service.getPrepaidUnprovisionConfigs() != null &&  service.getPrepaidUnprovisionConfigs().trim().length() > 0; 
				}
	        }    
			
		} catch ( Exception e){
		    throw new AbortVisitException("faile to get service" + e.getMessage()); 
		}
	}
	
	final boolean pflage; 
	final SubscriberTypeEnum sub_type;  
}
