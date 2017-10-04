package com.trilogy.app.crm.extension.account;

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCreationTemplateXInfo;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;


public class GroupPricePlanExtension extends AbstractGroupPricePlanExtension
{
    public final static String SYSTEM_TYPE           = "SubscriberTypeEnum.class";

    /**
     * @{inheritDoc}
     */
    @Override
    public String getSummary(final Context ctx)
    {
        String prepaidLabel = null;
        if( this.getPrepaidPricePlanID() != DEFAULT_PREPAIDPRICEPLANID )
        {
            prepaidLabel = GroupPricePlanExtensionXInfo.PREPAID_PRICE_PLAN_ID.getLabel() + "=" + this.getPrepaidPricePlanID();
        }

        String postpaidLabel = null;
        if( this.getPostpaidPricePlanID() != DEFAULT_POSTPAIDPRICEPLANID )
        {
            postpaidLabel = GroupPricePlanExtensionXInfo.POSTPAID_PRICE_PLAN_ID.getLabel() + "=" + this.getPostpaidPricePlanID();
        }

        final StringBuilder desc = new StringBuilder();
        if( prepaidLabel != null )
        {
            desc.append(prepaidLabel);
            if( postpaidLabel != null )
            {
                desc.append(", ");
            }
        }
        if( postpaidLabel != null )
        {
            desc.append(postpaidLabel);
        }
        return "";
    }


    @Override
    public void validate(final Context ctx) throws IllegalStateException
    {
        final CompoundIllegalStateException exception = new CompoundIllegalStateException();

        final ExtensionAware parentBean = getParentBean(ctx);
        final PropertyInfo systemTypeProperty;
		final GroupTypeEnum groupType;
        if( parentBean instanceof Account )
        {
            systemTypeProperty = AccountXInfo.SYSTEM_TYPE;
			groupType = ((Account) parentBean).getGroupType();
        }
        else if( parentBean instanceof AccountCreationTemplate )
        {
            systemTypeProperty = AccountCreationTemplateXInfo.SYSTEM_TYPE;
			groupType = ((AccountCreationTemplate) parentBean).getGroupType();
        }
        else
        {
            systemTypeProperty = null;
			groupType = null;
        }

        if( systemTypeProperty != null )
        {
            final SubscriberTypeEnum systemType = (SubscriberTypeEnum)systemTypeProperty.get(parentBean);
            if ((SubscriberTypeEnum.HYBRID.equals(systemType) || SubscriberTypeEnum.POSTPAID.equals(systemType)) &&
                    this.getPostpaidPricePlanID()<0)
            {
                exception.thrown(new IllegalPropertyArgumentException(GroupPricePlanExtensionXInfo.POSTPAID_PRICE_PLAN_ID,
                        "Postpaid price plan required."));
            }

            if ((SubscriberTypeEnum.HYBRID.equals(systemType) || SubscriberTypeEnum.PREPAID.equals(systemType)) &&
                    this.getPrepaidPricePlanID()<0)
            {
                exception.thrown(new IllegalPropertyArgumentException(GroupPricePlanExtensionXInfo.PREPAID_PRICE_PLAN_ID,
                        "Prepaid price plan required."));
            }
        }

		if (groupType != null && groupType.equals(GroupTypeEnum.SUBSCRIBER))
        {
            exception.thrown(new IllegalPropertyArgumentException(parentBean.getExtensionHolderProperty(), this.getName(ctx) + " extension not allowed for individual accounts."));
        }

        exception.throwAll();
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void install(final Context ctx) throws ExtensionInstallationException
    {
        // If there are subscribers in the account:
        // And the account is pooled:
        //   1. Apply the group price plan to the lead subscriber.
        //   2. Set member subscriber price plans to the leader's price plan.  Don't provision the leader's group bundles for the member.
        //   3. If the group or member bundles are of the same category, the bundle units are transferred.
        // And the account is not pooled:
        //   1. Set all subscribers price plans to the GPP extension's price plan.
        //   2. If the group or member bundles are of the same category, the bundle units are transferred.
        update(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void update(final Context ctx) throws ExtensionInstallationException
    {
        // If there are subscribers in the account:
        // And the account is pooled:
        //   1. Apply the group price plan to the lead subscriber.
        //   2. Set member subscriber price plans to the leader's price plan.  Don't provision the leader's group bundles for the member.
        //   3. If the group or member bundles are of the same category, the bundle units are transferred.
        // And the account is not pooled:
        //   1. Set all subscribers price plans to the GPP extension's price plan.
        //   2. If the group or member bundles are of the same category, the bundle units are transferred.
        final Account account = getAccount(ctx);
        if( account != null )
        {
            Collection coll = null;
            try
            {
                coll = AccountSupport.getAllSubscribers(ctx, account);
            }
            catch (final HomeException e)
            {
                new DebugLogMsg(this, e.getClass().getSimpleName() + " occurred in " + GroupPricePlanExtension.class.getSimpleName() + ".install(): " + e.getMessage(), e).log(ctx);
                throw new ExtensionInstallationException("Error retrieving subscribers for account " + account.getBAN(), false);
            }

            if( coll != null && coll.size() > 1 )
            {
                // If there are subscribers in the account:
                if( account.isPooled(ctx) )
                {
                    setAllPricePlans(ctx, coll, account);
                }
            }
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void uninstall(final Context ctx) throws ExtensionInstallationException
    {
        // Everyone's price plans remain untouched.  Only the extension itself is removed from the account.
        // NOP
    }

    public long getGroupPricePlan(final Subscriber sub)
    {
        return getGroupPricePlan(sub.getSubscriberType());
    }

    public long getGroupPricePlan(final SubscriberTypeEnum type)
    {
        if( SubscriberTypeEnum.PREPAID.equals(type) )
        {
            return getPrepaidPricePlanID();
        }
        return getPostpaidPricePlanID();
    }


    private void setAllPricePlans(final Context ctx, final Collection<Subscriber> subs, final Account account) throws ExtensionInstallationException
    {
        int count = 0;
        for( final Subscriber sub : subs )
        {
            if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
            {
                count++;
            }
        }
        if (count > 2)
        {
            Context appCtx = (Context) ctx.get("app");
            appCtx.put("groupPricePlanChange", true);
        }
        Context subCtx = ctx.createSubContext();
        for( final Subscriber sub : subs )
        {
            try
            {
                final long pricePlanID = getGroupPricePlan(sub);
                if( sub.getPricePlan() != pricePlanID
                        && isValidState(sub) && !sub.isPooledGroupLeader(subCtx) 
                               && sub.getState() != SubscriberStateEnum.INACTIVE)
                {
                    
                    sub.switchPricePlan(subCtx, pricePlanID);
                    final Home subHome = (Home)subCtx.get(SubscriberHome.class);
                    subHome.store(subCtx, sub);
                }
            }
            catch (final HomeException e)
            {
                    new DebugLogMsg(this, e.getClass().getSimpleName() + " occurred in " + GroupPricePlanExtension.class.getSimpleName() + ".install(): " + e.getMessage(), e).log(ctx);
                    throw new ExtensionInstallationException("Error setting price plan of group member subscribers for account " + sub.getBAN(), false);
            }
        }
    }

    private boolean isValidState(final Subscriber sub)
    {
        return !sub.isInFinalState();
    }
}
