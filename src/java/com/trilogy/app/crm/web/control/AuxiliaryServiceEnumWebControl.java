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

package com.trilogy.app.crm.web.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.support.LicensingSupportHelper;


/**
 * A custom web control to display different subsets of AuxiliaryServiceTypeEnum in <code>CREATE_MODE</code>.
 *
 * @author lily.zou@redknee.com
 * @author cindy.wong@redknee.com
 */
public class AuxiliaryServiceEnumWebControl extends EnumWebControl
{

    /**
     * Create a new instance of <code>AuxiliaryServiceEnumWebControl</code>.
     */
    public AuxiliaryServiceEnumWebControl()
    {
        super(AuxiliaryServiceTypeEnum.COLLECTION);
        super.setAutoPreview(true);
    }


    /**
     * Create a new instance of <code>AuxiliaryServiceEnumWebControl</code>.
     *
     * @param enumSize
     *            Number of items to display at one time.
     */
    public AuxiliaryServiceEnumWebControl(final int enumSize)
    {
        super(AuxiliaryServiceTypeEnum.COLLECTION, enumSize);
        super.setAutoPreview(true);
    }


    /**
     * Create a new instance of <code>AuxiliaryServiceEnumWebControl</code>.
     *
     * @param enumSize
     *            Number of items to display at one time.
     * @param autoPreview
     *            If set to <code>true</code>, the page refreshes whenever the values changes.
     */
    public AuxiliaryServiceEnumWebControl(final int enumSize, final boolean autoPreview)
    {
        super(AuxiliaryServiceTypeEnum.COLLECTION, enumSize);
        super.setAutoPreview(autoPreview);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EnumCollection getEnumCollection(final Context context)
    {
        final boolean isCreateMode = context.getInt("MODE", DISPLAY_MODE) == CREATE_MODE;
        final boolean isAdditionalMsisdnEnabled = AdditionalMsisdnAuxiliaryServiceSupport
                .isAdditionalMsisdnEnabled(context);
        final List<AuxiliaryServiceTypeEnum> resultList = new ArrayList<AuxiliaryServiceTypeEnum>();
        for (final Iterator iterator = AuxiliaryServiceTypeEnum.COLLECTION.iterator(); iterator.hasNext();)
        {
            final AuxiliaryServiceTypeEnum type = (AuxiliaryServiceTypeEnum) iterator.next();
            boolean add = true;
            if (AuxiliaryServiceTypeEnum.CallingGroup.equals(type) && isCreateMode)
            {
                add = false;
            }
            else if (AuxiliaryServiceTypeEnum.MultiSIM.equals(type))
            {
                add = LicensingSupportHelper.get(context).isLicensed(context, CoreCrmLicenseConstants.MULTI_SIM_LICENSE);
            }
            else if (!isAdditionalMsisdnEnabled)
            {
                add = !AuxiliaryServiceTypeEnum.AdditionalMsisdn.equals(type);
            }
            
            if (add)
            {
                resultList.add(type);
            }
        }
        Enum[] resultArray = new Enum[resultList.size()];
        resultArray = resultList.toArray(resultArray);
        return new EnumCollection(resultArray);
    }
}
