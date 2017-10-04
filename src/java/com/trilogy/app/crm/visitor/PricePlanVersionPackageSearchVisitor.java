/*
 * Created on Dec 15, 2005 11:36:11 AM
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

import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePackageFee;

import java.io.Serializable;
import java.util.Map;
import java.util.Iterator;

/**
 * This visitor will look into the PricePlanVersion if it uses a certain ServicePackage. It will only look on the
 * first level. If in the future the recurrent Service Packages in the ServicePackage are enabled this visitor
 * will have to be updated to look into them.
 *
 * @author psperneac
 */
public class PricePlanVersionPackageSearchVisitor implements Visitor,Serializable
{
    protected int servicePackageId;
    protected boolean found=false;

    public PricePlanVersionPackageSearchVisitor(int servicePackageId)
    {
        this.servicePackageId=servicePackageId;
    }

    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        PricePlanVersion ppv=(PricePlanVersion) obj;

        if(ppv.getServicePackageVersion()!=null && ppv.getServicePackageVersion().getPackageFees()!=null)
        {
            Map fees=ppv.getServicePackageVersion().getPackageFees();
            for(Iterator i=fees.keySet().iterator();i.hasNext();)
            {
                ServicePackageFee fee=(ServicePackageFee) fees.get(i.next());
                if(fee.getPackageId()==getServicePackageId())
                {
                    setFound(true);
                    throw new AbortVisitException("found it");
                }
            }
        }
    }

    public int getServicePackageId()
    {
        return servicePackageId;
    }

    public void setServicePackageId(int servicePackageId)
    {
        this.servicePackageId = servicePackageId;
    }

    public boolean isFound()
    {
        return found;
    }

    public void setFound(boolean found)
    {
        this.found = found;
    }
}
