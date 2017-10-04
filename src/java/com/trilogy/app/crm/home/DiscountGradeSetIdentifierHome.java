package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.DiscountGrade;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class DiscountGradeSetIdentifierHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	public DiscountGradeSetIdentifierHome(final Context ctx, final Home delegate) {
		super(ctx, delegate);
	}

	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		DiscountGrade discountGrade = (DiscountGrade) obj;

		long discountGradeId = getNextIdentifier(ctx);
		if (discountGrade.getIdentifier() == null || discountGrade.getIdentifier().trim().isEmpty() ) {
			discountGrade.setIdentifier(String.valueOf(discountGradeId));
		}
		LogSupport.info(ctx, this,
				"DiscountGrade ID set to: " + discountGrade.getIdentifier());
		return super.create(ctx, obj);
	}

	private long getNextIdentifier(Context ctx) throws HomeException {
		IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx,
				IdentifierEnum.DISCOUNTGRADEID, 1L, 9223372036854775807L);

		return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx,
				IdentifierEnum.DISCOUNTGRADEID, null);
	}
}