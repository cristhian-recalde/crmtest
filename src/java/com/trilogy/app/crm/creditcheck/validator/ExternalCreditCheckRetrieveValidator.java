package com.trilogy.app.crm.creditcheck.validator;

import com.trilogy.app.crm.bean.ExternalCreditCheck;
import com.trilogy.app.crm.creditcheck.ExternalCreditCheckSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

public class ExternalCreditCheckRetrieveValidator implements Validator {

	@Override
	public void validate(Context ctx, Object paramObject) throws IllegalStateException {
		ExternalCreditCheck externalCreditCheck = (ExternalCreditCheck) paramObject;
		CompoundIllegalStateException el = new CompoundIllegalStateException();
		try
		{
			ExternalCreditCheckSupport.validateSpid(ctx, externalCreditCheck);
			ExternalCreditCheckSupport.validateBan(ctx, externalCreditCheck);
		}
		catch (HomeException e)
		{
			el.thrown(e);
		}
		finally
		{
			el.throwAll();
		}
	}

}
