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

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * Filters out public or private contract groups based on the isPublic_ value; if true, filter out private contract groups; if false
 * only the associated private group that the MSISDN is provisioned to will be shown
 * 
 * @author ltang
 * 
 */
public class PublicContractGroupAwareKeyWebControl extends AbstractKeyWebControl
{

    private AbstractKeyWebControl delegate_;
    private boolean isPublic_;

    public PublicContractGroupAwareKeyWebControl(AbstractKeyWebControl delegate, boolean isPublic)
    {
        setDelegate(delegate);
        isPublic_ = isPublic;
    }


    public void toWeb(Context ctx, PrintWriter out, String name, final Object obj)
    {
        Context subCtx =  filterPublicContractGroups(ctx);
        
        getDelegate().toWeb(subCtx, out, name, obj);
    }

    /**
     * @return Returns the delegate.
     */
    public WebControl getDelegate()
    {
        return delegate_;
    }


    /**
     * @param delegate
     *            The delegate to set.
     */
    public void setDelegate(AbstractKeyWebControl delegate)
    {
        this.delegate_ = delegate;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl#getDesc(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public String getDesc(Context ctx, Object bean)
    {
        return delegate_.getDesc(ctx, bean);
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl#getIdentitySupport()
     */
    public IdentitySupport getIdentitySupport()
    {
        return delegate_.getIdentitySupport();
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl#getHomeKey()
     */
    public Object getHomeKey()
    {
        return delegate_.getHomeKey();
    }


    /**
     * @param origContext The operating context.
     * @return a subcontext with set filters on the group owner for the contract group home
     */
    public Context filterPublicContractGroups(final Context origContext)
    {
        final MsisdnOwnership ownership = (MsisdnOwnership) origContext.get(AbstractWebControl.BEAN);
        if (ownership == null)
        {
            // No need to filter.
            return origContext;
        }
        
        final Context subContext = origContext.createSubContext();
        final Set<String> allAccountIds = new HashSet<String>();
        String owner = "";
        if (isPublic_)
        {
            // Blank owner name is the default for public groups in CRM.
            // Let ContractGroupServiceHome handle the mapping to configured TFA client config public group owner name.
            owner = "";
            
            allAccountIds.add(owner);
            subContext.put(TransferContractSupport.TRANSFER_CONTRACT_GROUP_ID, allAccountIds);
        }
        else
        {
            if (ownership.getPrivateGroupId() != -1)
            {
                subContext.put(TransferContractSupport.TRANSFER_CONTRACT_PRIVATE_GROUP_ID, ownership.getPrivateGroupId());
            }
        }

        return subContext;
    }
}
