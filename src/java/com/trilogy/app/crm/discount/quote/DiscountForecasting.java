package com.trilogy.app.crm.discount.quote;

import com.trilogy.framework.xhome.context.Context;

public interface DiscountForecasting 
{
	void foreCastDiscount(Context ctx, QuoteDiscountRequest request) throws QuoteDiscountException;
}
