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
package com.trilogy.app.crm.subscriber;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.GroupScreeningTemplate;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;

 
public class GroupScreeningTemplateIdentifierSettingHome extends HomeProxy
{
 
    
    private static final long serialVersionUID = 1L;

    public GroupScreeningTemplateIdentifierSettingHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    @Override
    public Object create(final Context ctx, final Object bean) throws HomeException
    {
        final GroupScreeningTemplate  groupScreeningTemplate = (GroupScreeningTemplate ) bean;

        // Throws HomeException.
        final long identifier = getNextIdentifier(ctx);

        groupScreeningTemplate.setIdentifier(identifier);

        return super.create(ctx, groupScreeningTemplate);
    }

    /**
     * Gets the next available identifier.
     *
     * @return The next available identifier.
     */
    private static synchronized long getNextIdentifier(final Context ctx) throws HomeException
    {
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx,
                IdentifierEnum.GROUP_SCREENING_TEMPLATE_ID, 1, Long.MAX_VALUE);

        return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx,
                IdentifierEnum.GROUP_SCREENING_TEMPLATE_ID, null);
    }

} // class
