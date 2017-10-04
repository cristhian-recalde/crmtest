package com.trilogy.app.crm.home.account;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

public class AccountParentValidator implements Validator
{
   public AccountParentValidator()
   {
   }

   public void validate(Context ctx, Object obj)
      throws IllegalStateException
   {
      CompoundIllegalStateException el = new CompoundIllegalStateException();
      Account account = (Account)obj;

      if (account.isRootAccount())
      {
          // Perform root account specific validation
          if (!account.isResponsible())
          {
              el.thrown(new IllegalPropertyArgumentException(AccountXInfo.RESPONSIBLE, "Root account must be responsible."));
          }
      }
      else
      {
          // Validate against parent account
          validateSpid(ctx, account, el);

          if (SubscriberTypeEnum.PREPAID.equals(account.getSystemType()))
          {
             validatePrepaid(ctx, account, el);
          }
          else if (SubscriberTypeEnum.POSTPAID.equals(account.getSystemType()))
          {
             validatePostpaid(ctx, account, el);
          }
          else if (SubscriberTypeEnum.HYBRID.equals(account.getSystemType()))
          {
             validateConverge(ctx, account, el);
          }
      }
      el.throwAll();
   }

   /**
    * ensure prepaid account is a child of a non-individual prepaid or converge account
    */
   protected void validatePrepaid(Context ctx, Account account, ExceptionListener el)
      throws IllegalStateException
   {
      try
      {
         Account parentAccount = (Account)getAccountHome(ctx).find(ctx, account.getParentBAN());
         if (parentAccount != null)
         {
            if (parentAccount.isIndividual(ctx))
            {
               el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "Cannot create new account under an individual parent account"));
            }
            if (SubscriberTypeEnum.POSTPAID.equals(parentAccount.getSystemType()))
            {
               el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "Cannot create a new prepaid account under a postpaid parent account"));
            }
         }
      }
      catch (HomeException hEx)
      {
         el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "fail to look up parent BAN "+account.getParentBAN()));
      }
   }

   /**
    * ensure postpaid account is a child of a non-individual postpaid or converge account
    */
   protected void validatePostpaid(Context ctx, Account account, ExceptionListener el)
      throws IllegalStateException
   {
      try
      {
         Account parentAccount = (Account)getAccountHome(ctx).find(ctx, account.getParentBAN());
         if (parentAccount != null)
         {
            if (parentAccount.isIndividual(ctx))
            {
               el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "Cannot create new account under an individual parent account"));
            }
            if (SubscriberTypeEnum.PREPAID.equals(parentAccount.getSystemType()))
            {
               el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "Cannot create a new postpaid account under a prepaid parent account"));
            }
         }
      }
      catch (HomeException hEx)
      {
         el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "fail to look up parent BAN "+account.getParentBAN()));
      }
   }

   /**
    * ensure converge account is a child of a non-individual converge account
    */
   protected void validateConverge(Context ctx, Account account, ExceptionListener el)
      throws IllegalStateException
   {
      try
      {
         Account parentAccount = (Account)getAccountHome(ctx).find(ctx, account.getParentBAN());
         if (parentAccount != null)
         {
            if (parentAccount.isIndividual(ctx))
            {
               el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "Cannot create new account under an individual parent account"));
            }
            if (!SubscriberTypeEnum.HYBRID.equals(parentAccount.getSystemType()))
            {
               el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "Cannot create a new hybrid account under a non-hybrid parent account"));
            }
         }
      }
      catch (HomeException hEx)
      {
         el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "fail to look up parent BAN "+account.getParentBAN()));
      }
   }

   /**
    * ensure child account belongs in the same service provider as parent account.
    */
   protected void validateSpid(Context ctx, Account account, ExceptionListener el)
      throws IllegalStateException
   {
      try
      {
         Account parentAccount = (Account)getAccountHome(ctx).find(ctx, account.getParentBAN());
         if (parentAccount != null)
         {
            if (parentAccount.getSpid() != account.getSpid())
            {
               el.thrown(new IllegalPropertyArgumentException(AccountXInfo.SPID, "Cannot create new account under a different service provider as the parent account of service provider ["+parentAccount.getSpid()+"]"));
            }
         }
      }
      catch (HomeException hEx)
      {
         el.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "fail to look up parent BAN "+account.getParentBAN()));
      }
   }

   protected Home getAccountHome(Context ctx)
   {
      return (Home)ctx.get(AccountHome.class);
   }
}
