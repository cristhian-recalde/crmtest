/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.api.generic.entity.validator;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author alok.sohani
 * @since 9_5_4
 * 
 * Validation for not able to create Msisdn in 'IN_USE' from Generic Entity API
 * 
 */
public class MsisdnGenericEntityValidator extends AbstractGenericEntityValidator implements Validator
{

    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        Msisdn msisdn = (Msisdn) obj;
        if (msisdn.getState() == MsisdnStateEnum.IN_USE)
        {
            new MinorLogMsg(this,
                    "MsisdnGenericEntityValidator::validate():Msisdn cannot be in 'IN_USE' state while creation.", null)
                    .log(ctx);
            el.thrown(new IllegalPropertyArgumentException("", "Msisdn cannot be in 'IN_USE' state while creation."));
            el.throwAll();
        }
    }
}