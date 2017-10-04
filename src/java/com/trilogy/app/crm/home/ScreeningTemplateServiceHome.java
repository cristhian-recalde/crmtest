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
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.ScreeningTemplate;
import com.trilogy.app.crm.bean.ScreeningTemplateTransientHome;
import com.trilogy.app.crm.bean.ScreeningTemplateXInfo;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.urcs.ScreeningTemplatesServiceClient;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.Value;
import com.trilogy.framework.xhome.elang.Wildcard;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * This class implements a subset of Home functionality for screening templates
 * and delegates to the Screening Template corba client.
 *
 * @author Marcio Marques
 * @since 8.5
 */
public class ScreeningTemplateServiceHome extends ScreeningTemplateTransientHome
{
    public ScreeningTemplateServiceHome(Context ctx)
    {
        super(ctx);
    }
    
    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#select(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public Collection select(Context ctx, Object where) throws HomeException, UnsupportedOperationException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "selectAll " + where, null).log(ctx);
        }
        
        final Integer spid = (Integer) getPropertyArgument(where, ScreeningTemplateXInfo.SPID, Integer.class);
        
        return select(ctx, spid, XBeans.getInstanceOf(ctx, where, Predicate.class));
    }

    public Visitor forEach(Context ctx, Visitor visitor, Object where)
    throws HomeException
    {
        Collection retrievedObjects = select(ctx, where);
    
        for ( Iterator i = retrievedObjects.iterator() ; i.hasNext() ; )
        {
           try
           {
              Object bean = i.next();
              visitor.visit(ctx, bean);
           }
           catch (AbortVisitException e)
           {
              break;
           }
           catch (AgentException e)
           {
              // This is so that we preserve the type of the original HomeException
              if ( e.getCause() != null && e.getCause() instanceof HomeException )
              {
                 throw (HomeException) e.getCause();
              }
    
              throw new HomeException(e);
           }
        }
    
        return visitor;
     }   
    
    private Object getPropertyArgument(final Object x, final PropertyInfo propertyInfo, final Class<? extends Object> expectedClass)
    {
        if (x instanceof EQ)
        {
            final EQ eq = (EQ) x;
            final String name = ((PropertyInfo) eq.getArg1()).getName();
            if (name.equals(propertyInfo.getName()))
            {
                return eq.getArg2();
            }
            else if (expectedClass.isInstance(eq.getArg1()))
            {
                return  eq.getArg1();
            }
        }

        if (x instanceof Context)
        {
            new MinorLogMsg(this,"Unexpected context" +x,null).log(getContext());
            return null;
        }

        if (x instanceof Wildcard)
        {
            final Wildcard wildcard = (Wildcard) x;
            final String name = ((PropertyInfo) wildcard.getArg1()).getName();
            if (name.equals(propertyInfo.getName()))
            {
                return wildcard.getArg2();
            }
            else if (expectedClass.isInstance(wildcard.getArg1()))
            {
                return  wildcard.getArg1();
            }
        }

        if (x instanceof Value)
        {
            final Value v = (Value) x;
            final Object result = getPropertyArgument(((Value) x).getArg1(), propertyInfo, expectedClass);

            return result;
        }


        if (x instanceof Or)
        {
            final Or xc = (Or) x;
            for (Object o : xc.getList())
            {
                final Object result = getPropertyArgument(o, propertyInfo, expectedClass);

                if (result != null)
                {
                    return result;
                }
            }
            
            return null;
        }

        if (x instanceof And)
        {
            final And xc = (And) x;
            for (Object o : xc.getList())
            {
                final Object result = getPropertyArgument(o, propertyInfo, expectedClass);

                if (result != null)
                {
                    return result;
                }
            }
            
            return null;
        }

        return null;
    }    
    

    @Override
    public Object find(Context ctx, Object key) throws HomeException
    {
        Integer spid = null;
        Object bean = ctx.get(AbstractWebControl.BEAN);
        if (bean instanceof SpidAware)
        {
            spid = Integer.valueOf(((SpidAware) bean).getSpid());
        }
        else if (bean instanceof Extension)
        {
            Object parentBean = ExtensionSupportHelper.get(ctx).getParentBean(ctx);
            if (parentBean instanceof SpidAware)
            {
                spid = Integer.valueOf(((SpidAware) parentBean).getSpid());
            }
        }
        
        if (spid == null)
        {
            throw new HomeException("Operation not supported");
        }
        else
        {
            if (isKey(ctx, key))
            {
                Collection beans = select(ctx, spid, new EQ(ScreeningTemplateXInfo.IDENTIFIER, key));
                if (beans.size()>0)
                {
                    return beans.iterator().next();
                }
            }
        }
        return null;
    }

    private Collection<ScreeningTemplate> select(Context ctx, Integer spid, Object where) throws HomeException
    {
        Collection<ScreeningTemplate> result = new ArrayList<ScreeningTemplate>();
        if (spid == null)
        {
            throw new HomeException("Operation not supported. Spid should be informed.");
        }
        
        ScreeningTemplatesServiceClient client = (ScreeningTemplatesServiceClient) ctx.get(ScreeningTemplatesServiceClient.class);

        try
        {
            Collection<ScreeningTemplate> returnedObjects = client.retrieveScreeningTemplates(ctx, spid);
            
            if (where instanceof Predicate)
            {
                Predicate predicate = (Predicate) where;
                for (ScreeningTemplate sc : returnedObjects)
                {
                    if (predicate.f(ctx, sc))
                    {
                        result.add(sc);
                    }
                }
            }
            else
            {
                result = returnedObjects;
            }
        }
        catch (RemoteServiceException e)
        {
            throw new HomeException("Unable to retrieve screening templates: " + e.getMessage());
        }
        
        return result;
    }
}

