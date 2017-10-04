/*
 * Created on Apr 1, 2003
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.bas.promotion.web;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.bean.PromotionGenerationTypeEnum;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.xhome.CustomEnumCollection;

/**
 * @author kwong
 *
 */
public class PromotionGenerationEnumWebControl
    extends EnumWebControl
{
    static final String PERMISSION = "crm.promotion";

    public PromotionGenerationEnumWebControl(final EnumCollection enumeration, final boolean autoPreview){
        super(enumeration, autoPreview);
    }

    @Override
    public void toWeb (final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final EnumCollection newCol;

        if (AuthSupport.hasPermission(ctx, new SimplePermission(PERMISSION)))
        {
            newCol = new CustomEnumCollection(
                    PromotionGenerationTypeEnum.SUBSCRIBER,
                    PromotionGenerationTypeEnum.SPID);
        }
        else
        {
            newCol = new CustomEnumCollection(
                    PromotionGenerationTypeEnum.SPID);
        }

        new EnumWebControl(newCol, true).toWeb(ctx, out, name, obj);
    }
}
