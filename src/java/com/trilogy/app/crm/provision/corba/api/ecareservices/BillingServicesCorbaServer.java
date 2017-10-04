/*
 * Created on April 11, 2006
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED
 * BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */
package com.trilogy.app.crm.provision.corba.api.ecareservices;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.service.corba.CorbaServerInitService;

public class BillingServicesCorbaServer extends CorbaServerInitService
{

    public BillingServicesCorbaServer(Context ctx)
    {
        super(ctx);
     
        ctx.put(BillingServicesFacade.class, new BillingServicesFacade());
    }

}
