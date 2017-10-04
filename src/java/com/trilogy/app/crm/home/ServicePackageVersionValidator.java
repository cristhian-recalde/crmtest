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
package com.trilogy.app.crm.home;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageStateEnum;
import com.trilogy.app.crm.bean.ServicePackageVersionXInfo;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.bean.core.ServicePackageVersion;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.bean.ServicePackageFeeXInfo;

/**
 * This Validator checks when we create a price plan version and if that version attached a deprecated package then it throws a error
 * 
 * @author anuradha.malvadkar@redknee.com since 9.7.2
 */

public class ServicePackageVersionValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
    
        PricePlanVersion ppv = (PricePlanVersion)obj;
     
       
        if(ppv.getServicePackageVersion()!=null)
        {
            Map packages=ppv.getServicePackageVersion().getPackageFees();

            Home home=(Home) ctx.get(ServicePackageHome.class);

            if(packages!=null && packages.size()>0)
            {
                for(Iterator i=packages.keySet().iterator();i.hasNext();)
                {
              
                  ServicePackageFee fee=(ServicePackageFee) packages.get(i.next());
                  ServicePackage servicePackage = null;
            
                  try {
                      servicePackage = (ServicePackage) home.find(ctx, Integer.valueOf(fee.getPackageId()));
                      
                  }catch(HomeException e)
                  {
                      if(LogSupport.isDebugEnabled(ctx))
                      {
                          new DebugLogMsg(this,e.getMessage(),e).log(ctx);
                      }
                  }
                  
                   if(servicePackage.getState()==ServicePackageStateEnum.DEPRECATED_INDEX)
                   {
                   
                       final String msg = "Attached Package is deprecated " + fee.getPackageId();
                       final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                               ServicePackageFeeXInfo.PACKAGE_ID, msg);
                       exceptions.thrown(ex);
                   
                   }
                  
                }
            }
            exceptions.throwAllAsCompoundException();
        }
    }
}



