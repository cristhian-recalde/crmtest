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
package com.trilogy.app.crm.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.formula.functions.Islogical;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.LicenseCheck;
import com.trilogy.framework.xhome.filter.AndPredicate;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.msp.SpidAwareXInfo;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.snippet.log.Logger;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.support.SpidSupport;


/**
 * Provides a predicate for determining which ServiceTypeEnum values are
 * appropriate for a given Service. The Service is assumed to exist within the
 * context as AbstractWebControl.BEAN. A type of service is appropriate if it is
 * licensed, and supports the technology type of the service.
 *
 * @author gary.anderson@redknee.com
 */
public class AppropriateServiceTypePredicate
    implements Predicate
{
    /**
     * {@inheritDoc}
     */
    public boolean f(final Context context, final Object object)
    {
        final boolean appropriate;

        final Service service = (Service)context.get(AbstractWebControl.BEAN);

        if (!(object instanceof ServiceTypeEnum))
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, "Unexpected object: " + object, null).log(context);
            }

            appropriate = false;
        }
        else if (service == null)
        {
            new MinorLogMsg(
                this,
                "Could not find service for determining appropriate service type.",
                null).log(context);
            appropriate = false;
        }
        else
        {
            final ServiceTypeEnum serviceType = (ServiceTypeEnum)object;

            // A type is appropriate if licensed and it supports the technology
            // type of the service.
            appropriate = isAppropriate(context, service.getTechnology(), serviceType);
        }

        return appropriate;
    }


    /**
     * Determines whether or not the given service type is appropriate given a
     * particular technology, and considering current licensing.
     *
     * @param context The operating context.
     * @param technology The technology type to consider.
     * @param serviceType The service type.
     * @return True if the given service type is appropriate; false otherwise.
     */
    public static boolean isAppropriate(final Context context, final TechnologyEnum technology,
        final ServiceTypeEnum serviceType)
    {
        return isAppropriateTechnology(context, technology, serviceType) && isLicensed(context, serviceType);
    }


    /**
     * Determines whether or not the given service type is appropriate for the
     * given technology type.
     *
     * @param context The operating context.
     * @param technologyType The technology type.
     * @param serviceType The type of service.
     * @return True if the service type is appropriate for the given technology
     * type; false otherwise.
     */
    private static boolean isAppropriateTechnology(final Context context, final TechnologyEnum technologyType,
        final ServiceTypeEnum serviceType)
    {
        final boolean appropriate;
        switch (serviceType.getIndex())
        {
            // Currently, only EVDO is filtered according to type.
            case ServiceTypeEnum.EVDO_INDEX:
            {
                appropriate = technologyType == TechnologyEnum.CDMA;
                break;
            }
            
            case ServiceTypeEnum.WIMAX_INDEX:
            {
                appropriate = technologyType == TechnologyEnum.CDMA;
                break;
            }
            case ServiceTypeEnum.BLACKBERRY_INDEX:
            {
            appropriate = technologyType == TechnologyEnum.CDMA || technologyType == TechnologyEnum.TDMA
                    || technologyType == TechnologyEnum.GSM;
                break;
            }
            default:
            {
                appropriate = true;
            }
        }

        return appropriate;
    }


    /**
     * Determines whether or not the given service type is licensed for use.
     *
     * @param context The operating context.
     * @param serviceType The type of service.
     * @return True if the service is licensed for use; false otherwise.
     */
    private static boolean isLicensed(final Context context, final ServiceTypeEnum serviceType)
    {
    	
  
    	if(serviceType.equals(ServiceTypeEnum.WIMAX))
    	{
    		
    		boolean wimax=getWimaxSupport(context);
    		
    		return wimax;
    		
    	}
    	
        final AndPredicate checks = new AndPredicate();

        for (final String licenseKey : SERVICE_TYPE_LICENSES.get(serviceType))
        {
            checks.add(new LicenseCheck(licenseKey));
        }

        return checks.f(context, null);
    }
    
    protected static int findSpid(Context ctx, int defaultSpid)
    {
        Spid spidObj = MSP.getBeanSpid(ctx);
        if(spidObj==null)
        {
           
            Object parentBean = ctx.get(AbstractWebControl.BEAN);
            if (parentBean instanceof SpidAware)
                defaultSpid = ((SpidAware) parentBean).getSpid();
        }
        else
        {
            defaultSpid = spidObj.getSpid();
            Logger.debug(ctx, AppropriateServiceTypePredicate.class, "defaultSpid " + defaultSpid, null);
        }
        return defaultSpid;
    }
    
    protected static boolean getWimaxSupport(Context context)
    {
    
    		try
    		{
			
    			int spid = findSpid(context, -1);
    			Logger.debug(context, AppropriateServiceTypePredicate.class, "Spid set for Service" + spid, null);
    			
    			CRMSpid crmspid = SpidSupport.getCRMSpid(context, spid);
    			Logger.debug(context, AppropriateServiceTypePredicate.class, "Spid Level WimaxSupport - " +crmspid.getWimaxSupport(), null);
    			
    			return crmspid.getWimaxSupport();
    		}
    		catch ( HomeException h)
    		{
			
    		}
    	return false;
    }


    /**
     * Data map of all the licenses required by service types.
     */
    private static final Map<ServiceTypeEnum, Collection<String>> SERVICE_TYPE_LICENSES;
    static
    {
        SERVICE_TYPE_LICENSES = new HashMap<ServiceTypeEnum, Collection<String>>();
        for (int n = 0; n < ServiceTypeEnum.COLLECTION.getSize(); ++n)
        {
            final ServiceTypeEnum key = (ServiceTypeEnum)ServiceTypeEnum.COLLECTION.get((short)n);
            SERVICE_TYPE_LICENSES.put(key, new ArrayList<String>());
        }

        // Those requiring AIRTIME.
        SERVICE_TYPE_LICENSES.get(ServiceTypeEnum.VOICEMAIL).add(LicenseConstants.AIRTIME_LICENSE_KEY);
        SERVICE_TYPE_LICENSES.get(ServiceTypeEnum.VOICE).add(LicenseConstants.AIRTIME_LICENSE_KEY);
        SERVICE_TYPE_LICENSES.get(ServiceTypeEnum.SMS).add(LicenseConstants.AIRTIME_LICENSE_KEY);
        SERVICE_TYPE_LICENSES.get(ServiceTypeEnum.DATA).add(LicenseConstants.AIRTIME_LICENSE_KEY);
        SERVICE_TYPE_LICENSES.get(ServiceTypeEnum.EVDO).add(LicenseConstants.AIRTIME_LICENSE_KEY);

        // Feature based licenses.
        SERVICE_TYPE_LICENSES.get(ServiceTypeEnum.EVDO).add(LicenseConstants.EVDO_LICENSE);
        SERVICE_TYPE_LICENSES.get(ServiceTypeEnum.ALCATEL_SSC).add(LicenseConstants.ALCATEL_LICENSE);
        SERVICE_TYPE_LICENSES.get(ServiceTypeEnum.BLACKBERRY).add(LicenseConstants.BLACKBERRY_LICENSE);
       
   
        
    }

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
}
