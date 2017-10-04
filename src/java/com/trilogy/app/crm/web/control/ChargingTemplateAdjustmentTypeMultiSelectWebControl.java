package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;


public class ChargingTemplateAdjustmentTypeMultiSelectWebControl extends AdjustmentTypeMultiSelectWebControl
{
    
    public ChargingTemplateAdjustmentTypeMultiSelectWebControl()
    {
        super( new AdjustmentTypeEnum[] {AdjustmentTypeEnum.RecurringCharges, AdjustmentTypeEnum.AuxiliaryServices,
                AdjustmentTypeEnum.AuxiliaryBundles, AdjustmentTypeEnum.DiscountAuxiliaryServices,
                AdjustmentTypeEnum.BalanceTransfer, AdjustmentTypeEnum.Payments});
    }
}
