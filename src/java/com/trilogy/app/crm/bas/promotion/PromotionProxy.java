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

import java.util.Collection;

import com.trilogy.app.crm.bas.promotion.summary.*;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * @author kwong
 *
 */
public abstract class PromotionProxy 
    implements PromotionBy

{
    protected PromotionBy delegate_ ;
    protected Object    criteria_ = null;
    protected Object    whereClause_ = null;
    
    public void setWhereClause(Object where)
    {    
        whereClause_ = where;
    }
    
    /**
     * And two criteria into one for search sql clause
     * @param o1
     * @param o2
     * @return
     */
    abstract protected Object joinClause(Object o1, Object o2);
    
    
    public SummaryBundle generate()
        throws HomeException
    {
        if (criteria_ == null)
        {
            getDelegate().setWhereClause(whereClause_);
        }
        else if (whereClause_ != null)
        {
            getDelegate().setWhereClause(joinClause(criteria_, whereClause_));
        } 
        else 
        {
            getDelegate().setWhereClause(criteria_);
        }

        return getDelegate().generate();
    }
    
    
    public PromotionProxy(PromotionBy delegate)
    {
        setDelegate(delegate);
    }
    
    
    public PromotionProxy(PromotionBy delegate, Object criteria)
    {
        setDelegate(delegate);
        criteria_ = criteria;
    }
    
    
    public void setDelegate(PromotionBy delegate)
    {
          delegate_ = delegate;
    }


    public PromotionBy getDelegate()
    {
      return delegate_;
    }



}
