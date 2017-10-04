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

package com.trilogy.app.crm.extension.account;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validatable;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;

/**
 * AccountExtensionsValidator.
 * 
 * @author ankit.nagpal
 */

public class AccountExtensionsValidator implements Validator
{
    private PropertyInfo accountExtensionsProperty_;

    /**
     * Create a new account extensions validator.
     * @param accountExtensionsProperty Account extensions property, used to retrieve the account extensions list.
     * @param systemTypeProperty System type property, used to retrieve the system type for the parent object.
     */
    public AccountExtensionsValidator(PropertyInfo accountExtensionsProperty)
    {
        this.accountExtensionsProperty_ = accountExtensionsProperty;
    }
    
    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException exception = new CompoundIllegalStateException();
        Set<Class> extensionTypeSet = new HashSet<Class>();

        List<AccountExtensionHolder> extensions = (List<AccountExtensionHolder>) accountExtensionsProperty_.get(obj);
        for (AccountExtensionHolder extensionHolder : extensions)
        {
            Extension extension = (Extension) extensionHolder.getExtension();

            try
            {
                if (extension instanceof GroupPricePlanExtension)
                {
                    // Only one instance of the group price plan extension can be used at a time
                    assertSingleInstance(ctx, extension, extensionTypeSet, exception);
                    
                    // Only valid for non-individual accounts (?maybe?)
                    assertNonIndividual(ctx, extension, obj, exception);
                } 
                else if (extension instanceof FriendsAndFamilyExtension)
                {
                    // Only one instance of the friends and family extension can be used at a time
                    assertSingleInstance(ctx, extension, extensionTypeSet, exception);                
                } 
                else if (extension instanceof SubscriberLimitExtension)
                {
                    // Only one instance of the subscriber limit extension can be used at a time
                    assertSingleInstance(ctx, extension, extensionTypeSet, exception);
                }
               

                if( extension instanceof Validatable )
                {
                    // Put the parent bean in the context because the validatable extension might need it.
                    Context sCtx = ctx.createSubContext();
                    sCtx.put(AccountExtensionSupport.ACCOUNT_EXTENSION_PARENT_BEAN_CTX_KEY, obj);
                    
                    // Execute the extension's internal validation
                    ((Validatable)extension).validate(sCtx);
                }
            }
            catch( IllegalArgumentException e )
            {
                // Merge all of the validation exceptions
                exception.thrown(e);
            }
            catch( IllegalStateException e )
            {
                // Merge all of the validation exceptions
                exception.thrown(e);
            }
        }
        exception.throwAll();
    }
    
    private void assertSingleInstance(Context ctx, Extension extension, Set<Class> extensionTypeSet, CompoundIllegalStateException exception)
    {
        if (extensionTypeSet.contains(extension.getClass()))
        {
            exception.thrown(new IllegalPropertyArgumentException(accountExtensionsProperty_, extension.getName(ctx) + " extension is defined more than once."));
        } 
        else
        {
            extensionTypeSet.add(extension.getClass());
        }           
    }

    private void assertNonIndividual(Context ctx, Extension extension, Object parentBean, CompoundIllegalStateException exception)
    {
        AccountCategory type = null;
        boolean isIndividual = false;
        if( parentBean instanceof Account )
        {
            isIndividual = ((Account)parentBean).isIndividual(ctx);
            type = AccountTypeSupportHelper.get(ctx).getTypedAccountType(ctx, ((Account)parentBean).getType());
        }
        else if( parentBean instanceof AccountCreationTemplate )
        {
            isIndividual = ((AccountCreationTemplate)parentBean).isIndividual(ctx);
            type = AccountTypeSupportHelper.get(ctx).getTypedAccountType(ctx, ((AccountCreationTemplate)parentBean).getType());
        }
        if( type != null && isIndividual)
        {
            exception.thrown(new IllegalPropertyArgumentException(accountExtensionsProperty_, extension.getName(ctx) + " not allowed for individual accounts."));
        }
    }
}
