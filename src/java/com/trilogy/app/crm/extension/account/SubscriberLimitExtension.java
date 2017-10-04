package com.trilogy.app.crm.extension.account;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.extension.account.AbstractSubscriberLimitExtension;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtensionXInfo;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.support.SubscriberLimitSupport;


/**
 * This account extension limits the number of immediate non-inactive subscribers an
 * account may have.
 *
 * @author aaron.gourley@redknee.com
 * @author cindy.wong@redknee.com
 */
public class SubscriberLimitExtension extends AbstractSubscriberLimitExtension
{

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary(final Context ctx)
    {
        return SubscriberLimitExtensionXInfo.MAX_SUBSCRIBERS.getLabel() + "=" + this.getMaxSubscribers();
    }


    public void install(final Context ctx) throws ExtensionInstallationException
    {
        final Account account = getAccount(ctx);
        if (account == null)
        {
            throw new ExtensionInstallationException("Unable to install " + this.getName(ctx)
                + " extension.  No account found with BAN=" + this.getBAN(), false);
        }
        validateSubscriberLimit(ctx, account, "Unable to install " + this.getName(ctx)
            + " extension.  The account has already exceeded the new subscriber limit.", null);
        LogSupport.debug(ctx, this, this.getName(ctx) + " installed on account " + account.getBAN()
            + " successfully.");
    }


    public void update(final Context ctx) throws ExtensionInstallationException
    {
        final Account account = getAccount(ctx);
        if (account == null)
        {
            throw new ExtensionInstallationException("Unable to update " + this.getName(ctx)
                + " extension.  No account found with BAN=" + this.getBAN(), false);
        }
        validateSubscriberLimit(ctx, account, "Unable to update " + this.getName(ctx)
            + " extension.  The account has already exceeded the new subscriber limit.", null);

        LogSupport.debug(ctx, this, this.getName(ctx) + " updated on account " + account.getBAN()
            + " successfully.");
    }


    public void uninstall(final Context ctx) throws ExtensionInstallationException
    {
        final Account account = getAccount(ctx);
        if (account == null)
        {
            throw new ExtensionInstallationException("Unable to uninstall " + this.getName(ctx)
                + " extension.  No account found with BAN=" + this.getBAN(), false);
        }
        LogSupport.debug(ctx, this, this.getName(ctx) + " uninstalled from account " + account.getBAN()
            + " successfully.");
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx) throws IllegalStateException
    {
        final CompoundIllegalStateException ex = new CompoundIllegalStateException();

        if (this.getMaxSubscribers() < 1)
        {
            ex.thrown(new IllegalPropertyArgumentException(SubscriberLimitExtensionXInfo.MAX_SUBSCRIBERS,
                "Max subscriber limit must be greater than or equal to 1."));
        }
        else
        {
            final Account account = this.getAccount(ctx);
            if (account != null)
            {
                final String msg = "Max subscriber limit less than current subscriber count for account "
                    + account.getBAN() + ".";
                try
                {
                    validateSubscriberLimit(ctx, account, msg, ex);
                }
                catch (final ExtensionInstallationException exception)
                {
                    ex.thrown(exception);
                }
            }
            else
            {
                LogSupport.minor(ctx, this, "Cannot find current account, skipping subscriber limit validation.");
            }
        }

        ex.throwAll();
    }


    /**
     * Validates the subscriber limit of an account.
     *
     * @param context
     *            The operating context.
     * @param account
     *            The account being validated.
     * @param errorMessage
     *            Error message to use when the limit has exceeded.
     * @param ex
     *            Exception listener. Use <code>null</code> if none is needed, in which
     *            case, {@link ExtensionInstallationException} will be thrown instead.
     * @throws ExtensionInstallationException
     *             Exception thrown if there are errors in the validation, and no
     *             exception listener is set.
     */
    private void validateSubscriberLimit(final Context context, final Account account, final String errorMessage,
        final CompoundIllegalStateException ex) throws ExtensionInstallationException
    {
        try
        {
            final int totalSubs = SubscriberLimitSupport.getNumberOfSubscribersInAccount(context, account.getBAN());
            if (totalSubs > this.getMaxSubscribers())
            {
                if (ex != null)
                {
                    ex.thrown(new IllegalPropertyArgumentException(SubscriberLimitExtensionXInfo.MAX_SUBSCRIBERS,
                        errorMessage + "  Current subscriber count is " + totalSubs));
                }
                else
                {
                    throw new ExtensionInstallationException(errorMessage, false);
                }
            }
        }
        catch (final Exception e)
        {
            final String message = "Error occurred validating subscriber limit for account " + account.getBAN();
            LogSupport.minor(context, this, message, e);
            if (ex != null)
            {
                ex.thrown(new IllegalPropertyArgumentException(SubscriberLimitExtensionXInfo.MAX_SUBSCRIBERS, message
                    + ": " + e.getMessage()));
            }
            else
            {
                throw new ExtensionInstallationException(message + ": " + e.getMessage(), false);
            }
        }
    }
}
