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

import com.trilogy.app.crm.bean.UserDailyAdjustmentLimit;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimitSearchWebControl;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimitSearchXInfo;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimitXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

/**
 * Search border used in the user daily adjustment limit.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class UserDailyAdjustmentLimitSearchBorder extends SearchBorder
{
    public UserDailyAdjustmentLimitSearchBorder(Context context)
    {
       super(context, UserDailyAdjustmentLimit.class,new UserDailyAdjustmentLimitSearchWebControl());

       addAgent((new SelectSearchAgent(UserDailyAdjustmentLimitXInfo.LIMIT_DATE, UserDailyAdjustmentLimitSearchXInfo.LIMIT_DATE)));

       addAgent(new WildcardSelectSearchAgent(UserDailyAdjustmentLimitXInfo.USER_ID, UserDailyAdjustmentLimitSearchXInfo.USER_ID, true));

    }
 }
