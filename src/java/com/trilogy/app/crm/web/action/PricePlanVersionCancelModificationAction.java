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
 *
 */
package com.trilogy.app.crm.web.action;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.util.Link;

/**
 * Action responsible to cancel a price plan version modification request
 * @author Marcio Marques
 * @since 9.2
 */
public class PricePlanVersionCancelModificationAction extends SimpleWebAction
{

	public static final String DEFAULT_KEY = "cancelModification";

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for PricePlanVersionCancelActivationAction.
	 */
	public PricePlanVersionCancelModificationAction()
	{
		super(DEFAULT_KEY, DEFAULT_KEY);
		defaultHelpText_ = "Modify the version.";
	}

	/**
	 * Constructor for PricePlanVersionCancelActivationAction.
	 * 
	 * @param key
	 *            Key for this web action.
	 * @param label
	 *            Lable for this web action.
	 */
	public PricePlanVersionCancelModificationAction(final String key, final String label)
	{
		super(key, label);
	}

    public Link modifyLink(Context ctx, Object bean, Link link)
    {
    	link.getMap().put("cmd", "appCRMPPVModificationRequest");
        link.add("action", DEFAULT_KEY);
        return link;
    }

}
