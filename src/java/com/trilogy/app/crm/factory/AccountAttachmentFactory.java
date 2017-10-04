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
package com.trilogy.app.crm.factory;

import com.trilogy.app.crm.account.BANAware;
import com.trilogy.app.crm.account.BANAwareHome;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.framework.xhome.beans.GUID;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


/*
 * author: simar.singh@redknee.com
 */
public class AccountAttachmentFactory implements ContextFactory
{

    public Object create(Context ctx)
    {
        final AccountAttachment attachment = new AccountAttachment();
     // generate unique key for every attachment
        attachment.setAttachmentKey(new GUID().toString());
        BANAwareHome.accountalizeBean(ctx, attachment);
        return attachment;
    }
}