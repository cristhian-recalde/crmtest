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
package com.trilogy.app.crm.web.control;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This class is customzed to apply SAT values into subscriber profile.
 *
 * @author joe.chen@redknee.com
 */
public class NotifyAfterFromWebWebControl extends ProxyWebControl
{
    protected transient PropertyChangeSupport changeSupport_ = new PropertyChangeSupport(this);

    public NotifyAfterFromWebWebControl(final PropertyChangeListener listener, final WebControl delegate)
    {
        super(delegate);
        addPropertyChangeListener(listener);
    }

    public void fromWeb(final Context ctx, final Object obj, final ServletRequest p2, final String p3)
    {
        Object old = null;
        if (obj instanceof XCloneable)
        {
            try
            {
                old = ((XCloneable) obj).clone();
            }
            catch (CloneNotSupportedException e)
            {
                LogSupport.major(ctx, this, "Unable to clone bean", e);
            }
        }

        super.fromWeb(ctx, obj, p2, p3);
        firePropertyChange(ctx, old, obj);
    }

    public Object fromWeb(final Context ctx, final ServletRequest p1, final String p2)
    {
        final Object result = super.fromWeb(ctx, p1, p2);
        firePropertyChange(ctx, null, result);
        return result;
    }

    /**
     * We defer the creation of this until it is actually needed in order to save memory.
     */
    public PropertyChangeSupport propertyChangeSupport()
    {
        return changeSupport_;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport().addPropertyChangeListener(listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners()
    {
        return propertyChangeSupport().getPropertyChangeListeners();
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport().removePropertyChangeListener(listener);
    }

    public void firePropertyChange(final Context ctx, final Object oldValue, final Object newValue)
    {
        propertyChangeSupport().firePropertyChange(new PropertyChangeEvent(ctx, "bean", oldValue, newValue));
    }
}