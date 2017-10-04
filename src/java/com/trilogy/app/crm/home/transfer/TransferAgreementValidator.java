package com.trilogy.app.crm.home.transfer;

import com.trilogy.app.crm.transfer.AbstractTransferAgreement;
import com.trilogy.app.crm.transfer.GroupPrivacyEnum;
import com.trilogy.app.crm.transfer.TransferAgreementXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

public class TransferAgreementValidator
    implements Validator
{
    public void validate(Context ctx, Object obj)
        throws IllegalStateException
    {
        if(!(obj instanceof AbstractTransferAgreement))
        {
            throw new IllegalStateException("System Exception: Unexpected class type.");
        }

        AbstractTransferAgreement agreement = (AbstractTransferAgreement)obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();

        validatePrivateAgreement(ctx, agreement, el);

        el.throwAll();
    }

    private void validatePrivateAgreement(Context ctx, AbstractTransferAgreement agreement, CompoundIllegalStateException el)
    {
        if(GroupPrivacyEnum.PRIVATE.equals(agreement.getPrivacy()))
        {
            if(agreement.getOwnerID() == null || agreement.getOwnerID().trim().length() == 0)
            {
                el.thrown(new IllegalPropertyArgumentException(TransferAgreementXInfo.OWNER_ID, "Owner must be set for private transfer agreements."));
            }
        }
    }
}