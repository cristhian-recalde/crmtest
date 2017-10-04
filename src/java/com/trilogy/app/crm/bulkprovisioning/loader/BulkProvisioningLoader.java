package com.trilogy.app.crm.bulkprovisioning.loader;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


public class BulkProvisioningLoader extends AbstractBulkProvisioningLoader implements ContextAware
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public BulkProvisioningLoader()
    {
    }


    public void load(Context mainCtx, Object object, Object source) throws HomeInternalException, HomeException
    {
        Context ctx = mainCtx.createSubContext();
        if (bulkInterface_ != null)
        {
            bulkInterface_.provision(ctx, object, source);
        }
    }


    public Object newInstance(Context context, Object source)
    {
        try
        {
            return XBeans.instantiate(beanClass_, context);
        }
        catch (Exception e)
        {
            LogSupport.major(context, this, "Failed to instantiate instance of class " + beanClass_);
        }
        return null;
    }



    public Context getContext()
    {
        return context_;
    }


    public void setContext(Context context)
    {
        this.context_ = context;
    }


    public void init()
    {            
        new InfoLogMsg(this, "Initializing Bulkprovisioing for " + this.getBulkProvisioningInterface()
                + " with parameters => " + this.getParameters(), null).log(context_);
        Class bulkProvisioningInterfaceClass = Object.class;
        try
        {
            beanClass_ = Class.forName(super.getBeanClassName());
            bulkProvisioningInterfaceClass = Class.forName(this.getBulkProvisioningInterface());
        }
        catch (ClassNotFoundException e)
        {
            LogSupport.major(context_, this, e.getMessage(), e);
        }
        
        arrayOfParameters_ = split(this.getParameters(), ',');
        
        
        try
        {
            Object obj = XBeans.instantiate(bulkProvisioningInterfaceClass, context_);
            if (obj != null && obj instanceof BulkProvisioningLoaderI)
            {
                bulkInterface_ = (BulkProvisioningLoaderI) obj;
                bulkInterface_.initialize(getContext(), this.arrayOfParameters_);
            }
        }
        catch (HomeException homeEx)
        {
            LogSupport.major(context_, this, homeEx.getMessage(), homeEx);
            bulkInterface_ = null;
        }
        catch (Exception ex)
        {
            LogSupport.major(context_, this, ex.getMessage(), ex);
            bulkInterface_ = null;
        }
        new InfoLogMsg(this, "Completed initializing Bulkprovisioing for " + this.getBulkProvisioningInterface(),null).log(context_);
        
    }


    private String[] split(String string, char delimiter)
    {
        List<String> list = new ArrayList<String>();
        StringBuilder buf = new StringBuilder();
        string = string.trim();
        int occur = 0;
        for (int index = 0; index < string.length(); index++)
        {
            char ch = string.charAt(index);
            if (ch == '(')
            {
                occur++;
                buf.append(ch);
            }
            else if (ch == ')')
            {
                occur--;
                buf.append(ch);
            }
            else if (ch == delimiter && occur == 0)
            {
                list.add(buf.toString().trim());
                buf = new StringBuilder();
            }
            else
            {
                buf.append(ch);
            }
        }
        if (buf.length() > 0)
            list.add(buf.toString().trim());
        return list.toArray(new String[0]);
    }

    private Context context_;
    private String[] arrayOfParameters_;
    private Class beanClass_;
    private BulkProvisioningLoaderI bulkInterface_ = null;
}
