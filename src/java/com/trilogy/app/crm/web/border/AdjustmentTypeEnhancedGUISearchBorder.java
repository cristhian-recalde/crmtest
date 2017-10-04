/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUI;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUISearch;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUISearchWebControl;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUISearchXInfo;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIXInfo;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

/**
 * Search border for adjustment type enhanced GUI screen.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypeEnhancedGUISearchBorder extends SearchBorder
{

    /**
     * Create search border.
     * 
     * @param ctx
     *            Context.
     */
    public AdjustmentTypeEnhancedGUISearchBorder(Context ctx)
    {
        super(ctx, AdjustmentTypeEnhancedGUI.class,
                new AdjustmentTypeEnhancedGUISearchWebControl());
        // prefix matching for name
        addAgent(new WildcardSelectSearchAgent(AdjustmentTypeEnhancedGUIXInfo.USER_GROUP,
                AdjustmentTypeEnhancedGUISearchXInfo.USER_GROUP, true));
    }

}
