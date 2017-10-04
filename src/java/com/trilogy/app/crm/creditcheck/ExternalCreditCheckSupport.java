package com.trilogy.app.crm.creditcheck;

import java.util.List;

import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.ExternalCreditCheck;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.util.StringUtil;

public class ExternalCreditCheckSupport {

	public static void validateSpid(Context ctx, ExternalCreditCheck externalCreditCheck)
			throws HomeException {
		if(externalCreditCheck.getSpid()==-1){
			new MinorLogMsg(ExternalCreditCheckSupport.class, "<<< ExternalCreditCheck: Spid is Mandatory and cannot be null.>>>").log(ctx);
			throw new HomeException(": Spid is missing.");
		}
	}

	public static void validateBan(Context ctx, ExternalCreditCheck externalCreditCheck)
			throws HomeException {
		if(externalCreditCheck.getBan() == null && "".equals(externalCreditCheck.getBan())){
			new MinorLogMsg(ExternalCreditCheckSupport.class, "<<< ExternalCreditCheck: BAN is Mandatory and cannot be null.>>>").log(ctx);
			throw new HomeException(": BAN is missing.");
		}
		
		And filter = new And();
		filter.add(new EQ(AccountXInfo.SPID, externalCreditCheck.getSpid()));
		filter.add(new EQ(AccountXInfo.BAN, externalCreditCheck.getBan()));
		List<Account> accounts = (List<Account>) HomeSupportHelper.get(ctx).getBeans(ctx, Account.class, filter);
		if(accounts!=null && accounts.isEmpty()){
			new MinorLogMsg(ExternalCreditCheckSupport.class, "<<< ExternalCreditCheck: BAN/Spid is invalid.>>>").log(ctx);
			throw new HomeException(": BAN/Spid is invalid.");
		}
	}
	
	public static void validateCreditCheckResults(Context ctx, ExternalCreditCheck externalCreditCheck)
			throws HomeException {
		if(externalCreditCheck.getCreditCheckResult()==null || externalCreditCheck.getCreditCheckResult().size()==0){
			new MinorLogMsg(ExternalCreditCheckSupport.class, "<<< ExternalCreditCheck: CreditCheckResult is Mandatory and cannot be null.>>>").log(ctx);
			throw new HomeException(": CreditCheckResult is missing.");
		}
	}
	
	public static void validateCreditCheckDate(Context ctx, ExternalCreditCheck externalCreditCheck)
			throws HomeException {
		if(externalCreditCheck.getCreditCheckDate()==null){
			new MinorLogMsg(ExternalCreditCheckSupport.class, "<<< ExternalCreditCheck: CreditCheckDate is Mandatory and cannot be null.>>>").log(ctx);
			throw new HomeException(": CreditCheckDate is missing.");
		}
	}
	
	public static void validateSystemDefinedCreditCategory(Context ctx, ExternalCreditCheck externalCreditCheck)
			throws HomeException {
		if(externalCreditCheck.getSystemDefinedCreditCategory()==-1){
			new MinorLogMsg(ExternalCreditCheckSupport.class, "<<< ExternalCreditCheck: SystemCreditCategory is Mandatory and cannot be null.>>>").log(ctx);
			throw new HomeException(": SystemCreditCategory is missing.");
		}
	}
	
	public static void validateManuallyUpdatedCreditCategory(Context ctx, ExternalCreditCheck externalCreditCheck)
			throws HomeException {
		if(externalCreditCheck.getManuallyUpdatedCreditCategory()!=-1 && (externalCreditCheck.getAssociatedUser() == null || "".equals(externalCreditCheck.getAssociatedUser()))){
			new MinorLogMsg(ExternalCreditCheckSupport.class, "<<< Failure while validating the credit check parameters [ Manually updated credit category"
					+ "is not -1 and associated user is null or empty....>>>"+externalCreditCheck.getManuallyUpdatedCreditCategory() +" "+externalCreditCheck.getAssociatedUser()).log(ctx);
			throw new HomeException(": Associated user must be sent if credit category is manually updated.");
		}
		
		if(externalCreditCheck.getSystemDefinedCreditCategory()!=-1 && externalCreditCheck.getManuallyUpdatedCreditCategory()==-1
				&& (!(externalCreditCheck.getAssociatedUser() == null || "".equals(externalCreditCheck.getAssociatedUser())))){
			new MinorLogMsg(ExternalCreditCheckSupport.class, "<<< Failure while validating the credit check parameters [ Manually updated credit category"
					+ "is -1 and associated user is not null....>>>"+externalCreditCheck.getManuallyUpdatedCreditCategory() +" "+externalCreditCheck.getAssociatedUser()).log(ctx);
			throw new HomeException(": if credit category is not manually updated then associated user must be null.");
		}
	}
}
