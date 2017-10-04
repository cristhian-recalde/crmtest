package com.trilogy.app.crm.home.account;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;

/**
 * @deprecated this validations are not used any more
 */
@Deprecated
public class AccountOwnerMsisdnValidator implements Validator
{
   /**
    * make sure the owner MSISDN (if provided) for a pooled account is valid
    */
   @Override
public void validate(Context ctx, Object obj)
      throws IllegalStateException
   {
      CompoundIllegalStateException el = new CompoundIllegalStateException();
      Account account = (Account)obj;

		if (account.isPooled(ctx))
      {
         String ownerMSISDN = account.getOwnerMSISDN();
         if (ownerMSISDN != null && ownerMSISDN.trim().length() != 0)
         {
            // Lookup the corresponding subscriber.
            Subscriber subscriber = null;
            try
            {
               subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, ownerMSISDN);
            }
            catch (HomeException e)
            {
               // Empty
            }

            // Determine if the corresponding subscriber is valid and in valid state.
            String msg = null;
            if (subscriber == null)
            {
               msg = "Failed to look up the subscriber for MSISDN \"" + ownerMSISDN + "\".";
            }
            else if (!subscriber.getBAN().equals(account.getBAN()))
            {
               msg = "Subscriber does not belong to account \"" + account.getBAN() + "\".";
            }
            else if (!SubscriberStateEnum.ACTIVE.equals(subscriber.getState()))
            {
               msg = "Subscriber must be ACTIVE";
            }

            // Throw the error message (if it is set).
            if (msg != null)
            {
               final MessageMgr msgMgr = new MessageMgr(ctx, this);

               el.thrown(
                     new IllegalPropertyArgumentException(
                        msgMgr.get("Account.ownerMSISDN.Label", AccountXInfo.OWNER_MSISDN.getLabel(ctx)),
                        msg));
            }
         }
      }
      el.throwAll();
   }
}
