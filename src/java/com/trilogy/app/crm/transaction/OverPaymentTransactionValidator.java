package com.trilogy.app.crm.transaction;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * Validator for OverPayment Transaction
 * @author shailesh.makhijani
 * @since 9.7.1.1
 *
 */
public class OverPaymentTransactionValidator implements Validator {

	@Override
	public void validate(Context ctx, Object obj)
			throws IllegalStateException {
		CompoundIllegalStateException validationException = new CompoundIllegalStateException();
		
		if (obj instanceof Transaction){
			
			Transaction trans = (Transaction)obj;
			final int overPaymentAdjust = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx, AdjustmentTypeEnum.OverPaymentCredit);

			if (overPaymentAdjust == trans.getAdjustmentType()) {
				validationException.thrown(new IllegalPropertyArgumentException(TransactionXInfo.ADJUSTMENT_TYPE, "Manual OverPayment Credit transaction"
						+ " not allowed through BSS GUI and API "));
			}
			
			validationException.throwAll();
		}
	}
}
