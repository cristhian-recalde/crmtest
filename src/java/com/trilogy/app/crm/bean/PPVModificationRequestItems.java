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
package com.trilogy.app.crm.bean;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.xhome.adapter.StaticContextBeanAdapter;

/**
 * Price Plan Version modification request items
 * @author Marcio Marques
 * @since 9.2
 *
 */public class PPVModificationRequestItems extends AbstractPPVModificationRequestItems
{
    private final class ServiceFee2Visitor implements Visitor
    {
        private final Map<ServiceFee2ID, ServiceFee2> fees;


        private ServiceFee2Visitor(Map<ServiceFee2ID, ServiceFee2> fees)
        {
            this.fees = fees;
        }


        public void visit(Context vCtx, Object obj) throws AgentException, AbortVisitException
        {
            com.redknee.app.crm.bean.ServiceFee2 modelFee=(com.redknee.app.crm.bean.ServiceFee2) obj;
            Context appctx = (Context)vCtx.get("app");
            try
            {
                ServiceFee2 fee = (ServiceFee2) new StaticContextBeanAdapter<com.redknee.app.crm.bean.ServiceFee2, ServiceFee2>(
                        com.redknee.app.crm.bean.ServiceFee2.class, 
                        ServiceFee2.class, appctx).adapt(vCtx, modelFee);
                
                fees.put(new ServiceFee2ID(fee.getServiceId(),fee.getPath()),fee);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error populating service fee map: " + e.getMessage(), e).log(vCtx);
            }
        }
    }

    private final class BundleFeeVisitor implements Visitor
    {
        private final Map<Long, BundleFee> fees;


        private BundleFeeVisitor(Map<Long, BundleFee> fees)
        {
            this.fees = fees;
        }


        public void visit(Context vCtx, Object obj) throws AgentException, AbortVisitException
        {
            com.redknee.app.crm.bundle.BundleFee modelFee=(com.redknee.app.crm.bundle.BundleFee) obj;
            Context appctx = (Context)vCtx.get("app");
            try
            {
                BundleFee fee = (BundleFee) new StaticContextBeanAdapter<com.redknee.app.crm.bundle.BundleFee, BundleFee>(
                        com.redknee.app.crm.bundle.BundleFee.class, 
                        BundleFee.class, appctx).adapt(vCtx, modelFee);

                fees.put(Long.valueOf(fee.getId()),fee);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error populating service fee map: " + e.getMessage(), e).log(vCtx);
            }
        }
    }

    public Map<ServiceFee2ID, ServiceFee2> getServiceFees()
    {
        return getServiceFees(ContextLocator.locate());
    }


    public Map<Long, BundleFee> getBundleFees()
    {
        return getBundleFees(ContextLocator.locate());
    }


    public Map<ServiceFee2ID, ServiceFee2> getServiceFees(Context ctx)
    {
        if (serviceFees_ == null)
        {
            try
            {
                PricePlanVersion ppversion = PricePlanSupport.getVersion(ctx, getId(), getVersion());
                if (ppversion!=null)
                {
                    serviceFees_ = ppversion.getServicePackageVersion(ctx).getServiceFees();
                    bundleFees_ = ppversion.getServicePackageVersion(ctx).getBundleFees();
                }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to retrieve price plan " + getId() + " version " + getVersion()
                        + ": " + e.getMessage(), e);
            }
        }
        
        if (serviceFees_!=null)
        {
            final Map<ServiceFee2ID, ServiceFee2> fees=new HashMap<ServiceFee2ID, ServiceFee2>();
        
            try
            {
                Visitors.forEach(ctx, serviceFees_,new ServiceFee2Visitor(fees));
            }
            catch (AgentException e)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this,e.getMessage(),e).log(ctx);
                }
            }
    
            return fees;    
        }
        
        return null;
    }


    public Map<Long, BundleFee> getBundleFees(Context ctx)
    {
        if (bundleFees_ == null)
        {
            try
            {
                PricePlanVersion ppversion = PricePlanSupport.getVersion(ctx, getId(), getVersion());
                if (ppversion!=null)
                {
                    serviceFees_ = ppversion.getServicePackageVersion(ctx).getServiceFees();
                    bundleFees_ = ppversion.getServicePackageVersion(ctx).getBundleFees();
                }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to retrieve price plan " + getId() + " version " + getVersion()
                        + ": " + e.getMessage(), e);
            }
        }
        
        if (bundleFees_!=null)
        {
            final Map<Long, BundleFee> fees=new HashMap<Long, BundleFee>();
    
            try
            {
                Visitors.forEach(ctx, bundleFees_,new BundleFeeVisitor(fees));
            }
            catch (AgentException e)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this,e.getMessage(),e).log(ctx);
                }
            }
            
            return fees;
        }
        
        return null;
    }
    
    public Map<ServiceFee2ID, ServiceFee2> getNewServiceFees(Context ctx)
    {
        final Map<ServiceFee2ID, ServiceFee2> fees=new HashMap<ServiceFee2ID, ServiceFee2>();
        
        try
        {
            Visitors.forEach(ctx, getNewServiceFees(),new ServiceFee2Visitor(fees));
        }
        catch (AgentException e)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,e.getMessage(),e).log(ctx);
            }
        }

        return fees;
    }

    /**
     * This will return the list of all bundle fees from the current version and the service packages in this version
     *
     * @param ctx
     * @return
     */
    public Map<Long, BundleFee> getNewBundleFees(Context ctx)
    {
        final Map<Long, BundleFee> fees=new HashMap<Long, BundleFee>();

        try
        {
            Visitors.forEach(ctx, getNewBundleFees(),new BundleFeeVisitor(fees));
        }
        catch (AgentException e)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,e.getMessage(),e).log(ctx);
            }
        }

        return fees;
    }
}
