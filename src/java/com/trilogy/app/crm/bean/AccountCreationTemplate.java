package com.trilogy.app.crm.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;

public class AccountCreationTemplate extends AbstractAccountCreationTemplate
{
    public Context getContext()
    {
        return context_;
    }

    public void setContext(Context context)
    {
        context_ = context;
    }
    
    /**
     * The operating context.
     */
    protected transient Context context_;
    
    public boolean isPooled(Context ctx)
    {
		return getGroupType().equals(GroupTypeEnum.GROUP_POOLED);
    }
    
    public boolean isIndividual(Context ctx)
    {
		return getGroupType().equals(GroupTypeEnum.SUBSCRIBER);
	}
    
	public AccountCategory getAccountType(Context context)
    {
        return AccountTypeSupportHelper.get(context).getTypedAccountType(context, getType(), true);
    }    
	
    public PropertyInfo getExtensionHolderProperty()
    {
        return AccountCreationTemplateXInfo.ACCOUNT_EXTENSIONS;
    }

    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders =
            (Collection<ExtensionHolder>) getExtensionHolderProperty()
                .get(this);
        return ExtensionSupportHelper.get(getContext()).unwrapExtensions(
            holders);
    }
    
    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        return AccountSupport.getExtensionTypes(ctx, isPooled(ctx));
    }

    
}
