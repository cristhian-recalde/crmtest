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
package com.trilogy.app.crm.home;

import java.io.IOException;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.bean.CRMGroupXInfo;
import com.trilogy.app.crm.bundle.BundleMgrLimitValidator;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtension;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtensionXInfo;
import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.TimestampHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.home.auth.AuthenticatedHome;
import com.trilogy.framework.xhome.relationship.NoRelationshipRemoveHome;

/**
 * Provides a class from which to create the pipeline of Home decorators that
 * process a CRMGroup travelling between the application and the given
 * delegate.
 *
 * @author arturo.medina@redknee.com
 */
public class CRMGroupHomePipelineFactory implements PipelineFactory
{
    public Home createPipeline(final Context ctx, final Context serverCtx)
        throws HomeException, IOException, AgentException
    {
        // [CW] CRMGroup is clustered by all

        // use CompoundValidator in case we have more validation
        final CompoundValidator compoundValidator = new CompoundValidator();
        compoundValidator.add(new BundleMgrLimitValidator());

        Home groupHome = CoreSupport.bindHome(ctx, CRMGroup.class);
        groupHome = new PMHome(ctx, CRMGroupHome.class.getName(), groupHome);
        groupHome = new RMIClusteredHome(ctx, CRMGroupHome.class.getName(), groupHome);

        groupHome = new ExtensionHandlingHome<UserGroupExtension>(
                ctx, 
                UserGroupExtension.class, 
                UserGroupExtensionXInfo.GROUP_NAME, 
                groupHome);
        groupHome = new AdapterHome(groupHome, 
                new ExtensionForeignKeyAdapter(
                        UserGroupExtensionXInfo.GROUP_NAME));
        
        groupHome = new NoRelationshipRemoveHome(ctx, CRMGroupXInfo.UsersRelationship,
                "Cannot delete User Group containing Users. Please remove users first.",
                groupHome);

        groupHome = new ValidatingHome(compoundValidator, groupHome);
        groupHome = new NotifyingHome(groupHome);
        groupHome = new TimestampHome(ctx, groupHome);
        groupHome = new AuthenticatedHome(groupHome);

        ctx.put(CRMGroupHome.class, groupHome);

        return groupHome;
    }
}
