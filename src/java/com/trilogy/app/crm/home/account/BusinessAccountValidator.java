package com.trilogy.app.crm.home.account;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

public class BusinessAccountValidator implements Validator
{
   public BusinessAccountValidator()
   {
   }

   /**
    * verify contactName field is not empty if account type is Business or Digiline
    */
public void validate(Context ctx, Object obj)
      throws IllegalStateException
   {
      CompoundIllegalStateException el = new CompoundIllegalStateException();
      Account account = (Account)obj;

      if (LogSupport.isDebugEnabled(ctx))
      {
         new DebugLogMsg(this, "account type is "+account.getAccountCategory(ctx)+ " isBuiness? "+account.isBusiness(ctx), null).log(ctx);
      }
      
      // Business fields not mandatory for Prepaid Accounts
      if (account.getSystemType() != SubscriberTypeEnum.PREPAID
              && account.isBusiness(ctx))
      {
         if ((account.getCompanyTel() == null)
                || (account.getCompanyTel().trim().length() < 7))
         {
            el.thrown(new IllegalPropertyArgumentException(
               AccountXInfo.COMPANY_TEL,
               "Number must have at least 7 digits."));
         }

         if ((account.getCompanyFax() != null)
                && (account.getCompanyFax().trim().length() > 0)
                && (account.getCompanyFax().trim().length() < 7))
         {
            el.thrown(new IllegalPropertyArgumentException(
               AccountXInfo.COMPANY_FAX,
               "Number must have at least 7 digits if set."));
         }
      }
      el.throwAll();
   }
}
