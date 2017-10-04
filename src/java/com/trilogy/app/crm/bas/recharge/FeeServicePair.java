/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bas.recharge;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.ServiceSupport;


/**
 * A simple object containing a service and it's corresponding service fee.
 * 
 * Implements hashCode, equals, and comparable methods for effective use
 * in sets, sorting, comparisons, etc.
 *
 * TODO: Create an XGen model for this to generate equals, hashCode,
 * toString, and default getters/setters.
 * 
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class FeeServicePair implements Comparable<FeeServicePair>
{
    public FeeServicePair()
    {    
    }
    
    public FeeServicePair(Context ctx, ServiceFee2 fee)
    {
        setFee(fee);
        setService(getService(ctx));
    }
    
    public FeeServicePair(Service service, ServiceFee2 fee)
    {
        setService(service);
        setFee(fee);
    }
    
    public ServiceFee2 getFee()
    {
        return fee;
    }
    
    public void setFee(ServiceFee2 fee)
    {
        this.fee = fee;
    }
    
    public Service getService()
    {
        return this.service;
    }
    
    public void setService(Service service)
    {
        this.service = service;
    }
    
    /**
     * This method will attempt to lookup the service instance using the service
     * fee if the fee is set but the service is not, or if the service fee's
     * service ID does not match the service's service ID.
     * 
     * @param ctx Operating Context
     * @return Service instance or null if no matching service is set or exists.
     */
    public Service getService(Context ctx)
    {
        if (getFee() != null)
        {
            long serviceId = getFee().getServiceId();
            if (getService() == null
                        || serviceId != getService().getID())
            {
                try
                {
                    setService(fee.getService(ctx));
                }
                catch (HomeException e)
                {
                    new DebugLogMsg(this, "Error looking up service " + serviceId + ".  getService() returning null...", e).log(ctx);
                }
            }
        }
        return getService();
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(FeeServicePair other)
    {
        if (this.equals(other))
        {
            return 0;
        }
        
        if (other == null)
        {
            return -1;
        }

        if (getFee() != null && other.getFee() != null)
        {
        	
        	if(!(getService().getServiceSubType().equals(ServiceSubTypeEnum.DISCOUNT)) &&
        			!(other.getService().getServiceSubType().equals(ServiceSubTypeEnum.DISCOUNT)))
        	{
	            // service with negative fee should be processed first
        		// Although skip this check for discount type services, they should be compared based on execution order only
	            if (getFee().getFee() < 0
	                    && other.getFee().getFee() >= 0)
	            {
	                return -1;
	            }
	            else if (other.getFee().getFee() < 0
	                    && getFee().getFee() >= 0)
	            {
	                return 1;
	            }
        	}
            
            boolean isPackage = getFee().getSource().startsWith("Package");
            boolean isOtherPackage = other.getFee().getSource().startsWith("Package");
            if (isPackage && !isOtherPackage)
            {
                return -1;
            }
            else if (isOtherPackage && !isPackage)
            {
                return 1;
            }
        }
        
        return Service.PROVISIONING_ORDER.compare(getService(), other.getService());
    }
    
    @Override
    public boolean equals(Object o)
    {
        if ( o == this )
        {
            return true;
        }

        if ( o == null
                || o.getClass() != getClass())
        {
            return false;
        }

        return SafetyUtil.safeEquals(getFee(), ((FeeServicePair) o).getFee())
            && SafetyUtil.safeEquals(getService(), ((FeeServicePair) o).getService());
    }

    @Override
    public int hashCode()
    {
       int h = 17;
         
       h = h * 37 + SafetyUtil.hashCode(getService());
       h = h * 37 + SafetyUtil.hashCode(getFee());

       return h;
    }
    
    @Override
    public String toString()
    {
        return new StringBuilder().append(this.getClass().getSimpleName())
        .append("(service: ")
        .append(getService())
        .append(", fee: ")
        .append(getFee())
        .append(")").toString();
    }

    protected ServiceFee2 fee;
    protected Service service;
}
