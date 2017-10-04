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
package com.trilogy.app.crm.bulkloader;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.util.format.ThreadLocalSimpleDateFormat;

/**
 * @author victor.stratan@redknee.com
 */
public class BulkLoadSupport
{
    /**
     * Converts a string to a Date. returns the default_ value if parsing fails.
     *
     * @param ctx           the operating context
     * @param str           string containing an integer
     * @param default_      default value, if error
     * @param dateFormatter date format object
     * @param info          property info for the field that is being set
     * @return the parsed Date
     * @throws com.redknee.framework.xhome.context.AgentException
     *          thrown if Date parsing fails
     */
    public static Date getDate(final Context ctx, final String str, final Date default_,
            final ThreadLocalSimpleDateFormat dateFormatter, final PropertyInfo info) throws AgentException
    {
        if (str == null || str.length() == 0)
        {
            return default_;
        }
        try
        {
            return dateFormatter.parse(str);
        }
        catch (Exception e)
        {
            final String label = info.getLabel(ctx);
            throw new AgentException("Date field " + label + " with value [" + str
                    + "] could not be parsed, please check Date format.", e);
        }
    }

    /**
     * Converts a string to a Date. Throws exception if string is empty.
     *
     * @param ctx  the operating context
     * @param str  string containing an integer
     * @param info property info for the field that is being set
     * @param dateFormatter date format object
     * @return the parsed Date
     * @throws com.redknee.framework.xhome.context.AgentException if date cannot be parsed or string is empty
     */
    public static Date getMandatoryDate(final Context ctx, final String str,
            final ThreadLocalSimpleDateFormat dateFormatter, final PropertyInfo info) throws AgentException
    {
        if (str == null || str.length() == 0)
        {
            final String label = info.getLabel(ctx);
            throw new AgentException("Mandatory field " + label + " not set.");
        }
        try
        {
            return dateFormatter.parse(str);
        }
        catch (Exception e)
        {
            final String label = info.getLabel(ctx);
            throw new AgentException("Date field " + label + " with value [" + str
                    + "] could not be parsed, please check Date format.", e);
        }
    }
}
