/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.writeoff;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.csv.AbstractCSVSupport;
import com.trilogy.framework.xhome.support.StringSeperator;


/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
final class WriteOffInputCsvSupport extends AbstractCSVSupport
{

    public static final WriteOffInputCsvSupport Instance = new WriteOffInputCsvSupport();


    private WriteOffInputCsvSupport()
    {
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.csv.CSVSupport#append(java.lang.StringBuffer,
     * char, java.lang.Object)
     */
    public StringBuffer append(StringBuffer buf, char delimiter, Object obj)
    {
        throw new UnsupportedOperationException("WriteOffInputCsvSupport is used only for parsing purpose.");
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.csv.CSVSupport#parse(com.redknee.framework.xhome.support
     * .StringSeperator)
     */
    public Object parse(StringSeperator seperator)
    {
        List<String> list = toStringArray(seperator);
        WriteOffInput bean = new WriteOffInput();
        try
        {
            bean.OriginalString = list.toString();
            bean.SPID = parseInt(list.get(0));
            bean.Type = parseInt(list.get(1));
            bean.setBan(parseString(list.get(2)));
            bean.BillCycle = parseInt(list.get(3));
            bean.State = parseInt(list.get(7));
            bean.ExternalTransactionId = parseLong(list.get(8));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Illegal arguments in input: " + list, e);
        }
        return bean;
    }


    private long parseLong(String longNum)
    {
        long ret = WriteOffInput.INVALID_NUMBER;
        if (longNum != null)
        {
            longNum = longNum.trim();
            if (longNum.length() > 0)
            {
                ret = Long.parseLong(longNum);
            }
        }
        return ret;
    }


    private int parseInt(String intNum)
    {
        int ret = WriteOffInput.INVALID_NUMBER;
        if (intNum != null)
        {
            intNum = intNum.trim();
            if (intNum.length() > 0)
            {
                ret = Integer.parseInt(intNum);
            }
        }
        return ret;
    }


    private static List<String> toStringArray(StringSeperator sep)
    {
        List<String> tokens = new ArrayList<String>(8);
        while (sep.hasNext())
        {
            String item = sep.next();
            if (item != null)
            {
                tokens.add(item);
            }
        }
        return tokens;
    }
}