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
package com.trilogy.app.crm.home.account;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;


/**
 * Provides a Home decorator that provisions account information to Balance
 * Management (BM).
 * <p>
 * Upon creation, an attempt is made to create a profile in BM after delegating
 * to the remainder of the pipeline.
 * <p>
 * Upon update, if the state is not deactivate, an attempt is made to ensure
 * that the profile exists, creating one if necessary. If the state is in
 * transition from non-deactivated to deactivated, then an attempt is made to
 * remove the profile.
 * <p>
 * Upon removal, at attempt is made to ensure that the BM profile has been
 * removed.
 * <p>
 * Exceptions during BM provisioning encountered by this decorator are not
 * generally re-thrown, nor are new exceptions generated directly. Exceptional
 * cases are logged and passed to any exception listener in the context. As
 * such, this decorators should not normally cause a general pipeline failure.
 *
 * @author gary.anderson@redknee.com
 */
public class BalanceManagementAccountProvisioningHome
    extends HomeProxy
{
    /**
     * Creates a new BM provisioning decorator.
     *
     * @param context The operating context.
     * @param delegate The Home to which this proxy delegates.
     */
    public BalanceManagementAccountProvisioningHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context context, final Object obj)
        throws HomeException
    {
        Account account = (Account)obj;
        try
        {
            account = (Account)super.create(context, obj);
        }
        finally
        {
            // So long as a valid BAN is set, we will attempt to create the BM
            // profile.
            ensureAccountProfileExists(context, account);
        }

        return account;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context context, final Object obj)
        throws HomeException
    {
        final Account account = (Account)obj;

        if (account.getState() != AccountStateEnum.INACTIVE)
        {
            ensureAccountProfileExists(context, account);
            onAccountContactChange(context, account);
        }
        else
        {
            ensureAccountProfileRemoved(context, account);
        }
        return super.store(context, obj);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context context, final Object obj)
        throws HomeException
    {
        ensureAccountProfileRemoved(context, (Account)obj);
        super.remove(context, obj);
    }


    /**
     * Ensures that a profile exists in BM for the given account. If one is not
     * found, and attempt will be made to create it.
     *
     * @param context The operating context.
     * @param account The account for which to ensure a BM profile exists.
     */
    private void ensureAccountProfileExists(final Context context, final Account account)
    {
        if (account != null && account.getBAN() != null && account.getBAN().trim().length() > 0)
        {
            final SubscriberProfileProvisionClient client =
                BalanceManagementSupport.getSubscriberProfileProvisionClient(context);

            try
            {
                final Parameters profile = client.querySubscriberAccountProfile(context, account);
                if (profile == null)
                {
                    client.addSubscriberAccountProfile(context, account);
                }
            }
            catch (final SubscriberProfileProvisionException exception)
            {
                new MajorLogMsg(this, "Failed to communicate with BM to ensure profile exists for account "
                    + account.getBAN(), exception).log(context);

                notifyExceptionListeners(context, exception);
            }
            catch (final HomeException exception)
            {
                new MajorLogMsg(this, "Failed to ensure that BM profile exists for account " + account.getBAN(),
                    exception).log(context);

                notifyExceptionListeners(context, exception);
            }
        }
    }


    /**
     * Ensures that any existing profile is removed from BM for the given
     * account.
     *
     * @param context The operating context.
     * @param account The account for which to ensure there is no profile.
     */
    private void ensureAccountProfileRemoved(final Context context, final Account account)
    {
        final SubscriberProfileProvisionClient client =
            BalanceManagementSupport.getSubscriberProfileProvisionClient(context);

        try
        {
            final Parameters profile = client.querySubscriberAccountProfile(context, account);
            if (profile != null)
            {
                client.deleteSubscriberAccountProfile(context, account);
            }
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            new MajorLogMsg(this, "Failed to communicate with BM to ensure profile removed for account "
                + account.getBAN(), exception).log(context);

            notifyExceptionListeners(context, exception);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this, "Failed to ensure that BM profile removed for account " + account.getBAN(),
                exception).log(context);

            notifyExceptionListeners(context, exception);
        }
    }


    /**
     * Notifies any exception listener in the context that an exception has
     * occurred.
     *
     * @param context The operating context.
     * @param throwable The throwable to pass to the exception listener.
     */
    private void notifyExceptionListeners(final Context context, final Throwable throwable)
    {
        final ExceptionListener listener = (ExceptionListener)context.get(ExceptionListener.class);
        if (listener != null)
        {
            listener.thrown(throwable);
        }
    }
    /**
     * This method checks if Email Id is changed for Account, if yes then it will update it in CPS.
     * @param ctx
     * @param account
     * @throws HomeException
     */
    private void onAccountContactChange(Context ctx,Account account )throws HomeException
    {
		Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
		String newEmaild = account.getEmailID();
		if( newEmaild != null && !newEmaild.equals(oldAccount.getEmailID()))
		{
			 try
		        {
					if(MSP.getSpid(ctx) == null || MSP.getSpid(ctx).getId() != account.getSpid())
					{
						 MSP.setBeanSpid(ctx, account.getSpid());
					}
		            final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
		            client.updateEmailId(ctx, account, newEmaild);
		        }
		        catch (final SubscriberProfileProvisionException exception)
		        {
		            throw new HomeException("Unable to update the Account Mail Id. result = "
		                    + exception.getErrorCode(), exception);
		        }
		}
    }

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
}
