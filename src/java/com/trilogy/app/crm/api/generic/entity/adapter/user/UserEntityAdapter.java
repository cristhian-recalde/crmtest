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
package com.trilogy.app.crm.api.generic.entity.adapter.user;

import com.trilogy.app.crm.api.generic.entity.adapter.DefaultGenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.EntityParsingException;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;

/**
 * @author sbanerjee
 *
 */
public class UserEntityAdapter extends DefaultGenericEntityAdapter
{
    @Override
    public Entity unAdapt(Context ctx, Object obj)
            throws EntityParsingException
    {
        User usr = (User)obj;
        
        /*
         * For now, we make it null, as the extension class may not be present
         * in the class-path of RMI clients.
         */
        usr.setExtension(null);
        return super.unAdapt(ctx, obj);
    }
}
