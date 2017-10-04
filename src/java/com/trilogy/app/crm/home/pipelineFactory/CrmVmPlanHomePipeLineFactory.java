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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.CrmVmPlan;
import com.trilogy.app.crm.bean.CrmVmPlanHome;
import com.trilogy.app.crm.bean.VMPlanHome;
import com.trilogy.app.crm.bean.VoiceMailClientTypeEnum;
import com.trilogy.app.crm.bean.VoicemailServiceConfig;
import com.trilogy.app.crm.home.CrmVmPlanRemoveProtectionHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.voicemail.VMPlanAdapter;

public class CrmVmPlanHomePipeLineFactory implements PipelineFactory
{
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException
    {
        final VoicemailServiceConfig config = (VoicemailServiceConfig) ctx.get(VoicemailServiceConfig.class);

        if (config.getClientType().equals(VoiceMailClientTypeEnum.MPATHIX))
        {
            return null;
        }

        Home home = new AuditJournalHome(ctx, StorageSupportHelper.get(ctx).createHome(ctx, CrmVmPlan.class, "CRMVMPLAN"));
        home = new CrmVmPlanRemoveProtectionHome(ctx, home);

        ctx.put(CrmVmPlanHome.class, home);

        ctx.put(VMPlanHome.class, new AdapterHome(home, new VMPlanAdapter()));

        return home;
    }

}
