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
package com.trilogy.app.crm.support;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.dunning.config.DunningConfig;
import com.trilogy.app.crm.dunning.config.DunningConfigXInfo;
import com.trilogy.app.crm.hlr.CrmHlrServiceImpl;
import com.trilogy.app.crm.priceplan.PricePlanVersionUpdateAgent;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This class reads and writes system global properties.
 *
 * @author joe.chen@redknee.com
 */
public class SystemSupport
{
    public static final String SYSTEM_AGENT = CoreCrmConstants.SYSTEM_AGENT;

    /**
     * Gets system flag for auto creating account when instantiating a subscriber.
     *
     * @param ctx the operating context
     * @return
     */
    public static boolean autoCreatesAccount(final Context ctx)
    {
        boolean auto = false;
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        if (cfg != null)
        {
            auto = cfg.isAutoCreateAccount();
        }
        return auto;
    }

    public static boolean isFullProvisionOnPricePlanVersionUpdate(Context ctx)
    {
        boolean needs = true;
        SysFeatureCfg cfg = (SysFeatureCfg)ctx.get(SysFeatureCfg.class);
        if (cfg != null)
        {
            needs = cfg.isFullProvisionOnPPVUpdate();
        }
        return needs;
    }

    public static boolean isFullProvisionOnPricePlanUpdate(Context ctx)
    {
        boolean needs = true;
        SysFeatureCfg cfg = (SysFeatureCfg)ctx.get(SysFeatureCfg.class);
        if (cfg != null)
        {
            needs = cfg.isFullProvisionOnPPUpdate();
        }
        return needs;
    }


    public static boolean isReProvisionAuxServiceOnPricePlanChange(Context ctx)
    {
        boolean needs = true;
        SysFeatureCfg cfg = (SysFeatureCfg)ctx.get(SysFeatureCfg.class);
        if (cfg != null)
        {
            needs = cfg.isReProvisionAuxServiceOnPricePlanChange();
        }
        return needs;
    }

    public static boolean needsHlrOnPricePlanVersionUpdate(Context ctx)
    {
        SysFeatureCfg cfg = (SysFeatureCfg)ctx.get(SysFeatureCfg.class);
        boolean needs = needsHlr(cfg);
        
        if (cfg !=null && needs)
        {
            needs = !cfg.isDisableHLROnPPVUpdate();
        }
        
        return needs;
    }

    private static boolean needsHlr(SysFeatureCfg cfg)
    {
         boolean needs = true;
         
         if (cfg != null)
         {
             needs = !cfg.isDisableHLR();
         }
         
         return needs;
    }

    public static boolean needsHlr(Context ctx)
    {
        SysFeatureCfg cfg = (SysFeatureCfg)ctx.get(SysFeatureCfg.class);
        boolean needs = needsHlr(cfg);
         
         if (needs && ctx.getBoolean(PricePlanVersionUpdateAgent.PRICE_PLAN_VERSION_UPDATE, false))
         {
             needs = needsHlrOnPricePlanVersionUpdate(ctx);
         }
         
         return needs && !ctx.has(CrmHlrServiceImpl.HLR_SKIPPED);
    }
    

    public static boolean supportsInCollection(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return cfg.isSupportsInCollection();
    }
    
    
    public static boolean supportsPrepaidCreationInActiveState(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return cfg.isCreatePrepaidInActiveState();
        
    }

    public static boolean supportsPrepaidPendingState(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return cfg.isAllowPrepaidToBeCreatedInPending();               
    }
    
    public static  boolean supportsUnExpirablePrepaidSubscription(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return cfg.isUnExpirablePrepaid();
        
    }

    public static  boolean supportsAllowWriteOffForPrepaidSubscription(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return cfg.isAllowWriteOffPrepaid();
        
    }

    public static boolean supportsConvertBillingType(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        boolean result = true;
        if (cfg != null)
        {
            result = cfg.getAllowBillingTypeConversion();
        }
        return result;
    }
    

    /**
     * TODO replace with license manager
     *
     * @param ctx the operating context
     * @return
     */
    public static boolean supportsAccountHierachy(final Context ctx)
    {
        boolean needs = false;
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        if (cfg != null)
        {
            needs = cfg.isEnbleAccountHierachy();
        }
        return needs;
    }

