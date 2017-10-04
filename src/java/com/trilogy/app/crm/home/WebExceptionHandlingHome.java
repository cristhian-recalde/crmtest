package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;

public class WebExceptionHandlingHome 
extends HomeProxy
{


    public WebExceptionHandlingHome(Home delegate)
    {
        super(delegate); 
    }

    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException {
        // TODO Auto-generated method stub
        try 
        {
            return super.create(ctx, obj);
        } catch (Exception e)
        {
 
            handleException(ctx, e); 
        }
        return obj;       
    }

    public void remove(Context ctx, Object obj) throws HomeException,
            HomeInternalException {
        // TODO Auto-generated method stub
        try 
        {
            super.remove(ctx, obj);
        }    
        catch (Exception e)
        {
            handleException(ctx,  e); 
        }   
    }


    public Object store(Context ctx, Object obj) throws HomeException,
            HomeInternalException {
        try
        {
            return super.store(ctx, obj);
        }catch (Exception e)
        {
 
            handleException(ctx,  e); 
        }
        return obj; 
    }

    
    private void handleException(Context ctx, Throwable t) throws HomeException
    {
        if (t.getCause() instanceof CompoundIllegalStateException)
        {
            throw (HomeException) t;
        }
        else
        {
            CompoundIllegalStateException cise = new CompoundIllegalStateException();
            cise.thrown(t);
            throw new HomeException(cise.getMessage(), cise);
        }
    }
}
