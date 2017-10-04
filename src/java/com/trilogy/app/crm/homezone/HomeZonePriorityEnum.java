/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.homezone;

import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

/**
 * @author pkulkarni
 * 
 * This class will be used to generate the priority values dynamically which
 * will be used by the Homzone auxiliary service to display in the subscriber
 * rating tab
 *  
 */
public class HomeZonePriorityEnum extends AbstractEnum
{
    public HomeZonePriorityEnum(short index, String desc)
    {
        super(index, desc);
    }

    /*
     * We are not using this method as we wont ever operate getCollection on
     * this class This will only be used for creating new Enum objects and
     * putting them into the EnumCollection. So returning new Enumcollection
     * which returns only that object.-no use btw
     *  
     */
    public EnumCollection getCollection()
    {
        return new EnumCollection(new Enum[] { this });
    }

}
