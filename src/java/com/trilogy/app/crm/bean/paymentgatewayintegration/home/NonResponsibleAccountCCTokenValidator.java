package com.trilogy.app.crm.bean.paymentgatewayintegration.home;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.CreditCardTokenHome;
import com.trilogy.app.crm.bean.CreditCardTokenXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;

/**
 * 
 * When converting from a responsible account to a non-responsible account,
 * validate whether a cc token profile exists for this account. If TRUE,
 * 
 * throw exception.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class NonResponsibleAccountCCTokenValidator implements Validator {

	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException {
		
		Account account = (Account) obj;
		
		if(!account.isResponsible())
		{
			try
			{
				CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.BAN, account.getBAN()));
				
				if(token != null)
				{
					throw new IllegalStateException("Cannot make this Account Non-Responsible as a Credit Card Token[" + token.getId() + "/" + token.getMaskedCreditCardNumber()
							+ "] is associated with it. Please delete the token profile and proceed.");
				}
			}
			catch (Exception e) 
			{
				throw new IllegalStateException(e);
			}
		}

	}

}
