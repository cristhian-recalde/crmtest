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
package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.auth.bean.GroupSearch;
import com.trilogy.framework.xhome.auth.bean.GroupSearchWebControl;
import com.trilogy.framework.xhome.auth.bean.GroupSearchXInfo;
import com.trilogy.framework.xhome.auth.bean.GroupXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.web.search.PrePostWildcardSelectSearchAgent;

/**
 * @author psperneac
 * @since May 4, 2005 12:37:19 AM
 */
public class GroupSearchBorder extends SearchBorder
{
    public GroupSearchBorder(Context ctx)
    {
        super(ctx,CRMGroup.class,new GroupSearchWebControl());

        // prefix matching for name
        addAgent(new WildcardSelectSearchAgent(GroupXInfo.NAME, GroupSearchXInfo.NAME, true));

        // prefix and postfix matching for description
        addAgent(new PrePostWildcardSelectSearchAgent(GroupXInfo.DESC, GroupSearchXInfo.DESC, true));
    }
}
