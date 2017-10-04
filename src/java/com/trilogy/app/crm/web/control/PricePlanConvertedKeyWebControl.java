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

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.PricePlanKeyWebControl;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author psperneac
 */
public class PricePlanConvertedKeyWebControl extends PricePlanKeyWebControl
{
	/**
	 * 
	 */
	public PricePlanConvertedKeyWebControl()
	{
		super();
	}
	/**
	 * @param autoPreview
	 */
	public PricePlanConvertedKeyWebControl(boolean autoPreview)
	{
		super(autoPreview);
	}
	/**
	 * @param listSize
	 */
	public PricePlanConvertedKeyWebControl(int listSize)
	{
		super(listSize);
	}
	/**
	 * @param listSize
	 * @param autoPreview
	 */
	public PricePlanConvertedKeyWebControl(int listSize, boolean autoPreview)
	{
		super(listSize, autoPreview);
	}
	/**
	 * @param listSize
	 * @param autoPreview
	 * @param isOptional
	 */
	public PricePlanConvertedKeyWebControl(int listSize, boolean autoPreview, boolean isOptional)
	{
		super(listSize, autoPreview, isOptional);
	}
	/**
	 * @param listSize
	 * @param autoPreview
	 * @param isOptional
	 * @param allowCustom
	 */
	public PricePlanConvertedKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, boolean allowCustom)
	{
		super(listSize, autoPreview, isOptional, allowCustom);
	}
	/**
	 * @param listSize
	 * @param autoPreview
	 * @param optionalValue
	 */
	public PricePlanConvertedKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
	{
		super(listSize, autoPreview, optionalValue);
	}
	/**
	 * @param listSize
	 * @param autoPreview
	 * @param optionalValue
	 * @param allowCustom
	 */
	public PricePlanConvertedKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, boolean allowCustom)
	{
		super(listSize, autoPreview, optionalValue, allowCustom);
	}
	/**
	 * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
	 */
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		if(obj instanceof Number)
		{
			obj=Long.valueOf(obj.toString());
		}
		
		super.toWeb(ctx, out, name, obj);
	}
}
