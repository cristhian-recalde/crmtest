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
package com.trilogy.app.crm.client.alcatel;

import java.util.Date;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This interface will be installed for testing.
 * Depending on the license state, the relevant methods will return success (by way of no exception)
 * or an error (by way of an exception.
 * 
 * @author angie.li@redknee.com
 *
 */
public class TestAlcatelProvisioningImpl implements AlcatelProvisioning {

    public static String LICENSE = "TestAlcatelProvisioningImpl";
    public static String ERROR_LICENSE = "TestAlcatelProvisioningImpl.error";
    
    public void createService(Context ctx, Service service,
            Subscriber subscriber) throws AlcatelProvisioningException 
    {
        if(LicensingSupportHelper.get(ctx).isLicensed(ctx, ERROR_LICENSE))
        {
            new AlcatelProvisioningException("Create error", 100);
        }
        LogSupport.debug(ctx, this, "Created Alcatel Service=" + service.getID() + "-" + service.getName() 
                + " for subscriber=" + subscriber.getId());
    }

    public void deleteAccount(Context ctx, Service service,
            Subscriber subscriber) throws AlcatelProvisioningException 
    {
        if(LicensingSupportHelper.get(ctx).isLicensed(ctx, ERROR_LICENSE))
        {
            new AlcatelProvisioningException("Delete error", 101);
        }
        LogSupport.debug(ctx, this, "Deleted Alcatel Account for service=" + service.getID() + "-" + service.getName() 
                + " for subscriber=" + subscriber.getId());
    }

    public AccountUsage queryAccountUsage(Context ctx, Service service,
            Subscriber subscriber) throws AlcatelProvisioningException 
    {
        if(LicensingSupportHelper.get(ctx).isLicensed(ctx, ERROR_LICENSE))
        {
            new AlcatelProvisioningException( "Query Account error", 102);
        }
        
        AccountUsage usage = new AccountUsage();
        usage.setAccountId(subscriber.getMSISDN());
        usage.setBytesReceived(900);
        usage.setBytesSent(10000);
        usage.setBytesTransferred(50);
        usage.setEndDate(new Date());
        usage.setStartDate(new Date(0));
        
        LogSupport.debug(ctx, this, "Returning Alcatel Query for service=" + service.getID() + "-" + service.getName() 
                + " for subscriber=" + subscriber.getId() + " USAGE=" + usage.toString());
        return usage;
    }

    public void removeService(Context ctx, Service service,
            Subscriber subscriber) throws AlcatelProvisioningException 
    {
        if(LicensingSupportHelper.get(ctx).isLicensed(ctx, ERROR_LICENSE))
        {
            new AlcatelProvisioningException("Remove account error", 103);
        }
        LogSupport.debug(ctx, this, "Remove Alcatel Service=" + service.getID() + "-" + service.getName() 
                + " for subscriber=" + subscriber.getId());
    }

    public void resumeService(Context ctx, Service service,
            Subscriber subscriber) throws AlcatelProvisioningException 
    {
        if(LicensingSupportHelper.get(ctx).isLicensed(ctx, ERROR_LICENSE))
        {
            new AlcatelProvisioningException( "Resume account error", 104);
        }
        LogSupport.debug(ctx, this, "Resume Alcatel Service=" + service.getID() + "-" + service.getName() 
                + " for subscriber=" + subscriber.getId());
    }

    public void suspendService(Context ctx, Service service,
            Subscriber subscriber) throws AlcatelProvisioningException 
    {
        if(LicensingSupportHelper.get(ctx).isLicensed(ctx, ERROR_LICENSE))
        {
            new AlcatelProvisioningException( "Suspended account error", 105);
        }
        LogSupport.debug(ctx, this, "Suspend Alcatel Service=" + service.getID() + "-" + service.getName() 
                + " for subscriber=" + subscriber.getId());
    }

    public void updateAccount(Context ctx, Service service,
            Subscriber subscriber) throws AlcatelProvisioningException 
    {
        if(LicensingSupportHelper.get(ctx).isLicensed(ctx, ERROR_LICENSE))
        {
            new AlcatelProvisioningException("Update account error", 106);
        }
        LogSupport.debug(ctx, this, "Update Alcatel Account=" + service.getID() + "-" + service.getName() 
                + " for subscriber=" + subscriber.getId());
    }

    @Override
    public void createAccount(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AccountInfo getAccount(Context ctx, Service service, Subscriber subscriber) throws AlcatelProvisioningException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
