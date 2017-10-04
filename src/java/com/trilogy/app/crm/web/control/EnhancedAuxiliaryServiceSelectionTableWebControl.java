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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;

import com.trilogy.app.crm.bean.AuxiliaryServiceSelectionTableWebControl;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelectionXInfo;
import com.trilogy.app.crm.web.renderer.GroupByTableRenderer;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class EnhancedAuxiliaryServiceSelectionTableWebControl extends AuxiliaryServiceSelectionTableWebControl
{
    @Override
    public TableRenderer tableRenderer(final Context ctx)
    {
        final TableRenderer renderer = super.tableRenderer(ctx);
        return new GroupByTableRenderer(ctx, AuxiliaryServiceSelectionXInfo.TYPE, renderer);
    }
}
