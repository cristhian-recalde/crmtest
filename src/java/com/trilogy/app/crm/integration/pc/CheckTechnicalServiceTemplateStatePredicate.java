package com.trilogy.app.crm.integration.pc;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.TemplateStateEnum;

public class CheckTechnicalServiceTemplateStatePredicate implements Predicate
{

	private static final long serialVersionUID = 1L;

	@Override
	public boolean f(Context ctx, Object obj)
	{
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, this, "[CheckTechnicalServiceTemplateStatePredicate.f] checking if template state is Draft");
		}
		
		if(obj instanceof TechnicalServiceTemplate){
			TechnicalServiceTemplate technicalServiceTemplate = (TechnicalServiceTemplate) obj;
			if (technicalServiceTemplate != null)
			{
				return technicalServiceTemplate.getTemplateState() == TemplateStateEnum.DRAFT;
			}
		}
		return false;
		
	}

}
