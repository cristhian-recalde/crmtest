package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.DiscountClassHome;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.home.DependencyCheckOnRemveHome;
import com.trilogy.app.crm.home.DiscountClassAdjustmentTypeCreationHome;
import com.trilogy.app.crm.home.DiscountClassEnableServiceLevelDiscount;
import com.trilogy.app.crm.home.DiscountClassValidator;
import com.trilogy.app.crm.home.PermissionAwarePermissionSettingHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.account.AccountBillCycleValidator;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;


public class DiscountClassHomePipelineFactory implements PipelineFactory
{
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home home =  CoreSupport.bindHome(ctx, DiscountClass.class); 
        
        home = new NotifyingHome(home);

        home = new DiscountClassAdjustmentTypeCreationHome(home);
        
        home = new DiscountClassEnableServiceLevelDiscount(home);
                
        home = new DependencyCheckOnRemveHome<DiscountClass>(AccountXInfo.DISCOUNT_CLASS,home);
        
        home = new PermissionAwarePermissionSettingHome(home);
        
        home = new AuditJournalHome(ctx, home);
        
        home = new RMIClusteredHome(ctx, DiscountClassHome.class.getName(), home);
        
        home = new SpidAwareHome(ctx, home);

        home = new IdentifierSettingHome(ctx, home, IdentifierEnum.DISCOUNTCLASS_ID, null);        
        
        // validators
        final CompoundValidator validators = new CompoundValidator();
        validators.add(new DiscountClassValidator());
        
        home = new ValidatingHome(validators, home);
        
        IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.DISCOUNTCLASS_ID, home);

        ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, home, DiscountClass.class);
        
        return home;
    }
}
