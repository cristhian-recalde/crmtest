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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Comparator;

import com.trilogy.app.crm.bean.ChargingTemplate;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileIdentitySupport;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.MultiSelectWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Multi select web control used to display bundle profiles.
 * @author Marcio Marques
 * @since 8.5
 * 
 */
public class BundleProfileMultiSelectWebControl extends MultiSelectWebControl
{
    boolean retrieveAuxiliaryBundles_ = true;
    boolean retrievePricePlanBundles_ = true;
    boolean auxBundlesforCug = false;
    int bundleSegmentEnumIndex = 0;
    private static Comparator COMPARATOR = new Comparator(){

        @Override
        public int compare(Object arg0, Object arg1)
        {
            BundleProfile b0= (BundleProfile) arg0;
            BundleProfile b1= (BundleProfile) arg1;
            
            if (b0==null || b1==null || b0.getBundleId()==b1.getBundleId())
            {
                return 0;
            }
            else if (b0.getBundleId()>b1.getBundleId())
            {
                return 1;
            }
            else
            {
                return -1;
            }

        }
        
    };
    
    public BundleProfileMultiSelectWebControl()
    {
        super(BundleProfileHome.class, BundleProfileIdentitySupport.instance(), 
                new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                     BundleProfile bean = (BundleProfile)obj;
                     out.print(bean.getBundleId() + " - " + bean.getName());
                 }
                 catch (Exception e)
                 {
                     if (LogSupport.isDebugEnabled(ctx)) {
                         new DebugLogMsg(this, "Error during output of bundle profile multi-select webcontrol. ", e).log(ctx); 
                     } 
                 }
             }
        });

        setComparator(COMPARATOR);    
    }

    public BundleProfileMultiSelectWebControl(boolean pricePlanBundles, boolean auxiliaryBundles)
    {
        super(BundleProfileHome.class, BundleProfileIdentitySupport.instance(), 
                new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                     BundleProfile bean = (BundleProfile)obj;
                     out.print(bean.getBundleId() + " - " + bean.getName());
                 }
                 catch (Exception e)
                 {
                     if (LogSupport.isDebugEnabled(ctx)) {
                         new DebugLogMsg(this, "Error during output of bundle profile multi-select webcontrol. ", e).log(ctx); 
                     } 
                 }
             }
        });
        setComparator(COMPARATOR);    
        retrievePricePlanBundles_ = pricePlanBundles;
        retrieveAuxiliaryBundles_ = auxiliaryBundles;
    }

    public BundleProfileMultiSelectWebControl(boolean prepaidAuxiliaryBundles, int index)
    {
        super(BundleProfileHome.class, BundleProfileIdentitySupport.instance(), 
               new com.redknee.framework.xhome.webcontrol.OutputWebControl()
       {
            public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
            {
                try {
                    BundleProfile bean = (BundleProfile)obj;
                     out.print(bean.getBundleId() + " - " + bean.getName());
                 }
                 catch (Exception e)
                 {
                     if (LogSupport.isDebugEnabled(ctx)) {
                         new DebugLogMsg(this, "Error during output of bundle profile multi-select webcontrol. ", e).log(ctx); 
                     } 
                 }
             }
        });
        setComparator(COMPARATOR);    
        auxBundlesforCug = prepaidAuxiliaryBundles;
        bundleSegmentEnumIndex = index ;
    }
    
    public Home getHome(Context ctx)
    {
        Context subCtx = filterSubscriberType(filterBundleType(filterSpid(ctx)));
       return (Home) subCtx.get(BundleProfileHome.class);  
    }

    private Context filterBundleType(final Context ctx)
    {
        final Home originalHome = (Home) ctx.get(BundleProfileHome.class);
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object chargingTemplate = ctx.get(ChargingTemplate.class);
        int spid = -1;
        
        if (!retrieveAuxiliaryBundles_)
        {
            final Predicate auxiliaryFieldPredicate = new EQ(BundleProfileXInfo.AUXILIARY, Boolean.FALSE);
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, auxiliaryFieldPredicate);
            final Context subCtx = ctx.createSubContext();
            subCtx.put(BundleProfileHome.class, newHome);
            return subCtx;
        }
        else if (!retrievePricePlanBundles_)
        {
            final Predicate auxiliaryFieldPredicate = new EQ(BundleProfileXInfo.AUXILIARY, Boolean.TRUE);
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, auxiliaryFieldPredicate);
            final Context subCtx = ctx.createSubContext();
            subCtx.put(BundleProfileHome.class, newHome);
            return subCtx;
        }
        else if(auxBundlesforCug)
        {
        	BundleSegmentEnum bundleType = null;
        	final And auxiliaryBundleClause = new And();
        	auxiliaryBundleClause.add(new EQ(BundleProfileXInfo.AUXILIARY, Boolean.TRUE));
        	if(BundleSegmentEnum.PREPAID_INDEX == bundleSegmentEnumIndex)
        	{
        		bundleType = BundleSegmentEnum.PREPAID;
        	}
        	else
        	{
        		bundleType = BundleSegmentEnum.POSTPAID;
        	}
        	auxiliaryBundleClause.add(new EQ(BundleProfileXInfo.SEGMENT, bundleType));
        	final Predicate auxiliaryFieldPredicate = auxiliaryBundleClause;
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, auxiliaryFieldPredicate);
            final Context subCtx = ctx.createSubContext();
            subCtx.put(BundleProfileHome.class, newHome);
            return subCtx;
        }
        return ctx;
    }

    private static Context filterSpid(final Context ctx)
    {
        final Home originalHome = (Home) ctx.get(BundleProfileHome.class);
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object chargingTemplate = ctx.get(ChargingTemplate.class);
        int spid = -1;
        
        if (obj instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) obj;
            spid = spidAware.getSpid();
        }
        else if (chargingTemplate instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) chargingTemplate;
            spid = spidAware.getSpid();
        }
           
        if (spid!=-1)
        {
            final Predicate filterSpid = new EQ(BundleProfileXInfo.SPID, Integer.valueOf(spid));
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filterSpid);
            final Context subCtx = ctx.createSubContext();
            subCtx.put(BundleProfileHome.class, newHome);
            return subCtx;
        }
        return ctx;
    }

    protected Context filterSubscriberType(final Context ctx)
    {
        final Home originalHome = (Home) ctx.get(BundleProfileHome.class);
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object chargingTemplate = ctx.get(ChargingTemplate.class);
        BundleSegmentEnum subType = null;
        
        if (obj instanceof ChargingTemplate)
        {
            ChargingTemplate ct = (ChargingTemplate) obj;
            
            if (SubscriberTypeEnum.POSTPAID_INDEX  == ct.getSubscriberType())
            {
                subType = BundleSegmentEnum.POSTPAID;
            }
            else if (SubscriberTypeEnum.PREPAID_INDEX == ct.getSubscriberType())
            {
                subType = BundleSegmentEnum.PREPAID;
            }
        }
        else if (obj instanceof com.redknee.app.crm.bean.ui.ChargingTemplate)
        {
            com.redknee.app.crm.bean.ui.ChargingTemplate ct = (com.redknee.app.crm.bean.ui.ChargingTemplate) obj;
            
            if (SubscriberTypeEnum.POSTPAID_INDEX  == ct.getSubscriberType())
            {
                subType = BundleSegmentEnum.POSTPAID;
            }
            else if (SubscriberTypeEnum.PREPAID_INDEX == ct.getSubscriberType())
            {
                subType = BundleSegmentEnum.PREPAID;
            }
        }
           
        if (subType!=null)
        {
            final Predicate typePredicate = new EQ(BundleProfileXInfo.SEGMENT, subType);
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, typePredicate);
            final Context subCtx = ctx.createSubContext();
            subCtx.put(BundleProfileHome.class, newHome);
            return subCtx;
        }
        return ctx;
    }

}
