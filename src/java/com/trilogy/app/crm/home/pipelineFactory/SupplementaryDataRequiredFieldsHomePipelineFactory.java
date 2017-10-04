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
import java.rmi.RemoteException;
import java.util.Comparator;

import com.trilogy.app.crm.bean.SupplementaryDataReqFields;
import com.trilogy.app.crm.bean.SupplementaryDataReqFieldsHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.SortingHome;

/**
 * 
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class SupplementaryDataRequiredFieldsHomePipelineFactory extends HomeProxy 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * Singleton instance.
     */
    private static SupplementaryDataRequiredFieldsHomePipelineFactory instance_;
    
    /**
     * Create a new instance of <code>SupplementaryDataRequiredFieldsHomePipelineFactory</code>.
     */
    protected SupplementaryDataRequiredFieldsHomePipelineFactory()
    {
        // empty
    }

    /**
     * Returns an instance of <code>SupplementaryDataRequiredFieldsHomePipelineFactory</code>.
     *
     * @return An instance of <code>SupplementaryDataRequiredFieldsHomePipelineFactory</code>.
     */
    public static SupplementaryDataRequiredFieldsHomePipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new SupplementaryDataRequiredFieldsHomePipelineFactory();
        }
        return instance_;
    }

    /**
     * @{inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) 
        throws RemoteException, HomeException, IOException, AgentException
    {
        Home home = CoreSupport.bindHome(ctx, SupplementaryDataReqFields.class, true);
        home = new NotifyingHome(home);
        home = new SortingHome(ctx, home, new Comparator() 
        {

            @Override
            public int compare(Object o1, Object o2)
            {
                SupplementaryDataReqFields obj1 = (SupplementaryDataReqFields) o1;
                SupplementaryDataReqFields obj2 = (SupplementaryDataReqFields) o2;
                if (o2==null)
                {
                    return -1;
                }
                else if (o1 == null)
                {
                    return 1;
                }
                else if (obj1.getSpid()<obj2.getSpid())
                {
                    return -1;
                }
                else if (obj1.getSpid()>obj2.getSpid())
                {
                    return 1;
                }
                else if (obj1.getEntity()<obj2.getEntity())
                {
                    return -1;
                }
                else if (obj1.getEntity()>obj2.getEntity())
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
            
        });
        home = new RMIClusteredHome(ctx, SupplementaryDataReqFieldsHome.class.getName(), home);
		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home, SupplementaryDataReqFields.class);

        return home;
    }
}
