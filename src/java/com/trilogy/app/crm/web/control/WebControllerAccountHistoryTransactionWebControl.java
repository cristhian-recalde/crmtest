/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.action.ViewAction;
import com.trilogy.framework.xhome.webcontrol.WebController;


/**
 * Provides a custom WebController for the versions of a price plan that set up
 * the action links and buttons properly.
 *
 * @author gary.anderson@redknee.com
 */
public
class WebControllerAccountHistoryTransactionWebControl
    extends WebControllerWebControl57
{
    /**
     * Creates a new WebController.
     */
    public WebControllerAccountHistoryTransactionWebControl(Class beanType)
    {
        super(beanType);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context context)
        throws AgentException
    {
        final List list = new ArrayList();
        list.add(new ViewAction());
        //list.add(new PricePlanVersionDeleteAction());

        final Context subContext = context.createSubContext();
        ActionMgr.setActions(subContext, list);

        super.execute(subContext);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setUpWebControlFunctionButton(
        final Context context,
        final WebController controller,
        final Class type)
    {
        super.setUpWebControlFunctionButton(context, controller, type);
        controller.setSummaryBorder(null);
        controller.setNewEnabled(false);
        controller.setDeleteEnabled(false);
        controller.setUpdateEnabled(false);
        controller.setCopyEnabled(false);
        controller.setHelpEnabled(false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String createSQLClause(final Object key)
    {
        return "BAN = '" + key+"'";
    }

} // class
