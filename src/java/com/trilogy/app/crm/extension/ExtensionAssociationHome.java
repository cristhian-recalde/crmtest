/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtension;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * 
 *
 * @author Marcio Marques
 * @since 9_1_2
 */
public class ExtensionAssociationHome<T extends Extension, Y extends AbstractBean> extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected final Class<T> extType_;
    protected final PropertyInfo extKeyProp_;
    protected final PropertyInfo associationKeyProp_;
    protected final Class extParentClass_;
    
    
    /**
     * 
     */
    public ExtensionAssociationHome(final Context ctx, Class<T> preferredType, PropertyInfo extensionForeignKeyProp,
            PropertyInfo associationKeyProp, Class extParentClass, final Home delegate)
    {
        super(ctx, delegate);
        extType_ = preferredType;
        extKeyProp_ = extensionForeignKeyProp;
        associationKeyProp_ = associationKeyProp;
        extParentClass_ = extParentClass;
    }


    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        Object result = null;
        final List<T> extensions = getExtensionsForCurrentInstance(ctx, obj);
        associateAllExtensions(ctx, (Y) obj, extensions);
        boolean success = false;
        try
        {
            result = super.create(ctx, obj);
            success = true;
        }
        finally
        {
            executePostCreationForAllExtensions(ctx, (Y) obj, extensions, success);
        }
        return result;
    }


    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        Object result = null;
        final List<T> extensions = getExtensionsForCurrentInstance(ctx, obj);
        
        updateAllExtensions(ctx, (Y) obj, extensions);

        boolean success = false;
        try
        {
            result = super.store(ctx, obj);
            success = true;
        }
        finally
        {
            executePostUpdateForAllExtensions(ctx, (Y) obj, extensions, success);
        }
        
        return result;
    }


    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final List<T> extensions = getExtensionsForCurrentInstance(ctx, obj);
        
        dissociateAllExtensions(ctx, (Y) obj, extensions);

        boolean success = false;
        try
        {
            super.remove(ctx, obj);
            success = true;
        }
        finally
        {
            executePostRemovalForAllExtensions(ctx, (Y) obj, extensions, success);
        }
    }


    private List<T> getExtensionsForCurrentInstance(final Context ctx, final Object obj) throws HomeException
    {
        ExtensionAware extAware = (ExtensionAware) HomeSupportHelper.get(ctx).findBean(ctx, extParentClass_,
                associationKeyProp_.get((Y) obj));
        Collection<Class> eTypes = extAware.getExtensionTypes();
        Class[] extTypeArray = eTypes.toArray(new Class[0]);
        if (extTypeArray != null && extTypeArray.length > 0)
        {
            final List<T> extensions = ExtensionSupportHelper.get(ctx).getAllExtensions(ctx, extType_,
                    new EQ(extKeyProp_, associationKeyProp_.get((Y) obj)), extTypeArray);
            return extensions;
        }
        return new ArrayList<T>();
    }


    private void associateAllExtensions(Context ctx, Y associatedBean, List<T> extensions) throws HomeException
    {
        if (extensions.size() > 0)
        {
            final Context sCtx = ctx.createSubContext();
            for (T extension : extensions)
            {
                if (extension instanceof AssociableExtension)
                {
                    if (extension instanceof ContextAware)
                    {
                        ((ContextAware) extension).setContext(ctx);
                    }
                    try
                    {
                        ((AssociableExtension) extension).associate(sCtx, associatedBean);
                    }
                    catch (ExtensionAssociationException e)
                    {
                        Exception he = getException(ctx, extension, associatedBean, e, "associating");
                        
                        if (e.wasExtensionAssociated())
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                        }
                        else
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                            throw new HomeException(he);
                        }
                    }
                }
            }
        }    
    }


    private void dissociateAllExtensions(Context ctx, Y associatedBean, List<T> extensions) throws HomeException
    {
        if (extensions.size() > 0)
        {
            final Context sCtx = ctx.createSubContext();
            for (T extension : extensions)
            {
                if (extension instanceof AssociableExtension)
                {
                    if (extension instanceof ContextAware)
                    {
                        ((ContextAware) extension).setContext(ctx);
                    }
                    try
                    {
                        ((AssociableExtension) extension).dissociate(sCtx, associatedBean);
                    }
                    catch (ExtensionAssociationException e)
                    {
                        Exception he = getException(ctx, extension, associatedBean, e, "dissociating");
                        
                        if (e.wasExtensionAssociated())
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                        }
                        else
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                            throw new HomeException(he);
                        }
                    }
                }
            }
        }
    }

    private void updateAllExtensions(Context ctx, Y associatedBean, List<T> extensions) throws HomeException
    {
        if (extensions.size() > 0)
        {
            final Context sCtx = ctx.createSubContext();
            for (T extension : extensions)
            {
                if (extension instanceof AssociableExtension)
                {
                    if (extension instanceof ContextAware)
                    {
                        ((ContextAware) extension).setContext(ctx);
                    }
                    try
                    {
                        ((AssociableExtension) extension).updateAssociation(sCtx, associatedBean);
                    }
                    catch (ExtensionAssociationException e)
                    {
                        Exception he = getException(ctx, extension, associatedBean, e, "updating");
                        
                        if (e.wasExtensionAssociated())
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                        }
                        else
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                            throw new HomeException(he);
                        }
                    }
                }
            }
        }    
     }
   
    
    private void executePostCreationForAllExtensions(Context ctx, Y associatedBean, List<T> extensions, boolean success) throws HomeException
    {
        if (extensions.size() > 0)
        {
            final Context sCtx = ctx.createSubContext();
            for (T extension : extensions)
            {
                if (extension instanceof ExtendedAssociableExtension)
                {
                    if (extension instanceof ContextAware)
                    {
                        ((ContextAware) extension).setContext(ctx);
                    }
                    try
                    {
                        ((ExtendedAssociableExtension) extension).postExternalBeanCreation(sCtx, associatedBean, success);
                    }
                    catch (ExtensionAssociationException e)
                    {
                        Exception he = getException(ctx, extension, associatedBean, e, "associating");
                        
                        if (e.wasExtensionAssociated())
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                        }
                        else
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                            throw new HomeException(he);
                        }
                    }
                }
            }
        }
    }
    
    private void executePostUpdateForAllExtensions(Context ctx, Y associatedBean, List<T> extensions, boolean success) throws HomeException
    {
        if (extensions.size() > 0)
        {
            final Context sCtx = ctx.createSubContext();
            for (T extension : extensions)
            {
                if (extension instanceof ExtendedAssociableExtension)
                {
                    if (extension instanceof ContextAware)
                    {
                        ((ContextAware) extension).setContext(ctx);
                    }
                    try
                    {
                        ((ExtendedAssociableExtension) extension).postExternalBeanUpdate(sCtx, associatedBean, success);
                    }
                    catch (ExtensionAssociationException e)
                    {
                        Exception he = getException(ctx, extension, associatedBean, e, "updating");
                        
                        if (e.wasExtensionAssociated())
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                        }
                        else
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                            throw new HomeException(he);
                        }
                    }
                }
            }
        }
    }
    
    private void executePostRemovalForAllExtensions(Context ctx, Y associatedBean, List<T> extensions, boolean success) throws HomeException
    {
        if (extensions.size() > 0)
        {
            final Context sCtx = ctx.createSubContext();
            for (T extension : extensions)
            {
                if (extension instanceof ExtendedAssociableExtension)
                {
                    if (extension instanceof ContextAware)
                    {
                        ((ContextAware) extension).setContext(ctx);
                    }
                    try
                    {
                        ((ExtendedAssociableExtension) extension).postExternalBeanRemoval(sCtx, associatedBean, success);
                    }
                    catch (ExtensionAssociationException e)
                    {
                        Exception he = getException(ctx, extension, associatedBean, e, "dissociating");
                        
                        if (e.wasExtensionAssociated())
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                        }
                        else
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, he);
                            throw new HomeException(he);
                        }
                    }
                }
            }
        }
    }
    
    private Exception getException(Context ctx, T extension, Y associatedBean, ExtensionAssociationException e, String action)
    {
    
        if (associatedBean instanceof ServiceBase)
        {
           return new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getProvisionErrorMessage(ctx,
                    e.getExternalApp(), e.getResultCode(), (ServiceBase) associatedBean), e.getResultCode(), e.getExternalApp(), e);
        }
        else if (extension instanceof AuxiliaryServiceExtension)
        {
            AuxiliaryServiceExtension auxSvcExtension = (AuxiliaryServiceExtension) extension;
            return new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getProvisionErrorMessage(ctx,
                    e.getExternalApp(), e.getResultCode(), auxSvcExtension.getAuxiliaryService(ctx)),
                    e.getResultCode(), e.getExternalApp(), e);
        }
        else
        {
           return new HomeException((e.wasExtensionAssociated()?"Partial failure":"Failure") + " " + action + " "
                    + extension.getName(ctx) + " extension with bean " + associatedBean + ": "
                    + e.getMessage(), e);
        }
    }

    
}
