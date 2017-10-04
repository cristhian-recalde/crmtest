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
package com.trilogy.app.crm.support;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.IdFormat;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationXInfo;

/**
 * Identification Type support methods
 *
 * This is not implemented as a simple Validator on Subscriber Home, because we want to avoid this validation when the
 * subscriber is not updated or created from GUI. Existing subscriber objects that are updated automatialy should
 * not be validated.
 * Validator on Subscriber Home could be made possible if it had a flag signaling if it is a GUI update.
 * The problem with this approach is in multinode deployment, because Context is not passed from E-Care to BAS
 * and the validation is run on the BAS node so it is hard to signal that it is web update.
 *
 * @author arturo.medina@redknee.com
 */
public class IdentificationSupport
{
    /**
     * Validates the identification type according to the rules
     * configured on the Identification type.
     *
     * @param ctx
     * @param idType
     * @param idNumber
     * @param typeProperty
     * @param idProperty
     * @param el
     */
	public static void validateIdNumber(final Context ctx, final int idType,
	    final String idNumber, Date expiryDate,
	    final PropertyInfo typeProperty, final PropertyInfo idProperty,
	    PropertyInfo expiryDateProperty, CompoundIllegalStateException el)
    {
        Identification id = null;

        try
        {
            id = HomeSupportHelper.get(ctx).findBean(ctx, Identification.class, idType);
            if (id == null)
            {
                throwPropertyException(typeProperty, "Identification type " + idType + " not found.", null, el);
                return;
            }
        }
        catch (HomeException e)
        {
            LogSupport.major(ctx, "[" + typeProperty.getName() + "] - IdentificationSupport",
                    "Home exception " + e.getMessage(), e);
            final String msg = "IdentificationSupport Home exception " + e.getMessage();
            throwPropertyException(typeProperty, msg, e, el);
            return;
        }

		// validate expiry date
		if (expiryDate == null || expiryDate.getTime() == 0)
		{
			if (id.isMandatoryExpiryDate())
			{
				StringBuilder sb = new StringBuilder();
				sb.append("Expiry date is mandatory for identification type '");
				sb.append(id.getCode() + " - " + id.getDesc() + "'");
				LogSupport.info(ctx, "IdentificationSupport.validateIdNumber",
				    sb.toString());
				throwPropertyException(expiryDateProperty, sb.toString(), null,
				    el);
				return;
			}
		}

        IdFormat format = id.getFormat();
        if ( format != null)
        {
            String substring = idNumber;
            String regularExp = format.getRegEx();
            try
            {
                validate(regularExp,substring);
            }
            catch(IllegalArgumentException e)
            {
                String msg = format.getErrorMsg();
                if (msg == null || msg.trim().length() == 0)
                {
                    msg = e.getMessage();
                }
                throwPropertyException(idProperty, msg, null,el);
            }
        }
        else
        {
            final String msg = "No format configured for this identification type.";
            throwPropertyException(typeProperty, msg, null,el);
        }

   
    }
    
    private static void validate(String pattern, String str) throws IllegalArgumentException
    {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        boolean matched = m.matches();
        if (!matched)
        {
            throw new IllegalArgumentException("Invalid input for identification");
        }
        
    }

    private static void throwPropertyException(final PropertyInfo idProperty, final String msg, final Exception cause, CompoundIllegalStateException el)
    {
        final IllegalPropertyArgumentException exception = new IllegalPropertyArgumentException(idProperty, msg);
        if (cause != null)
        {
            exception.initCause(cause);
        }
        el.thrown(exception);
    }

    /**
     * @param identification
     * @return
     */
    public static String getExample(final Identification identification)
    {
        final IdFormat format = identification.getFormat();
        final StringBuilder example = new StringBuilder();
        if (format != null)
        {
            example.append(format.getRegEx());
        }

        return example.toString();
    }

    /**
     * Parses and finds the identification type. idType can be either a number as String or the id description.
     *
     * @param ctx
     * @param idType
     * @return
     */
    public static int getIdType(final Context ctx, int spid, final String idType)
    {
        if (idType == null)
        {
            return -1;
        }

        int type = -1;
        try
        {
            type = Integer.parseInt(idType);
        }
        catch (Throwable th)
        {

            try
            {
                And where = new And();
                where.add(new EQ(IdentificationXInfo.DESC, idType));
                where.add(new EQ(IdentificationXInfo.SPID, spid));
                final Identification id = HomeSupportHelper.get(ctx).findBean(ctx, Identification.class, where);
                if (id != null)
                {
                    type = id.getCode();
                }
            }
            catch (HomeException e)
            {
                LogSupport.debug(ctx, "[IdentificationSupport.getIdType]", e.getMessage(), e);
            }

        }

        return type;
    }
    public static Identification getIdentification(final Context ctx, final int idType)
    {
        Identification id = null;
        try
        {
            id = HomeSupportHelper.get(ctx).findBean(ctx, Identification.class, idType);
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, "[IdentificationSupport.getIdentification]", e.getMessage(), e);
        }
        return id;
    }

}