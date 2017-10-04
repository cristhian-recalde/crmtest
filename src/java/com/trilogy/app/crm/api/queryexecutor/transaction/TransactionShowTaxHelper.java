package com.trilogy.app.crm.api.queryexecutor.transaction;

import java.util.List;

import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxComponent;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.invoice.util.MathSupportUtil;
import com.trilogy.app.crm.support.TaxAuthoritySupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

public class TransactionShowTaxHelper {
    public final static String MODULE = TransactionShowTaxHelper.class.getName();
    
    public static long calculateTaxForTransaction (Context ctx, Transaction trans) 
    {
		double taxPer = 0.0;
		long amount = trans.getAmount();
		TaxAuthority taxAuth = TaxAuthoritySupportHelper.get(ctx).getTaxAuthorityById(ctx, trans.getTaxAuthority(), trans.getSpid());
		long taxAmount = 0;
		if (taxAuth!=null) {
			taxPer = getTotalTaxPercentageUnVersioned(ctx, taxAuth, trans.getSpid());
			taxAmount = MathSupportUtil.round(ctx,  trans.getSpid(), amount * (taxPer/100));			
			if (LogSupport.isDebugEnabled(ctx)){
				LogSupport.debug(ctx, MODULE, "Tax Amount for transaction record :: " + taxAmount);
			}
		}
		else {
			if (LogSupport.isDebugEnabled(ctx)){
				LogSupport.debug(ctx, MODULE, "Tax Authority is null for trans " + trans.getResponsibleBAN());
			}
		}
	 return taxAmount;
    }

    public static double getTotalTaxPercentageUnVersioned(final Context ctx, final TaxAuthority taxAuth, final int spid)
    {
        double taxRate = taxAuth.getTaxRateUnversioned();
        final List<TaxComponent> listOfTaxRates = taxAuth.getTaxComponents();
        for (final TaxComponent trs : listOfTaxRates)
        {
            taxRate += trs.getTaxRate();
        }
        return taxRate;
    }
    
}