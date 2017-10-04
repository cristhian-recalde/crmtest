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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.beans.facets.java.lang.StringWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * Currency web control which displays an error message instead of the value if
 * it matches a predetermined error value.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class BalanceDisplayWebControl extends XCurrencyWebControl
{
	public static String DEFAULT_ERROR_MESSAGE = "Balance Not Available";

	/**
	 * Constructor for BalanceDisplayWebControl.
	 */
	public BalanceDisplayWebControl()
	{
		this(true);
	}

	/**
	 * Constructor for BalanceDisplayWebControl.
	 * 
	 * @param showCurrency
	 *            Whether to display the currency.
	 */
	public BalanceDisplayWebControl(boolean showCurrency)
	{
		this(true, SubscriberSupport.INVALID_VALUE);
	}

	/**
	 * Constructor for BalanceDisplayWebControl.
	 * 
	 * @param showCurrency
	 *            Whether to display the currency.
	 * @param errorValue
	 *            The value to be considered an error value.
	 */
	public BalanceDisplayWebControl(boolean showCurrency, long errorValue)
	{
		this(true, errorValue, DEFAULT_ERROR_MESSAGE);
	}

	/**
	 * Constructor for BalanceDisplayWebControl.
	 * 
	 * @param showCurrency
	 *            Whether to display the currency.
	 * @param errorValue
	 *            The value to be considered an error value.
	 * @param errorMessage
	 *            The message to display if the provided value is an error
	 *            value.
	 */
	public BalanceDisplayWebControl(boolean showCurrency, long errorValue,
	    String errorMessage)
	{
		super(showCurrency);
		errorValue_ = errorValue;
		errorMessage_ = errorMessage;
	}

	/**
	 * Display the currency value. If the value matches the error value set in
	 * this web control, display the error message instead.
	 * 
	 * @param ctx
	 *            Operating context.
	 * @param out
	 *            Output writer.
	 * @param name
	 *            Name of the field.
	 * @param obj
	 *            Object to be displayed.
	 * @see XCurrencyWebControl#toWeb(Context, PrintWriter, String, Object)
	 */
	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		if (obj instanceof Number || obj == null)
		{
			Number number = (Number) obj;
			if (number == null || number.longValue() == errorValue_)
			{
				MessageMgr mmgr = new MessageMgr(ctx, getClass());
				String message =
				    mmgr.get(name + ".defaultMessage", errorMessage_);
				messageWebControl_.toWeb(ctx, out, name, message);
				return;
			}
		}

		super.toWeb(ctx, out, name, obj);
	}

	private final long errorValue_;
	private final String errorMessage_;
	private final WebControl messageWebControl_ = new ReadOnlyWebControl(
	    new StringWebControl());
}
