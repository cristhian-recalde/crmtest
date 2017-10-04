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
package com.trilogy.app.crm.client.urcs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;

/**
 * @author sbanerjee
 *
 */
public class UrcsDateExtensionSupport
{
    public static final String URCS_EXP_DATE_FORMAT = "yyyyMMdd";

    /**
     * 
     * @param ctx
     * @param calendar
     * @param newExpiryDate
     * @return
     * @throws ParseException
     */
    public static Calendar getUpdatedExpiryDate(Context ctx, final Calendar calendar, String newExpiryDate)
            throws ParseException
    {
        if(newExpiryDate==null)
            return null;
        SimpleDateFormat df = new SimpleDateFormat(URCS_EXP_DATE_FORMAT);
        Date newExp = df.parse(newExpiryDate);
        calendar.setTime(newExp);
        return calendar;
    }

}
