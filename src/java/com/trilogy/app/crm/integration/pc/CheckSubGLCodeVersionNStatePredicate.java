package com.trilogy.app.crm.integration.pc;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
//import com.trilogy.app.crm.bean.GLCodeVersionN;
//import com.trilogy.app.crm.bean.ui.SubGLCodeN;
import com.trilogy.app.crm.bean.ui.SubGLCodeVersionN;
//import com.trilogy.app.crm.bean.ui.SubGLCodeVersionNHome;

public class CheckSubGLCodeVersionNStatePredicate implements Predicate{
	
	private static final long serialVersionUID = 1L;

	@Override
	public boolean f(Context ctx, Object obj)
	{
	   SubGLCodeVersionN subglcodeversionn = (SubGLCodeVersionN) obj;
	   if (subglcodeversionn != null)
	   {
		   return subglcodeversionn.getEffectiveFromDate().compareTo(new Date()) > 0;   
	   }
	   return false;
	}

}
