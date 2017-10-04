/*
 * Created on Nov 4, 2005 3:27:53 PM
 *
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
package com.trilogy.app.crm.visitor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * @author psperneac
 */
public class ServicePackageVersionPricePlanPropagationVisitor implements Visitor,Serializable
{
    protected ServicePackageVersion version;
    protected Map packages;
    protected Collection packageIds;

    public ServicePackageVersionPricePlanPropagationVisitor(ServicePackageVersion version)
    {
        super();

        this.version=version;
    }

    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        try
        {
            PricePlan pp=(PricePlan) obj;
            PricePlanVersion ppv=PricePlanSupport.getCurrentVersion(ctx,pp);

            if(containsPackages(ctx,ppv))
            {
                Home ppvHome=(Home) ctx.get(PricePlanVersionHome.class);
                try
                {
                    PricePlanVersion ppv1=(PricePlanVersion) ppv.clone();
                    ppv1.setVersion(0);

                    ppvHome.create(ctx,ppv1);
                }
                catch (CloneNotSupportedException e)
                {
                    throw new AgentException(e);
                }
            }
        }
        catch (HomeException e)
        {
            throw new AgentException(e);
        }
    }

    /**
     * Returns true is the price plan version references the searched package.
     * @param ctx
     * @param ppv
     * @return
     */
    private boolean containsPackages(Context ctx, PricePlanVersion ppv)
    {
        if(ppv.getServicePackageVersion()!=null && ppv.getServicePackageVersion().getPackageFees()!=null)
        {
            for(Iterator i=ppv.getServicePackageVersion().getPackageFees().values().iterator();i.hasNext();)
            {
                ServicePackageFee fee=(ServicePackageFee) i.next();
                if(version.getId()==fee.getPackageId())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public ServicePackageVersion getVersion()
    {
        return version;
    }

    public void setVersion(ServicePackageVersion version)
    {
        this.version = version;
    }
}
