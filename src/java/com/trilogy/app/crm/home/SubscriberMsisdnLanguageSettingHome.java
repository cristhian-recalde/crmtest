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

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.SubscriberLanguageException;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MultiLanguageSupport;


/**
 * This home handles provisioning of the language a MSISDN associated with a Subscription (if Multi
 * Language is supported in the system)
 *
 * @author simar.singh@redknee.com
 */
public class SubscriberMsisdnLanguageSettingHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SubscriberMsisdnLanguageSettingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * Set language preferences of the MSISDN even before the Subscription has been
     * successfully created. This important because Post-paid Subscribers get Activated
     * right-away. Therefore, the language for the MSISDN should be already set before the
     * Subscriber.create() pipeline attempts to send a active state notification message
     * All errors are logged in ExceptionListner. Should the newSub creation fail, we need
     * not un-provivision the language creation because the language service will simply
     * override the language on next setting.
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	final Subscriber newSub = (Subscriber) super.create(ctx, obj);
        try
        {
            if (isApplicalble(ctx, newSub))
            {
                final String languageAsSetByAccessor = newSub.getLanguageAsSetByAccessor();
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Language Generation [" + newSub.getId() + "]", null).log(ctx);
                }
                if (null != languageAsSetByAccessor && !"".equals(languageAsSetByAccessor))
                {
                    // set language only if any preference has been made in this regard.
                    String loggingHeader = "[SubscriberMsisdnLanguageSettingHome::create (ban=" + newSub.getBAN()
                            + ", msisdn=" + newSub.getMSISDN() + ", msisdn=" + newSub.getId() + ")] ";
                    try
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, loggingHeader + " Attempt to set language of msisdn "
                                    + newSub.getMSISDN() + " to " + languageAsSetByAccessor, null).log(ctx);
                        }
                        MultiLanguageSupport.setSubscriberLanguage(ctx, newSub.getSpid(), newSub.getMSISDN(),
                                languageAsSetByAccessor);
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, loggingHeader + " Successfully set language preferences!", null)
                                    .log(ctx);
                        }
                    }
                    catch (ProvisioningHomeException e)
                    {
						final String errorMsg =
						    SubscriberLanguageException.getVerboseResult(ctx,
						        e.getResultCode());
                        new MinorLogMsg(this, "Error encountered setting language preferences for msisdn ["
                                + newSub.getMSISDN() + "] on Language Service. Error [" + errorMsg + "]", e).log(ctx);
                        handleError(ctx, errorMsg, e);
                    }
                }
                else
                {
                    new InfoLogMsg(this, "Skipped Language Generation [" + newSub.getId() + "]. Language as set  ["
                            + languageAsSetByAccessor + "]", null).log(ctx);
                }
            }
        }
        catch (Throwable t)
        {
            final String errorMessonge = "Languge Setting failed for Subscriber [" + newSub.getId()
                    + "] due to unknown error [" + t.getMessage() + "]";
            new MinorLogMsg(this, errorMessonge, t).log(ctx);
            handleError(ctx, errorMessonge, t);
        }
        return newSub;
    }


    /**
     * Update language preferences of the Subscription MSISDN (if changed)
     * Update Subscription after the pipeline below has completed
     * Do not fail the home-operation due to errors here
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final Subscriber newSub = (Subscriber) obj ;
        try
        {
            if (isApplicalble(ctx, newSub))
            {
                new DebugLogMsg(this, "Skipping Language Generation [" + newSub.getId() + "]", null).log(ctx);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "[MsisdnOwnershipLanguageSettingHome::store (ban=" + newSub.getBAN()
                            + ", originalMsisdn=" + oldSub.getMSISDN() + ", newMsisdn=" + newSub.getMSISDN() + ")] ",
                            null).log(ctx);
                }
                // This case is for a language change without MSISDN change
                // Language changes are accessed relative to what was fetched last time
                // Blind write regardless of what is there at present in the language
                // model
                final String languageAsSetbyAccessor = newSub.getLanguageAsSetByAccessor();
                if (null != languageAsSetbyAccessor)
                {
                    final String languageAsLastRetreived = newSub.getLanguageAsLastRetrieved(ctx);
                    if (null == languageAsLastRetreived || !languageAsSetbyAccessor.equals(languageAsLastRetreived))
                    {
                        onLangChange(ctx, newSub);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Error setting language for Subscriber[" + newSub.getId() + "] with MSISDN["
                    + newSub.getMSISDN() + "]", t).log(ctx);
        }
        return super.store(ctx, obj);
    }

    // no need to implement remove() - Language is a part of CPS - Subscription Profile



    /**
     * On a Language change for Subscription, Set the new language for the Subscription's
     * MSISDN Change of MSISDN by MsisdnOwnereship (GUI: MSISDN Management) will perform a
     * Subscriber.store for all Subscriptions associated with the ORIGINAL MSISDN. So this
     * code will also be run in that case.
     *
     * @param ctx
     * @param oldSub
     * @param newSub
     * @throws HomeException
     */
    public void onLangChange(final Context ctx, final Subscriber newSub) throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, " Language changed; Attempting to update language preferences... ", null).log(ctx);
        }
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Attempt to set language of msisdn " + newSub.getMSISDN() + " from "
                        + newSub.getLanguageAsLastRetrieved(ctx) + " to " + newSub.getLanguageAsSetByAccessor(), null)
                        .log(ctx);
            }
            MultiLanguageSupport.setSubscriberLanguage(ctx, newSub.getSpid(), newSub.getMSISDN(),
                    newSub.getLanguageAsSetByAccessor());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, " Successfully updated language preferences!", null).log(ctx);
            }
        }
        catch (ProvisioningHomeException e)
        {
			final String errorMsg =
			    SubscriberLanguageException.getVerboseResult(ctx,
			        e.getResultCode());
            new MinorLogMsg(this, "Error encountered attempting to update language preferences: " + errorMsg, e)
                    .log(ctx);
            handleError(ctx, errorMsg, e);
        }
    }


    private void handleError(final Context ctx, final String errorMsg, final Throwable t)
    {
        final Exception exception = new IllegalPropertyArgumentException(SubscriberXInfo.BILLING_LANGUAGE, errorMsg);
        final ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (el != null)
        {
            el.thrown(exception);
        }
        //FrameworkSupport.notifyExceptionListener(ctx, exception);
        new MinorLogMsg(this, errorMsg, null);
        new DebugLogMsg(this, errorMsg, t);
    }

    private  boolean isApplicalble(final Context ctx, final Subscriber sub)
    {
        return !(sub.isPooledGroupLeader(ctx));
    }
}
