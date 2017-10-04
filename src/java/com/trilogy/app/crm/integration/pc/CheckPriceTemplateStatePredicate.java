package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.TemplateStateEnum;

/**
 * 
 * @author AChatterjee
 *
 */
public class CheckPriceTemplateStatePredicate implements Predicate{

	private static final long serialVersionUID = 1L;

	@Override
	public boolean f(Context ctx, Object obj) throws AbortVisitException {
		
		LogSupport.debug(ctx, this, "[CheckPriceTemplateStatePredicate.f] Start");
		
		if(obj instanceof PriceTemplate) {
			PriceTemplate priceTemplate = (PriceTemplate) obj;
			if (priceTemplate != null)
			{
				return priceTemplate.getTemplateState() == TemplateStateEnum.DRAFT;
			}
		}
		return false;
	}

}
