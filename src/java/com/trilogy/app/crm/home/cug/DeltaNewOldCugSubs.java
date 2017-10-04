/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.cug;

import java.util.Map;

import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.util.DeltaMap;


/**
 * A classes that produces delta of Member Subscribers in old and new CUGS
 * 
 * @author ssimar
 * 
 */
public class DeltaNewOldCugSubs
{

    public DeltaNewOldCugSubs(DeltaMap<String, ClosedSub> deltaMapOfNewAndOldCug)
    {
        subNewOldDelta_ = deltaMapOfNewAndOldCug;
    }


    public DeltaNewOldCugSubs(Map<String, ClosedSub> newCugSubs, Map<String, ClosedSub> oldCugSubs)
    {
        subNewOldDelta_ = new DeltaMap<String, ClosedSub>(newCugSubs, oldCugSubs);
    }


    @SuppressWarnings("unchecked")
    public DeltaNewOldCugSubs(ClosedUserGroup newCug, ClosedUserGroup oldCug)
    {
        subNewOldDelta_ = new DeltaMap<String, ClosedSub>(newCug.getSubscribers(), oldCug.getSubscribers());
    }


    public Map<String, ClosedSub> getsubsToBeAdded()
    {
        return subNewOldDelta_.getExclusiveFirstMap();
    }


    public Map<String, ClosedSub> getsubsToBeUpdated()
    {
        return subNewOldDelta_.getDifferntValueFirstMap();
    }


    public Map<String, ClosedSub> getsubsToBeRemoved()
    {
        return subNewOldDelta_.getExclusiveSecondMap();
    }


    public Map<String, ClosedSub> getSubsToBeSame()
    {
        return subNewOldDelta_.getSameValuesMap();
    }

    private final DeltaMap<String, ClosedSub> subNewOldDelta_;
}
