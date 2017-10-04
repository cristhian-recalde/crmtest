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
package com.trilogy.app.crm.bulkloader.generic;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Using the configuration of the Generic Bean Bulkloader, create a CSV format example. 
 * @author angie.li@redknee.com
 *
 * @since 8.2
 */
public class SampleFormatWebControl extends TextFieldWebControl
{
    public SampleFormatWebControl()
    {
        super();
    }

    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        StringBuilder format = new StringBuilder();

        try
        {
            Object parentBean = ctx.get(AbstractWebControl.BEAN);
            if (parentBean instanceof GenericBeanBulkloader)
            {
                CSVParser parser = new CSVParser(ctx, (GenericBeanBulkloader) parentBean);

                // We'll merge the two lists of fields (Input and Search fields) into one list.
                MultiValueMap mergedFields = parser.getInputFields();
                MultiValueMap searchFields = parser.getSearchFields();
                

                Iterator<Integer> iterator = searchFields.keySet().iterator();
                while(iterator.hasNext())
                {
                    Integer index = iterator.next();
                    if (mergedFields.containsKey(index))
                    {
                        String msg = "<font color=\"red\">The Bulkloader Configuration has identical indexes set for two different fields.  Please use unique indexes.</font>";
                        new MinorLogMsg(this, msg, null).log(ctx);
                        format.append(msg);
                        break;
                    }
                    else
                    {
                        for (PropertyInfo searchFieldPropertyInfo : (List<PropertyInfo>) searchFields.getCollection(index))
                        {
                            mergedFields.put(index, searchFieldPropertyInfo);
                        }
                        
                    }
                }
                
                Iterator<Integer> i = mergedFields.keySet().iterator();

                if (format.length() == 0)
                {
                    //No error messages yet.  Continue to create the Sample.
                    int position = 0; //Used to measure extra delimiter padding.
                    while(i.hasNext())
                    {
                        Integer index = i.next();
                        List<PropertyInfo> propertyList = (List<PropertyInfo>) mergedFields.get(index);
                        int counter = 1;
                        for (int j = 0; j < propertyList.size(); j++)
                        {
                            //Add extra delimiters padding
                            if(j >= counter)
                            {
                                format.append(" & ");
                            }
                            //Add extra delimiters padding
                            if(index > position)
                            {
                                int numberOfPaddedCommas = index - position;
                                for (int count=0; count<numberOfPaddedCommas; count++)
                                {
                                    format.append(parser.getDelimiter());
                                }
                            }
                            PropertyInfo property = propertyList.get(j);
                            format.append(property.getName());
                            /* If I append a delimiter after the property, then there will be an extra delimiter at the 
                             * end of the sample. Instead, we don't pad the end, and add the delimiter before the property
                             * as it is written.  By marking the current position as position == index (instead of 
                             * position == index + 1) we allow the Extra delimiter padding condition to add the delimiter
                             * for us.  (Also, this way we avoid the extra delimiter at the beginning of the command --
                             * which is wrong.)*/ 
                            position = index;
                        }
                    }
                }
            }
        }
        catch (Throwable t)
        {
            format.append("<font color=\"red\">" + t.getMessage() + "</font>");
        }

        super.toWeb(ctx, out, name, format.toString());
    }

}