    public static int getAccountTopologyViewLimit(Context ctx)
    {
        SysFeatureCfg cfg = (SysFeatureCfg)ctx.get(SysFeatureCfg.class);
        if (cfg != null)
        {
            return cfg.getAccountTopologyViewLimit();
        }
        return 250;
    }

    public static int getDunningReportDaysInAdvance(Context ctx)
    {
        SysFeatureCfg cfg = (SysFeatureCfg)ctx.get(SysFeatureCfg.class);
        if (cfg != null)
        {
            return cfg.getDunningReportDaysInAdvance();
        }
        return 2;
    }

    public static boolean isDunningReportAutoAccept(Context ctx, int spid)
    {
        try
        {
            DunningConfig cfg = HomeSupportHelper.get(ctx).findBean(ctx, DunningConfig.class, new EQ(DunningConfigXInfo.SPID, Integer.valueOf(spid)));
            if (cfg != null)
            {
                return cfg.getDunningReportAutoAccept();
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, SystemSupport.class, "Unable to retrieve dunning config for SPID " + spid + ": " + e.getMessage());
        }
        return true;
    }

    public static int getDunningReportAgedDebtBreakdown(Context ctx, int spid)
    {
        try
        {
            DunningConfig cfg = HomeSupportHelper.get(ctx).findBean(ctx, DunningConfig.class, new EQ(DunningConfigXInfo.SPID, Integer.valueOf(spid)));
            if (cfg != null)
            {
                return cfg.getDunningReportAgedDebtBreakdown();
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, SystemSupport.class, "Unable to retrieve dunning config for SPID " + spid + ": " + e.getMessage());
        }
        return 3;
    }

    /**
     * Check if we will be using the ABM Activation ER453 instead of the the URS FCA ER909
     *
     * @param ctx
     * @return
     */
    public static boolean supportsABMActivationER(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return cfg.isEnableABMActivationERForFCA();
    }

    /**
     * Check if the system is configured to support hybrid prepaid
     *
     * @param ctx
     * @return
     */
    public static boolean supportsHybridPrepaid(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return !cfg.isAllowPrepaidToBeCreatedInPending() &&
                cfg.isAllowWriteOffPrepaid() &&
               !cfg.isAllowBillingTypeConversion() &&
               cfg.isCreatePrepaidInActiveState() &&
               cfg.isUnExpirablePrepaid();
    }

    /**
     * @param ctx the operating context
     * @param bean
     * @return
     */
    public static boolean supportsMultiLanguage(final Context ctx)
    {
        final LicenseMgr manager = (LicenseMgr) ctx.get(LicenseMgr.class);
        return manager.isLicensed(ctx, LicenseConstants.MULTI_LANGUAGE);
    }

    public static String getAgent(final Context ctx)
    {
        final User principal = (User) ctx.get(java.security.Principal.class, new User());
        return (principal.getId().trim().equals("") ? SYSTEM_AGENT : principal.getId());
    }

    public static boolean isAccountManagerDropDownEnabled(final Context ctx)
    {
        final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        boolean result = false;
        if (cfg != null)
        {
            result = cfg.getEnableAccountManagerDropdown();
        }
        return result;
    }
    
    public static boolean isPinManagerEnabled(final Context ctx)
    {
        final LicenseMgr manager = (LicenseMgr) ctx.get(LicenseMgr.class);
        return manager.isLicensed(ctx, LicenseConstants.PIN_MANAGER_LICENSE_KEY);
    }
    
    public static boolean isMultiSimEnabled(final Context ctx)
    {
    	final LicenseMgr manager = (LicenseMgr) ctx.get(LicenseMgr.class);
        return manager.isLicensed(ctx, LicenseConstants.MULTI_SIM_LICENSE);
    }
    
    public static boolean generateDummyMsisdnForMultisim(final Context ctx)
    {
    	final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        boolean result = false;
        if (cfg != null)
        {
            result = cfg.getGenerateDummyMsisdnForSlaveSim();
        }
        return result;
    }
}
