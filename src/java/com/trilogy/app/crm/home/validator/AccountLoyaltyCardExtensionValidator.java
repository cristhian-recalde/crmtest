package com.trilogy.app.crm.home.validator;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.extension.account.LoyaltyCardExtension;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;

/**
 * Prevents addition or removal of LoyaltyCardExtension
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public class AccountLoyaltyCardExtensionValidator implements Validator
{

    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException exception = new CompoundIllegalStateException();
        
        final Account newAccount = (Account) obj;
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        
        if (newAccount == null && oldAccount == null)
        {
            return;
        }
        LoyaltyCardExtension newExtension = (LoyaltyCardExtension) newAccount.getFirstAccountExtensionOfType(LoyaltyCardExtension.class);
        if (newExtension != null && oldAccount == null)
        {
            exception.thrown(new IllegalPropertyArgumentException(newAccount.getExtensionHolderProperty(), 
            "LoyaltyCardExtension cannot be added through Account creation. See Loyalty SOAP API."));
        }
        else if (oldAccount != null)
        {
            LoyaltyCardExtension oldExtension = (LoyaltyCardExtension) oldAccount.getFirstAccountExtensionOfType(LoyaltyCardExtension.class);
            if (newExtension == null && oldExtension != null && !AccountStateEnum.INACTIVE.equals(newAccount.getState()))
            {
                exception.thrown(new IllegalPropertyArgumentException(newAccount.getExtensionHolderProperty(), 
                "LoyaltyCardExtension cannot be removed through Account update. See Loyalty SOAP API."));
            }
            if (newExtension != null && oldExtension == null && !AccountStateEnum.INACTIVE.equals(newAccount.getState()))
            {
                exception.thrown(new IllegalPropertyArgumentException(newAccount.getExtensionHolderProperty(), 
                "LoyaltyCardExtension cannot be added through Account update. See Loyalty SOAP API."));
            }
        }   

        //Display as warning
        HTMLExceptionListener listner = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
        listner.thrown(exception);
    }
    
}
