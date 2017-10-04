package com.trilogy.app.crm.integration.pc;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.AdjustmentTypeVersion;

public class CheckAdjustmentTypeVersionNStatePredicate implements Predicate {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean f(Context ctx, Object obj) {
		
		LogSupport.debug(ctx, this, "[CheckAdjustmentTypeVersionNStatePredicate.f] checking if template state is Draft");
		
		AdjustmentTypeVersion adjversionn = (AdjustmentTypeVersion) obj;
		if (adjversionn != null) {
			return adjversionn.getVersionDate().compareTo(new Date()) > 0;
		}
		return false;
	}

}
