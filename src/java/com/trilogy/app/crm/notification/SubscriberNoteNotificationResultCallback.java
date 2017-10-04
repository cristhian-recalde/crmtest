/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.notification;

import java.io.OutputStream;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.io.OutputStreamFactory;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.notification.template.NotificationTemplateHolderDescriptionFunction;
import com.trilogy.app.crm.notification.template.StateChangeNotificationTemplate;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;

/**
 * A notification result callback object which logs results.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class SubscriberNoteNotificationResultCallback extends AbstractSubscriberNoteNotificationResultCallback
{
    private String constructMessage(Context ctx)
    {
        StringBuilder sb = new StringBuilder();
        NotificationTypeEnum type = NotificationTypeEnum.get((short)getNotificationTypeIndex());
        if (type != null)
        {
            sb.append(type.getDescription(ctx));
        }
        else
        {
            sb.append("Notification Type Index: " + getNotificationTypeIndex());
        }
        
        NotificationTemplate template = getTemplate();
        if (template instanceof StateChangeNotificationTemplate)
        {
            StateChangeNotificationTemplate stateTmpl = (StateChangeNotificationTemplate) template;
            Enum oldState = SubscriberStateEnum.get((short) stateTmpl.getPreviousState());
            Enum newState = SubscriberStateEnum.get((short) stateTmpl.getNewState());
            
            if (oldState != null || newState != null)
            {
                sb.append(" [");
                if (oldState != null)
                {
                    sb.append("from ");
                    sb.append(oldState.getDescription(ctx));
                    sb.append(" state ");
                }
                if (newState != null)
                {
                    sb.append("to ");
                    sb.append(newState.getDescription(ctx));
                    sb.append(" state");
                }
                sb.append("]");
            }
        }

        sb.append(" of ");
        sb.append(NotificationTemplateHolderDescriptionFunction.instance().f(ctx, template));
        
        sb.append(" to [E-Mail=");
        sb.append(getEmailAddress());
        
        sb.append(",SMS=");
        sb.append(getSmsNumber());
        
        sb.append(",OutputStream");
        OutputStream outputStream = null;
        OutputStreamFactory outFactory = getOutFactory();
        if (outFactory != null)
        {
            outputStream = outFactory.getOutputStream(ctx);
        }
        if (outputStream == null)
        {
            sb.append("Factory");
        }
        sb.append("=");
        sb.append(outputStream != null ? outputStream.getClass().getName() : outFactory);
        
        sb.append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void reportAttempt(Context ctx)
    {
        // NOP
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reportFailure(Context ctx, boolean recoverable, Exception cause)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append(constructMessage(ctx));
            sb.append(" failed (");
            sb.append(recoverable ? "Recoverable" : "Non-Recoverable");
            sb.append(")");
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubId(), sb.toString(),
                    SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.NOTIFICATION);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "ERROR - reportFailure(): " + e.getMessage(),
                    e).log(ctx);
            throw new RuntimeException(e);
        }
        catch (RuntimeException e)
        {
            new MinorLogMsg(this, "ERROR - reportFailure(): " + e.getMessage(),
                    e).log(ctx);
            throw e;
        }
        finally
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "EXIT - reportFailure()", null).log(ctx);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportFailure(Context ctx, boolean recoverable, String msg,
            Exception cause)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append(constructMessage(ctx));

            sb.append(" failed (");
            sb.append(recoverable ? "Recoverable" : "Non-Recoverable");
            sb.append("): ");
            sb.append(msg);
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubId(), sb.toString(),
                    SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.NOTIFICATION);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "ERROR - reportFailure(): " + e.getMessage(),
                    e).log(ctx);
            throw new RuntimeException(e);
        }
        catch (RuntimeException e)
        {
            new MinorLogMsg(this, "ERROR - reportFailure(): " + e.getMessage(),
                    e).log(ctx);
            throw e;
        }
        finally
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "EXIT - reportFailure()", null).log(ctx);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportFailure(Context ctx, boolean recoverable, String msg)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append(constructMessage(ctx));

            sb.append(" failed (");
            sb.append(recoverable ? "Recoverable" : "Non-Recoverable");
            sb.append("): ");
            sb.append(msg);
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubId(), sb.toString(),
                    SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.NOTIFICATION);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "ERROR - reportFailure(): " + e.getMessage(),
                    e).log(ctx);
            throw new RuntimeException(e);
        }
        catch (RuntimeException e)
        {
            new MinorLogMsg(this, "ERROR - reportFailure(): " + e.getMessage(),
                    e).log(ctx);
            throw e;
        }
        finally
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "EXIT - reportFailure()", null).log(ctx);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportSuccess(Context ctx)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append(constructMessage(ctx));
            sb.append(" successfully sent");
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubId(), sb.toString(),
                    SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.NOTIFICATION);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "ERROR - reportSuccess(): " + e.getMessage(),
                    e).log(ctx);
            throw new RuntimeException(e);
        }
        catch (RuntimeException e)
        {
            new MinorLogMsg(this, "ERROR - reportSuccess(): " + e.getMessage(),
                    e).log(ctx);
            throw e;
        }
        finally
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "EXIT - reportSuccess()", null).log(ctx);
            }
        }
    }
}
