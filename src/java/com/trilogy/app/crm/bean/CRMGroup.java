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

package com.trilogy.app.crm.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtension;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class CRMGroup extends AbstractCRMGroup
{
    public PropertyInfo getExtensionHolderProperty()
    {
        return CRMGroupXInfo.USER_GROUP_EXTENSIONS;
    }
    
    
    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>)getExtensionHolderProperty().get(this);
        return ExtensionSupportHelper.get(getContext()).unwrapExtensions(holders);
    }
    
    
    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        Set<Class<UserGroupExtension>> extClasses = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(ctx,
                UserGroupExtension.class);
        Collection<Class> desiredClass = new ArrayList<Class>();
        for (Class<UserGroupExtension> ext : extClasses)
        {
            desiredClass.add(ext);
        }
        return desiredClass;
    }

    
    
    /**
     * Lazy loading extensions.
     * {@inheritDoc}
     */
    @Override
    public List getUserGroupExtensions()
    {
        synchronized (this)
        {
            if (super.getUserGroupExtensions() == null)
            {
                final Context ctx = getContext();
                try
                {
                    // To avoid deadlock, use a user group "with extensions loaded" along with extension loading adapter.
                    CRMGroup groupCopy = (CRMGroup) this.clone();
                    groupCopy.setUserGroupExtensions(new ArrayList());
                    
                    groupCopy = (CRMGroup) new ExtensionLoadingAdapter<UserGroupExtension>(UserGroupExtension.class, UserGroupExtensionXInfo.GROUP_NAME).adapt(ctx, groupCopy);
                    
                    this.setUserGroupExtensions(groupCopy.getUserGroupExtensions());
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.");
                    LogSupport.debug(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.", e);
                }
            }
        }
        
        return super.getUserGroupExtensions();
    }
    
    
    /**
     * Adding cloning functionality to clone added fields.
     *
     * @return the clone object
     * @throws CloneNotSupportedException should not be thrown
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CRMGroup clone = (CRMGroup) super.clone();
        return cloneUserGroupExtensionList(clone);
    }

    private CRMGroup cloneUserGroupExtensionList(final CRMGroup clone) throws CloneNotSupportedException
    {
        List groupExtensions = super.getUserGroupExtensions();
        if (groupExtensions != null)
        {
            final List extentionList = new ArrayList(groupExtensions.size());
            clone.setUserGroupExtensions(extentionList);
            for (final Iterator it = groupExtensions.iterator(); it.hasNext();)
            {
                extentionList.add(safeClone((XCloneable) it.next()));
            }
        }
        return clone;
    }


    public Context getContext()
    {
        return ctx_;
    }

    public void setContext(final Context context)
    {
        ctx_ = context;
    }

    private transient Context ctx_ = new ContextSupport();

}
