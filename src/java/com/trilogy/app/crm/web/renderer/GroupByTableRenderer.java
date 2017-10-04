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
package com.trilogy.app.crm.web.renderer;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRendererProxy;
import com.trilogy.framework.xhome.webcontrol.TableWebControl;

/**
 * Groups items in a table by the given property.  For presentation purposes,
 * the list items should be sorted by this property.  Unsorted lists will result
 * in duplicate headers being output.
 * 
 * TODO: [Enhancement] Make it possible for sections to be collapsable (with
 * memory about which sections are open on screen refresh.  Also consider expand
 * all/collapse all functionality.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class GroupByTableRenderer extends TableRendererProxy
{
    private static final long serialVersionUID = 1L;
    
    final protected int colspan_;
    final protected PropertyInfo property_;
    final protected Map<Object, String> descriptionMap_;
    protected Object lastValue_ = "";

    protected boolean isFirstRow_ = true;

    public GroupByTableRenderer(Context ctx, final PropertyInfo property, final TableRenderer delegate)
    {
        this(ctx, property, new HashMap<Object, String>(), delegate);
    }


    public GroupByTableRenderer(Context ctx, final PropertyInfo property, final Map<Object, String> descriptionMap, final TableRenderer delegate)
    {
        super(delegate);
        property_ = property;
        descriptionMap_ = descriptionMap;
        
        int numColumns = 0;
        {
            Class beanClass = property.getBeanClass();
            Class twcClass = XBeans.getClass(ctx, beanClass, TableWebControl.class);
            
            Method[] methods = twcClass.getMethods();
            for (Method method : methods)
            {
                String name = method.getName();
                if (name.startsWith("get") && name.endsWith("WebControl"))
                {
                    numColumns++;
                }
            }
        }
        
        colspan_ = numColumns;
    }

    @Override
    public TableRenderer TR(final Context ctx, final PrintWriter out, final Object obj, final int row)
    {
        Class beanType = property_.getBeanClass();
        if (beanType.isInstance(obj))
        {
            Object value = property_.get(obj);
            if (value != null)
            {
                if (!value.equals(lastValue_))
                {
                    lastValue_ = value;

                    String header = descriptionMap_.get(value);
                    if (header == null)
                    {
                        header = value.toString();
                        descriptionMap_.put(value, header);
                    }

                    
                    out.print("<tr class=\"row-group\">");
                    super.TD(ctx, out, "align=\"left\" colspan=\"" + colspan_ + "\"");
                    out.print("<font size=\"+1\">");

                    if (header != null && header.trim().length() > 0)
                    {
                        out.print(header);   
                    }
                    else
                    {
                        out.print("---");
                    }

                    out.print("</font>");
                    super.TDEnd(ctx, out);
                    out.println("</tr>");
                }
            }
        }

        return super.TR(ctx, out, obj, row);
    }
}
