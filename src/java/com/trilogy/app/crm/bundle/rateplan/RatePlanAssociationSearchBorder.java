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

package com.trilogy.app.crm.bundle.rateplan;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

/**
 * Search border for bundle rate plan association.
 *
 * @author larry.xia@redknee.com
 * @author cindy.wong@redknee.com
 */
public class RatePlanAssociationSearchBorder extends SearchBorder
{
    /**
     * Creates a new rate plan association search border.
     *
     * @param ctx The operating context.
     */
    public RatePlanAssociationSearchBorder(final Context ctx)
    {
        super(ctx.createSubContext(), RatePlanAssociation.class, new RatePlanAssociationSearchWebControl());

        // rate plan ID
        addAgent(new CustomOrSelectSearchAgent(RatePlanAssociationSearchXInfo.RATE_PLAN_ID)
                .addPropertyInfo(RatePlanAssociationXInfo.VOICE_RATE_PLAN)
                .addPropertyInfo(RatePlanAssociationXInfo.SMS_RATE_PLAN)
                .addPropertyInfo(RatePlanAssociationXInfo.DATA_RATE_PLAN)
                .addIgnore(RatePlanAssociationSearch.DEFAULT_RATEPLANID));

        // category ID
        addAgent(new SelectSearchAgent(RatePlanAssociationXInfo.CATEGORY_ID, RatePlanAssociationSearchXInfo.CATEGORY_ID)
                .addIgnore(RatePlanAssociationSearch.DEFAULT_CATEGORYID));

        // bundle ID
        addAgent(new SelectSearchAgent(RatePlanAssociationXInfo.BUNDLE_ID, RatePlanAssociationSearchXInfo.BUNDLE_ID)
                .addIgnore(RatePlanAssociationSearch.DEFAULT_BUNDLEID));

        // SPID
        //addAgent(new SelectSearchAgent(RatePlanAssociationXInfo.SPID, RatePlanAssociationSearchXInfo.SPID)
        //        .addIgnore(RatePlanAssociationSearch.DEFAULT_SPID));
    }
}
