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
package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.home.UserDailyAdjustmentLimitTransactionIncreaseHome;
import com.trilogy.app.crm.validator.UserDailyAdjustmentLimitTransactionValidator;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;


/**
 * Provides a border that adds a Transaction validating home to the context to
 * enforce the rule that payments are credits and not debits.
 *
 * @author Marcio Marques
 * @since 9.1.1
 */
public class UserDailyAdjustmentLimitEnforcementBorder
    implements Border
{
    // INHERIT
    public void service(
        final Context ctx,
        final HttpServletRequest req,
        final HttpServletResponse res,
        final RequestServicer delegate)
        throws ServletException, IOException
    {
        final Home originalHome = (Home)ctx.get(TransactionHome.class);

        final Context subcontext = ctx.createSubContext();
        
        CompoundValidator createValidator = new CompoundValidator(); 
        createValidator.add(new UserDailyAdjustmentLimitTransactionValidator()); 

        CompoundValidator storeValidator = new CompoundValidator(); 

        final Home enforcementHome = new ValidatingHome(createValidator, storeValidator,
                new UserDailyAdjustmentLimitTransactionIncreaseHome(subcontext, originalHome));

        subcontext.put(TransactionHome.class, enforcementHome);

        delegate.service(subcontext, req, res);
        
        
    }

} // class
