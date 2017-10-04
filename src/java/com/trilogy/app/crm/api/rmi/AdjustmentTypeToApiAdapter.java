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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.AdjustmentActionEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.AdjustmentTypeReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.AdjustmentTypeStateEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.ProfileTypeEnum;

/**
 * Adapts AdjustmentType object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class AdjustmentTypeToApiAdapter implements Adapter
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    public Object adapt(final Context ctx, final Object obj)
    {
        return adaptAdjustmentTypeToReference(ctx, (AdjustmentType) obj);
    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException();
    }


    /**
     * Adapts an CRM adjustment type object to API adjustment type.
     *
     * @param context
     *            The operating context.
     * @param adjustmentType
     *            CRM adjustment type.
     * @param sp
     *            Service provider identifier.
     * @return The adapted API adjustment type.
     */
    public static com.redknee.util.crmapi.wsdl.v2_1.types.transaction.AdjustmentType adaptAdjustmentTypeToApi(final Context context,
        final AdjustmentType adjustmentType, final int sp)
    {
        final com.redknee.util.crmapi.wsdl.v2_1.types.transaction.AdjustmentType type;
        type = new com.redknee.util.crmapi.wsdl.v2_1.types.transaction.AdjustmentType();
        adaptAdjustmentTypeToReference(context, adjustmentType, type);
        final Integer spid = Integer.valueOf(sp);
        final AdjustmentInfo info = (AdjustmentInfo) adjustmentType.getAdjustmentSpidInfo().get(spid);
        if (info != null)
        {
            type.setSpid(spid);
            type.setGlCode(info.getGLCode());
            type.setInvoiceDescription(info.getInvoiceDesc());
            type.setTaxAuthority(Long.valueOf(info.getTaxAuthority()));
        }

        return type;
    }


    /**
     * Adapts an CRM adjustment type object to API adjustment type reference.
     *
     * @param context
     *            The operating context.
     * @param adjustmentType
     *            CRM adjustment type.
     * @return The adapted API adjustment type reference.
     */
    public static AdjustmentTypeReference adaptAdjustmentTypeToReference(final Context context,
        final AdjustmentType adjustmentType)
    {
        final AdjustmentTypeReference reference = new AdjustmentTypeReference();
        adaptAdjustmentTypeToReference(context, adjustmentType, reference);

        return reference;
    }


    /**
     * Adapts an CRM adjustment type object to API adjustment type reference.
     *
     * @param context
     *            The operating context.
     * @param adjustmentType
     *            CRM adjustment type.
     * @param reference
     *            Reference object to be adapted to.
     * @return The adapted API adjustment type.
     */
    public static AdjustmentTypeReference adaptAdjustmentTypeToReference(final Context context,
        final AdjustmentType adjustmentType, final AdjustmentTypeReference reference)
    {
        reference.setIdentifier(Long.valueOf(adjustmentType.getCode()));
        reference.setParent(Long.valueOf(adjustmentType.getParentCode()));
        reference.setCategory(Boolean.valueOf(adjustmentType.getCategory()));
        reference.setSystem(Boolean.valueOf(adjustmentType.getSystem()));
        reference.setName(adjustmentType.getName());
        reference.setDescription(adjustmentType.getDesc());
        reference.setOwnerType(ProfileTypeEnum.valueOf(adjustmentType.getOwnerType().getIndex()));
        reference.setState(AdjustmentTypeStateEnum.valueOf(adjustmentType.getState().getIndex()));
        reference.setAction(AdjustmentActionEnum.valueOf(adjustmentType.getAction().getIndex()));

        Boolean provisionToOcg = Boolean.TRUE;
        if (context != null)
        {
            /*
             * [Cindy] 2008-02-21: According to Gary and Larry, in trunk, all transactions
             * are provisioned to OCG unless they are payments.
             */
            if (AdjustmentTypeSupportHelper.get(context).isInCategory(context, adjustmentType.getCode(), AdjustmentTypeEnum.Payments))
            {
                provisionToOcg = Boolean.FALSE;
            }
            else
            {
                provisionToOcg = Boolean.TRUE;
            }
        }
        reference.setProvisionToOCG(provisionToOcg);

        return reference;
    }
}
