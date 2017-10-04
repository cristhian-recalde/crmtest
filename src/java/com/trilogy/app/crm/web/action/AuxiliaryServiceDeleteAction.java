/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.web.action.DeleteAction;
import com.trilogy.framework.xhome.web.util.Link;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;

/**
 * Provides a link to the "Reactivate Subscriber" screen.
 *
 * @author Albert Tse
 */
public class AuxiliaryServiceDeleteAction
    extends DeleteAction implements Predicate
{
    /**
     * Create a new AuxiliaryServiceDeleteAction.
     */
    public AuxiliaryServiceDeleteAction()
    {
        super();
    }


    /**
     * Create a new AuxiliaryServiceDeleteAction with the given permission.
     *
     * @param permission The permission required to use the action.
     */
    public AuxiliaryServiceDeleteAction(final Permission permission)
    {
        super (permission);
    }


    // INHERIT
    @Override
    public void writeLink(
        final Context ctx,
        final PrintWriter out,
        final Object bean,
        final Link link)
    {
        if (bean instanceof com.redknee.app.crm.bean.AuxiliaryService
                || bean instanceof com.redknee.app.crm.bean.ui.AuxiliaryService)
        {                    
            if (f(ctx,bean))            
            {
                super.writeLink(ctx,out,bean,link);
            }        
        }
        else
        {
            return;
        }
    }


	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context arg0, Object bean) throws AbortVisitException {
	    AuxiliaryServiceTypeEnum type = null;
	    if (bean instanceof com.redknee.app.crm.bean.AuxiliaryService)
	    {
	        type = ((com.redknee.app.crm.bean.AuxiliaryService) bean).getType();
	    }
	    else if (bean instanceof com.redknee.app.crm.bean.ui.AuxiliaryService)
	    {
            type = ((com.redknee.app.crm.bean.ui.AuxiliaryService) bean).getType();
	    }
		
		return (type != null && type != AuxiliaryServiceTypeEnum.CallingGroup);
	}


	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.web.action.WebAction#setPredicate(com.redknee.framework.xhome.filter.Predicate)
	 */


} //class