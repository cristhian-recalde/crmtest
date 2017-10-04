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
package com.trilogy.app.crm.move.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountsDiscount;
import com.trilogy.app.crm.bean.AccountsDiscountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Concrete class providing customization of move request related code.  Note that this move
 * request is used for account conversion (i.e. moving from prepaid to postpaid or visa versa).
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ConvertAccountGroupTypeRequest extends AbstractConvertAccountGroupTypeRequest
{
    public static final String CONVERT_SUCCESS_MSG_KEY = ConvertAccountGroupTypeRequest.class.getSimpleName() + ".success";

    /**
     * @{inheritDoc}
     */
    @Override
    public String getSuccessMessage(Context ctx)
    {
        String msg = null;
        
        MessageMgr mmgr = new MessageMgr(ctx, this);
        
        String newType = null;
        String oldType = null;
        
        switch (getGroupType().getIndex())
        {
            case GroupTypeEnum.SUBSCRIBER_INDEX:
                newType = "subscriber";
                break;
            case GroupTypeEnum.GROUP_POOLED_INDEX:
                newType = "group pooled";
                break;
            case GroupTypeEnum.GROUP_INDEX:
                newType = "group";
                break;
        }
        
        switch (getExistingGroupType().getIndex())
        {
            case GroupTypeEnum.SUBSCRIBER_INDEX:
                oldType = "subscriber";
                break;
            case GroupTypeEnum.GROUP_POOLED_INDEX:
                oldType = "group pooled";
                break;
            case GroupTypeEnum.GROUP_INDEX:
                oldType = "group";
                break;
        }
        
        
        final Link link = new Link(ctx);
        link.remove("cmd");
        link.add("cmd","SubMenuAccountEdit");
        link.remove("key");
        link.add("key",this.getNewBAN());

        if(!this.getRetainOriginalAccount())
        {
            msg = mmgr.get(CONVERT_SUCCESS_MSG_KEY, 
                            "New {0} account created with BAN: <a href=\"{1}\">{2}</a>.  Old {3} account with BAN {4} has been deactivated.",
                            new String[] {
                                    newType,
                                    link.write(), 
                                    this.getNewBAN(),
                                    oldType,
                                    this.getExistingBAN()
                                });
        }
        else
        {
            
            msg = mmgr.get(CONVERT_SUCCESS_MSG_KEY, 
                    "New {0} account created with BAN: <a href=\"{1}\">{2}</a>.  Old {3} account with BAN {4} has been converted " +
                    "into child account. Data migration may take some time.  Please return to the Account at a later " +
                    "time and refresh the data",
                    new String[] {
                            newType,
                            link.write(), 
                            this.getNewBAN(),
                            oldType,
                            this.getExistingBAN()
                        });
        }
        
        	Account newAccount = this.getNewAccount(ctx);
            Account oldAccount = this.getOldAccount(ctx);
            
            And and = new And();
            and.add(new EQ(AccountsDiscountXInfo.BAN, oldAccount.getBAN()));
            and.add(new EQ(AccountsDiscountXInfo.SPID, oldAccount.getSpid()));
            Collection<AccountsDiscount> coll = null;
    		try {
    			
    			CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, oldAccount.getSpid());
    			Home home = HomeSupportHelper.get(ctx).getHome(ctx, AccountsDiscount.class);
    			coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccountsDiscount.class, and);
    			
    			Account parentAccount = newAccount.getResponsibleParentAccount(ctx);
            
    	        for (AccountsDiscount accountsDiscount : coll)
    	        {
    	        	AccountsDiscount ad = new AccountsDiscount();
    	        	if (parentAccount != null && crmSpid.getApplyServiceLevelDiscountToGroupAccount())
    	        	{
    	        		ad.setBAN(parentAccount.getBAN());
    	        		HomeSupportHelper.get(ctx).removeBean(ctx, accountsDiscount);
    	        		ad.setDiscountClass(accountsDiscount.getDiscountClass());
        				ad.setSpid(newAccount.getSpid());
        				home.create(ctx, ad);
    	        	}
    	        }
    		}
            
            catch (HomeException e) {
            	new MajorLogMsg(Account.class, "Unable to create/remove discount class for ban " + newAccount.getBAN(), e).log(ctx);
    		}
        return msg;
    }


    @Override
    public PropertyInfo getExtensionHolderProperty()
    {
        return ConvertAccountGroupTypeRequestXInfo.ACCOUNT_EXTENSIONS;
    }

    @Override
    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders =
            (Collection<ExtensionHolder>) getExtensionHolderProperty()
                .get(this);
        return ExtensionSupportHelper.get(ContextLocator.locate()).unwrapExtensions(
            holders);
    }
    
    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        Set<Class<AccountExtension>> extClasses = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(ctx,
                AccountExtension.class);
        Collection<Class> desiredExtTypes = new ArrayList<Class>();
        for (Class<AccountExtension> ext : extClasses)
        {
            if (PoolExtension.class.isAssignableFrom(ext) && GroupTypeEnum.GROUP_POOLED.equals(getGroupType()))
            {
                desiredExtTypes.add(ext);
            }
        }
        return desiredExtTypes;
    }
    
    public static final String CONVERT_ACCOUNT_GROUP_TYPE = "Move.ConvertAccountGroupType";
}
