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

package com.trilogy.app.crm.log;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xlog.er.ERSupport;

import com.trilogy.app.crm.bean.AccountStateEnum;


/**
 * {{{GENERATED_CODE}}}
 * Methods for reading and writing ERs.
 * This class was saved after being generated because the model is reused for ER generation and DB save.
 * For DB save model is specifying s-transient fields that are excluded from ER generation,
 * but we need them in the ER. Also Date fields need to be formatted differently.
 *
 * @author joel.hughes@redknee.com
 * @author victor.stratan@redknee.com
 */
public class CustomDunningActionERERSupport implements ERSupport
{
    protected static CustomDunningActionERERSupport instance__ = null;

    public static synchronized CustomDunningActionERERSupport instance()
    {
        if (instance__ == null)
        {
            instance__ = new CustomDunningActionERERSupport();
        }

        return instance__;
    }

    public String[] getFields(final Context ctx, final Object obj)
    {
        DunningActionER bean = (DunningActionER) obj;

        String[] fields = new String[9];
        int index = 0;

        fields[index++] = bean.getVoiceMobileNumber();
        fields[index++] = bean.getBAN();
        fields[index++] = AccountStateEnum.get((short)bean.getOldAccountState()).getDescription();
        fields[index++] = AccountStateEnum.get((short)bean.getNewAccountState()).getDescription();
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getPromiseToPayDate());
        fields[index++] = String.valueOf(bean.getResultCode());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getToBeDunnedDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getToBeInArrearsDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getDueDate());

        return fields;
    }


    public Object setFields(final Context ctx, final Object obj, final String[] fields)
    {
        DunningActionER bean = (DunningActionER) obj;

        String field = null;
        int index = 0;

        field = fields[index++];
        bean.setVoiceMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setBAN((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldAccountState((field == null) ? -1 : Integer.parseInt(field));
        field = fields[index++];
        bean.setNewAccountState((field == null) ? -1 : Integer.parseInt(field));
        field = fields[index++];
        bean.setPromiseToPayDate((field == null) ? null : ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setToBeDunnedDate((field == null) ? null : ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setToBeInArrearsDate((field == null) ? null : ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setDueDate((field == null) ? null : ERLogger.parseERDateDayOnlyNoException(ctx, field));

        return bean;
    }


    public Object setFields(final Context ctx, final Object obj, final StringSeperator seperator)
    {
        DunningActionER bean = (DunningActionER) obj;
        String field = null;

        field = seperator.next();
        bean.setVoiceMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setBAN((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldAccountState((field == null) ? -1 : Integer.parseInt(field));
        field = seperator.next();
        bean.setNewAccountState((field == null) ? -1 : Integer.parseInt(field));
        field = seperator.next();
        bean.setPromiseToPayDate((field == null) ? null : ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setToBeDunnedDate((field == null) ? null : ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setToBeInArrearsDate((field == null) ? null : ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setDueDate((field == null) ? null : ERLogger.parseERDateDayOnlyNoException(ctx, field));

        return bean;
    }

}
