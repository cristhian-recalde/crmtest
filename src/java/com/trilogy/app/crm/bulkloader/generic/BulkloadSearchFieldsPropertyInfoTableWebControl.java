package com.trilogy.app.crm.bulkloader.generic;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;

public class BulkloadSearchFieldsPropertyInfoTableWebControl extends BulkloadPropertyInfoTableWebControl
{

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(GenericBeanBulkloader.CLASS_NAME_PROPERTY_CTX_KEY, GenericBeanBulkloaderXInfo.SEARCH_CLASS_NAME);
        super.toWeb(subCtx, out, name, obj);
    }
    
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(GenericBeanBulkloader.CLASS_NAME_PROPERTY_CTX_KEY, GenericBeanBulkloaderXInfo.SEARCH_CLASS_NAME);
        super.fromWeb(subCtx, obj, req, name);
    }

}
