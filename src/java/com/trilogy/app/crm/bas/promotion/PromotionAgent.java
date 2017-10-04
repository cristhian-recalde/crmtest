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
package com.trilogy.app.crm.bas.promotion;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bas.promotion.summary.Summary;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;

/**
 * Promotion Agent searches calldetail table with given criteria (promotionBys)
 * and summarize the result in collection of SummaryBundle, which contains 
 * promotion summary and search critera. 
 * 
 * @author kwong
 *
 */
public class PromotionAgent 
{
    private PromotionSupport composites_;
    public PromotionAgent(Collection promotionBys) 
    {
		super();
        composites_ = new PromotionSupport();
        composites_.add(promotionBys);
	}

    /**
     * summarize usage reports from calldetail table
     * 
     * @return collection, Collection of Summary with PromotionBy criteria 
     * @throws HomeException
     */
    public Collection summarize()
        throws HomeException
    {       
        return composites_.generate();
    }


}
