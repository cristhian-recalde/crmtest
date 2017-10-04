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
package com.trilogy.app.crm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Provides support for processing collections of subscribers.  This class
 * operates by letting the caller add subscribers to an internal collection
 * through various means: by subscriber identifier, by MSISDN, and by account
 * identifier.  All of these subscriberes are then processed by the process()
 * method.
 *
 * @author gary.anderson@redknee.com
 */
public
class SubscriberCollectionProcessor
    implements ContextAware
{
    /**
     * Creates a new SubscriberCollectionProcessor.
     *
     * @param context The operating context.
     */
    public SubscriberCollectionProcessor(final Context context)
    {
        contextSupport_.setContext(context);
        subscriberIdentifier_ = new HashSet();
        exceptionListener_ = null;
    }


    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return contextSupport_.getContext();
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(final Context context)
    {
        contextSupport_.setContext(context);
    }


    /**
     * Addes all of the subscribers which belong to the given accounts to the
     * list of subscribers to be processed.
     *
     * @param identifiers A collection of account identifiers (String).
     */
    public void addAccounts(final Collection identifiers)
    {
        final Home home = (Home)getContext().get(AccountHome.class);

        CollectionSupportHelper.get(getContext()).forEach(getContext(),
            identifiers,
            new Predicate()
            {
                public boolean f(Context ctx,final Object object)
                {
                    final String accountIdentifier = (String)object;

                    try
                    {
                        final Account account =
                            (Account)home.find(ctx,accountIdentifier);

                        if (account == null)
                        {
                            throw new HomeException(
                                "Account " + accountIdentifier + " does not exist.");
                        }

                       /* Commenting the code as it doen't work writing own method to return subscriber 
                        * final Collection subscriberIdentifiers =
                            AccountSupport.getSubscriberIdentifiers(getContext(), account);
                        */
                        final Collection subscriberIdentifiers = getSubscriberIdentifiers(getContext(), account);
                        addIdentifiers(subscriberIdentifiers);
                    }
                    catch (final HomeException exception)
                    {
                        final IllegalStateException newException =
                            new IllegalStateException(
                                "Failed to look up subscribers for account "
                                + accountIdentifier);
                        newException.initCause(exception);
                        thrown(newException);
                    }

                    return true;
                }
            });
    }

    /**
     * @param ctx 
     * @param account
     * @return subscriber identifiers collection 
     */
    private Collection getSubscriberIdentifiers(Context ctx, Account account)
    {
    	Collection subscriberIdentifiers = null;
    	Collection subscribers = null;
    	final Home subHome = (Home) ctx.get(SubscriberHome.class);
    	if (subHome == null )
    	{
    		 new MajorLogMsg(
                     this,
                     "SubscriberHome not found in context.",
                     null).log(ctx);
    	}
    	try 
    	{
    		subscribers = subHome.select(ctx,new EQ(SubscriberXInfo.BAN,account.getBAN()));
    		if ( subscribers != null )
    		{
    			subscriberIdentifiers = new ArrayList(subscribers.size());
    			for ( Iterator i = subscribers.iterator() ; i.hasNext() ; )
    			{
    				Subscriber sub = (Subscriber) i.next();
    				subscriberIdentifiers.add(sub.getId());
    			}
    		}
		} 
    	catch (HomeException e)
		{
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,"Fail to retrieve subscriber "+e.getMessage(),e).log(ctx);
            }
		}
    	// avoiding Null Pointer Exception
    	if ( subscriberIdentifiers == null)
    	{
    		subscriberIdentifiers = new ArrayList(0);
    	}
    	return subscriberIdentifiers;
    }

    /**
     * Adds all of the subscribers which own the given set of subscriber
     * identifiers to the list of subscribers to be processed.
     *
     * @param identifiers A collection of subscriber identifiers (String).
     */
    public void addIdentifiers(final Collection identifiers)
    {
        subscriberIdentifier_.addAll(identifiers);
    }


    /**
     * Adds all of the subscribers which own the given set of MSISDNs to the
     * list of subscribers to be processed.
     *
     * @param msisdns A collection of MSISDNs (String).
     */
    public void addMSISDNs(final Collection msisdns)
    {
        final Home msisdnHome = (Home)getContext().get(MsisdnHome.class);

        final Function convertMsisdnToSubscriberIdentifier =
            new Function()
            {
                public Object f(Context ctx,final Object object)
                {
                    final String msisdn = (String)object;

                    Msisdn msisdnObject;
                    try
                    {
                        msisdnObject = (Msisdn)msisdnHome.find(ctx,msisdn);
                    }
                    catch (final HomeException exception)
                    {
                        thrown(exception);
                        return BAD_IDENTIFIER;
                    }

                    if (msisdnObject == null)
                    {
                        thrown(
                            new IllegalStateException(
                                "Failed to locate MSISDN for number " + msisdn));

                        return BAD_IDENTIFIER;
                    }

                    if (msisdnObject.getSubscriberID(ctx) == null
                        || msisdnObject.getState() != MsisdnStateEnum.IN_USE)
                    {
                        thrown(
                            new IllegalStateException(
                                "MSISDN " + msisdn + " is not in use by a subscriber."));

                        return BAD_IDENTIFIER;
                    }

                    return msisdnObject.getSubscriberID(ctx);
                }
            };

        final Collection identifiers = new HashSet();

        CollectionSupportHelper.get(getContext()).process(getContext(),
            msisdns,
            convertMsisdnToSubscriberIdentifier,
            identifiers);

        identifiers.remove(BAD_IDENTIFIER);

        addIdentifiers(identifiers);
    }


    /**
     * Adds all of the subscribers which belong to the given set of SPIDs to the
     * list of subscribers to be processed.
     *
     * @param identifiers A collection of SPIDs (Integer).
     */
    public void addSPIDs(final Collection identifiers)
    {
        final Home home = (Home)getContext().get(CRMSpidHome.class);

        CollectionSupportHelper.get(getContext()).forEach(getContext(),
            identifiers,
            new Predicate()
            {
                public boolean f(Context ctx,final Object object)
                {
                    final Integer spidIdentifier = (Integer)object;

                    try
                    {
                        final CRMSpid spid =
                            (CRMSpid)home.find(ctx,spidIdentifier);

                        if (spid == null)
                        {
                            throw new HomeException(
                                "Spid " + spidIdentifier + " does not exist.");
                        }

                        final Collection subscriberIdentifiers =
                            SpidSupport.getSubscriberIdentifiers(getContext(), spid);

                        addIdentifiers(subscriberIdentifiers);
                    }
                    catch (final HomeException exception)
                    {
                        final IllegalStateException newException =
                            new IllegalStateException(
                                "Failed to look up subscribers for SPID "
                                + spidIdentifier);
                        newException.initCause(exception);
                        thrown(newException);
                    }

                    return true;
                }
            });
    }


    /**
     * Processes all of the subscribers added to this collection processor.
     * Inability to look up subscribers does not stop processing.  If a
     * HomeException is encounterred while looking up the subscriber, then the
     * given listener is informed and processing continues on to the next
     * subscriber.
     *
     * @param processor The processor for processing the subscriber.
     *
     * @exception SubscriberProcessingInterruptionException Thrown if a problem
     * occured during processing that should gracefully halt processing of
     * further subscribers.
     */
    public void process(final SubscriberProcessor processor)
        throws SubscriberProcessingInterruptionException
    {
        final Home home = (Home)getContext().get(SubscriberHome.class);

        final SubscriberProcessorToPredicateAdapter adapter =
            new SubscriberProcessorToPredicateAdapter(getContext(), processor);

        CollectionSupportHelper.get(getContext()).forEach(getContext(),
            subscriberIdentifier_,
            new Predicate()
            {
                public boolean f(Context ctx,final Object object)
                {
                    final String subscriberIdentifier = (String)object;

                    final Subscriber subscriber;
                    try
                    {
                        subscriber = (Subscriber)home.find(ctx,subscriberIdentifier);
                        if (subscriber == null)
                        {
                            throw new HomeException("Failed to lookup subscriber " + subscriberIdentifier);
                        }
                    }
                    catch (final HomeException exception)
                    {
                        thrown(exception);
                        return true;
                    }

                    return adapter.f(ctx,subscriber);
                 }
            });

        if (adapter.isInterrupted())
        {
            throw adapter.getInterruption();
        }
    }


    /**
     * Sets the ExceptionListener to which ignorable exceptions are passed.
     *
     * @param listener The ExceptionListener to which ignorable exceptions are
     * passed.
     */
    public void setExceptionListener(final ExceptionListener listener)
    {
        exceptionListener_ = listener;
    }


    /**
     * Passes ignorable exceptions to the registered listener, if one is set.
     *
     * @param throwable The ignorable exceptions.
     */
    void thrown(final Throwable throwable)
    {
        if (exceptionListener_ != null)
        {
            exceptionListener_.thrown(throwable);
        }
    }


    /**
     * Used to indicate a bad identifier in a collection.
     */
    private static final String BAD_IDENTIFIER = "";

    /**
     * Provides ContextAware support for this class.  ContextAwareSupport is
     * abstract, so we must create a derivation of it.
     */
    private final ContextAware contextSupport_ = new ContextAwareSupport() { };

    /**
     * Represents the collection of subscribers to process in the call to process().
     */
    private final Set subscriberIdentifier_;

    /**
     * The ExceptionListener to which ignorable exceptions are passed.
     */
    private ExceptionListener exceptionListener_;

} // class
