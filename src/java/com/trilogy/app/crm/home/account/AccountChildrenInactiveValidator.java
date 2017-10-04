package com.trilogy.app.crm.home.account;

import java.util.Collection;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.FindVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * If the account is being deactivate, ensure that its children
 * including subscribers and subaccounts are already inactive.
 *
 * @author candy
 */
public class AccountChildrenInactiveValidator implements Validator, Visitor
{
    public static final String BYPASS_DEACTIVE_ACCOUNT_SUBSCRIBER_STATE_VALIDATION =
        "bypass deactive account subscriber state validation";

   public void work(Context ctx, Object obj)
      throws IllegalStateException, AbortVisitException, AgentException
   {
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        Account newAccount = (Account) obj;
        Account oldAccount;
        FindVisitor findVisitor;
        try
        {
            oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
            if (oldAccount != null
                    && EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.INACTIVE) && !ctx.getBoolean(BYPASS_DEACTIVE_ACCOUNT_SUBSCRIBER_STATE_VALIDATION, false))
            {
                // an inactive state transition.
                // handle subscriber state validation differently if the
                // account is non-individual. Individual account is governed
                // by the corresponding subscriber state hence the subscriber
                // state is available from the input instead of the data store
                if (newAccount.isIndividual(ctx))
                {
                    And filter = new And();
                    filter.add(new EQ(SubscriberXInfo.BAN, newAccount.getBAN()));
                    filter.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
                    Collection<Subscriber> subs = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter, 1);
                    if (subs != null && subs.size() > 0)
                    {
                        el.thrown(new IllegalPropertyArgumentException("Account.state",
                                "Fail to deactivate account. Deactivate subscriber " + subs.iterator().next().getId()
                                        + " first"));
                        throw new AbortVisitException();
                    }
                }
                // Make sure the subaccounts and subscribers are inactive
                // Check the subaccounts only if current account is a responsible account
                // simar.singh@redknee.com - the comments above do not make sense
                // we should care about children of non-responsible parents :)
                // if we wouldn't then who would
                // else if (newAccount.getResponsible())
                else
                {
                    Home subAcctHome = newAccount.getImmediateChildrenAccountHome(ctx);
                    findVisitor = new FindVisitor();
                    
                    subAcctHome.where(ctx, new Not(new EQ(AccountXInfo.STATE, AccountStateEnum.INACTIVE))).forEach(
                            ctx, findVisitor);
                    if (findVisitor.getValue() != null)
                    {
                        el.thrown(new IllegalPropertyArgumentException("Account.state",
                                "Fail to deactivate account. Deactivate subaccount "
                                        + ((Account) findVisitor.getValue()).getBAN() + " first"));
                        throw new AbortVisitException();
                    }
                    else
                    {
                        subAcctHome.forEach(ctx, this);
                    }
                }
            }
        }
        catch (HomeException hEx)
        {
            new MinorLogMsg(this, "fail to validate inactive account for inactive subaccounts and subscribers", hEx)
                    .log(ctx);
        }
        finally
        {
            el.throwAll();
        }
    }

   public void visit(Context ctx, Object obj)
      throws AgentException, AbortVisitException
   {
      try
      {
         work(ctx, obj);
      }
      catch (AgentException aEx)
      {
         throw aEx;
      }
      catch (AbortVisitException avEx)
      {
         throw avEx;
      }
      catch (IllegalStateException isEx)
      {
         // eat it because it is already handled by the AbortVisitException
      }
   }

   @Override
   public void validate(Context ctx, Object obj)
      throws IllegalStateException
   {
      try
      {
         work(ctx, obj);
      }
      catch (IllegalStateException isEx)
      {
         throw isEx;
      }
      catch (AgentException aEx)
      {
         // eat it because it is already handled by the IllegalStateException
      }
      catch (AbortVisitException avEx)
      {
         // eat it because it is already handled by the IllegalStateException
      }
   }
}
