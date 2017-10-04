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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This WebControl that feeds default value of the controlled property from an indicated
 * spid Property
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class DefaultSpidPropertyWebControl extends ProxyWebControl
{

    public DefaultSpidPropertyWebControl(final PropertyInfo spidProperty, final WebControl delegate)
    {
        super(delegate);
        spidPropertyInfo_ = spidProperty;
        if (null == spidPropertyInfo_)
        {
            intializationError_ = "Incorrect Intialization of Web-Control[" + this.getClass().getName()
                    + "]. Required Property could for entity [" + CRMSpid.class.getSimpleName() + "] not intialized";
        }
        else if (!(spidPropertyInfo_.getXInfo() instanceof CRMSpidXInfo))
        {
            intializationError_ = "Incorrect Intialization of Web-Control[" + this.getClass().getName()
                    + "]. Property [" + spidPropertyInfo_.getName() + " could not be determined for entity ["
                    + CRMSpid.class.getSimpleName() + "]";
        }
        else
        {
            intializationError_ = null;
        }
        // we record initialization error in a unconventional fashion because if we reject
        // the the construction with IllegalArgumentException (politically correct :); the
        // ParentWebControl class that includes this web-control as a staic final variable
        // will fail to load even and ClassNotFound Exception would confuse while
        // debugging. It is better we let the error message be thrown on GUI and in logs
        // each time an incorrectly instantiated web-controlled is used since we can't
        // prevent it at compile time.
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String propertyName, Object obj)
    {
        // only process the control if it was initialized properly
        // other wise just log an error and pass on to the delegate.
        if (null == intializationError_)
        {
            final int mode = ctx.getInt("MODE", DISPLAY_MODE);
            // set the default based on the SPID configuration if creating a new bean
            // if there is any exception or discrepancy, just delegate blindly
            // on all other operations the value of the property is expected to be already
            // set
            // hence default value should not be fed.
            if (mode == CREATE_MODE)
            {
                final PropertyInfo beanPropertyInfo = (PropertyInfo) ctx.get(AbstractWebControl.PROPERTY);
                final AbstractBean bean = (AbstractBean) ctx.get(AbstractWebControl.BEAN);
                if (beanPropertyInfo != null && bean != null)
                {
                    if (beanPropertyInfo.getType().isAssignableFrom(spidPropertyInfo_.getType()))
                    {
                        if (bean instanceof SpidAware)
                        {
                            try
                            {
                                final int spid = ((SpidAware) bean).getSpid();
                                final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
                                if (null != crmSpid)
                                {
                                    // it is alright to lose the passed value as it only
                                    // happens when the bean is created first.
                                    obj = spidPropertyInfo_.f(ctx, crmSpid);
                                }
                                else
                                {
                                    final String message = "Unable to find SPID-Entity for SPID [" + spid
                                            + "]. Default value for Property [" + beanPropertyInfo.getName()
                                            + "] not set.";
                                    handleErrorMessage(ctx, new IllegalPropertyArgumentException(beanPropertyInfo,
                                            message));
                                }
                            }
                            catch (HomeException e)
                            {
                                final String message = "Could not find SPID Entity due to Error [" + e.getMessage()
                                        + "]. Default value for  property [" + beanPropertyInfo.getName()
                                        + "] not set.";
                                new DebugLogMsg(this, message, e).log(ctx);
                                handleErrorMessage(ctx, new IllegalPropertyArgumentException(beanPropertyInfo, message));
                            }
                        }
                        else
                        {
                            final String message = "Entity [" + bean.getClass().getSimpleName()
                                    + "] is not SPID-Aware. Default value for Property [" + beanPropertyInfo.getName()
                                    + "] not set.";
                            handleErrorMessage(ctx, new IllegalPropertyArgumentException(beanPropertyInfo, message));
                        }
                    }
                    else
                    {
                        final String message = "Entity-Property [" + beanPropertyInfo.getName() + "] of Type ["
                                + beanPropertyInfo.getType() + "] is not assignable from SPID Property ["
                                + spidPropertyInfo_.getName() + "] which is of Type [" + spidPropertyInfo_.getType()
                                + "]; hence default value not set.";
                        handleErrorMessage(ctx, new IllegalPropertyArgumentException(beanPropertyInfo, message));
                    }
                }
                else
                {
                    String message = "Entity-Property [" + beanPropertyInfo
                            + "] could not be determined . Default value for  Property [" + beanPropertyInfo.getName()
                            + "] not set.";
                    handleErrorMessage(ctx, new IllegalPropertyArgumentException(beanPropertyInfo, message));
                }
            }
        }
        else
        {
            handleErrorMessage(ctx, new IllegalStateException(intializationError_));
        }
        super.toWeb(ctx, out, propertyName, obj);
    }


    /**
     * Log the error message in minor-log and to exception-listener and print the trance
     * in debug
     * 
     * @param ctx
     * @param error
     */
    private void handleErrorMessage(final Context ctx, final Throwable error)
    {
        ExceptionListener exceptionListener = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != exceptionListener)
        {
            exceptionListener.thrown(error);
        }
        new MinorLogMsg(this, error.getMessage(), null);
        new DebugLogMsg(this, "", error);
    }

    private final PropertyInfo spidPropertyInfo_;
    private final String intializationError_;
}
