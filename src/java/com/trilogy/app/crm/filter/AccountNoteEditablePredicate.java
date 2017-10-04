package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xlog.log.LogSupport;

public class AccountNoteEditablePredicate implements Predicate {

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean f(Context ctx, Object obj)
	{
		LogSupport.debug(ctx, this, "[CheckAccountNoteLicencePredicate.f] checking if Account Note Licence is true");
		
		final boolean isLicensed = LicensingSupportHelper.get(ctx).isLicensed(ctx,
				CoreCrmLicenseConstants.ACCOUNT_NOTE_EDITABLE);
		
		return isLicensed;
		
	}
}
