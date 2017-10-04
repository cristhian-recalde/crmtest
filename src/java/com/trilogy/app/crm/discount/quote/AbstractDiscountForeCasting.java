package com.trilogy.app.crm.discount.quote;

import com.trilogy.framework.xhome.context.Context;

public abstract class AbstractDiscountForeCasting implements DiscountForecasting {
	
	protected abstract void prepareDataForDiscountForecasting(Context ctx, QuoteDiscountRequest req) throws QuoteDiscountException;	
	
	protected abstract void findApplicableDiscounts(Context ctx);
	
	protected abstract void calculateDiscountValue(Context ctx);
	
	protected abstract void formatResponse(Context ctx);
	
	@Override
	public void foreCastDiscount(Context ctx, QuoteDiscountRequest request) throws QuoteDiscountException{
		Context subCtx = ctx.createSubContext();
		prepareDataForDiscountForecasting(subCtx, request);
		findApplicableDiscounts(subCtx);
		calculateDiscountValue(subCtx);
		formatResponse(subCtx);
		
	}
}
