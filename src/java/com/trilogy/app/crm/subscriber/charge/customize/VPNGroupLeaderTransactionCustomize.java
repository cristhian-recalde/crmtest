package com.trilogy.app.crm.subscriber.charge.customize;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.GroupChargingAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

public class VPNGroupLeaderTransactionCustomize implements TransactionCustomize
{

	public VPNGroupLeaderTransactionCustomize(AuxiliaryService service)
	{
		 this.service = service; 
		 
	}


	
	public Transaction customize(Context ctx, Transaction trans)
	{		
        long groupCharge = GroupChargingAuxSvcExtension.DEFAULT_GROUPCHARGE;
        int groupAdjustmentType = GroupChargingAuxSvcExtension.DEFAULT_GROUPADJUSTMENTTYPE;
        
        GroupChargingAuxSvcExtension groupChargingAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, GroupChargingAuxSvcExtension.class);
        if (groupChargingAuxSvcExtension!=null)
        {
            groupCharge = groupChargingAuxSvcExtension.getGroupCharge();
            groupAdjustmentType = groupChargingAuxSvcExtension.getGroupAdjustmentType();
        }
        else
        {
            LogSupport.minor(ctx, this,
                    "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }

        trans.setAdjustmentType(groupAdjustmentType);
        trans.setFullCharge(groupCharge + service.getCharge()); 
        trans.setAmount(Math.round((groupCharge + service.getCharge())* trans.getRatio()));
		return trans; 
	}

	public void setDelegate(TransactionCustomize delegate)
    {
    	this.delegate_ = delegate; 
    }


	AuxiliaryService service; 
	TransactionCustomize delegate_; 
}
