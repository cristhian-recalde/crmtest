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
import java.text.DecimalFormat;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AlignmentEnum;
import com.trilogy.framework.xhome.webcontrol.LongWebControl;

/**
 * @author jchen
 */
public class HiddenLongWebControl extends LongWebControl
{

	/**
	 * @param width
	 */
	public HiddenLongWebControl(int width)
	{
		super(width);
	}

	/**
	 * @param width
	 * @param align
	 */
	public HiddenLongWebControl(int width, AlignmentEnum align)
	{
		super(width, align);
	}

	/**
	 * @param width
	 * @param base
	 */
	public HiddenLongWebControl(int width, int base)
	{
		super(width, base);
	}

	/**
	 * @param width
	 * @param base
	 * @param align
	 */
	public HiddenLongWebControl(int width, int base, AlignmentEnum align)
	{
		super(width, base, align);
	}

	/**
	 * @param width
	 * @param base
	 * @param maxLength
	 */
	public HiddenLongWebControl(int width, int base, int maxLength)
	{
		super(width, base, maxLength);
	}

	/**
	 * @param width
	 * @param format
	 */
	public HiddenLongWebControl(int width, String format)
	{
		super(width, format);
	}

	/**
	 * @param width
	 * @param format
	 */
	public HiddenLongWebControl(int width, DecimalFormat format)
	{
		super(width, format);
	}

	/**
	 * @param width
	 * @param format
	 * @param base
	 * @param min
	 * @param max
	 * @param align
	 * @param maxLength
	 */
	public HiddenLongWebControl(int width, DecimalFormat format, int base, long min, long max, AlignmentEnum align,
		int maxLength)
	{
		super(width, format, base, min, max, align, maxLength);
	}

	/**
	 * @param width
	 * @param format
	 * @param min
	 * @param max
	 */
	public HiddenLongWebControl(int width, DecimalFormat format, long min, long max)
	{
		super(width, format, min, max);
	}

	/**
	 * 
	 */
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		long value = ((Number) obj).longValue();

		out.print("<input type=\"hidden\" name=\"");
		out.print(name);
		out.print("\" ");
		out.print("size=\"");
		out.print(width_);
		out.print("\" ");

		if (maxLength_ != -1)
		{
			out.print("maxlength=\"");
			out.print(maxLength_);
			out.print("\" ");
		}

		out.print("value=\"");

		out.print(format(value));
		out.println("\" onChange=\"try{checkLong(this," + min_ + "," + max_ + ",true);}catch(everything){}\" />");
	}

}
