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
import java.util.Iterator;

import com.trilogy.app.crm.bas.promotion.processor.*;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author kwong
 *
 */
public class PromotionSupport 
{
    
    /**
     * The objects of which this object is composed.
     */
    protected final ArrayList composites_;
   
    
    /**
     * Creates a new support object.
     */
    public PromotionSupport()
    {
        composites_ = new ArrayList();
    }
    
    /**
     * 
     * @return Collection, collection of SummeryBundle
     */
    public Collection generate()
        throws HomeException
    {
        final Iterator compositeIterator = composites_.iterator();
        // for each promotion set result from a calldetail table search 
        Collection results = new ArrayList();
        while (compositeIterator.hasNext())
        {
            final PromotionBy promotionSet = (PromotionBy)compositeIterator.next();
            results.add(promotionSet.generate());
        }
        return results;
    }


    /**
     * Adds the given Promotion to this composition.
     *
     * @param promotion The promotion to add to this composition.
     *
     * @exception IllegalArgumentException Thrown if the given Promotion
     * parameter is null.
     */
    public void add(final PromotionBy promotion)
    {
        if (promotion == null)
        {
            throw new IllegalArgumentException(
                "Could not add Promotion.  The Promotion parameter is null.");
        }

        composites_.add(promotion);
    }

    /**
     * Adds the given collection to this composition.
     *
     * @param Collection The collection to add to this composition.
     *
     * @exception IllegalArgumentException Thrown if the given Collection
     * parameter is null.
     */
    public void add(final Collection collection)
    {
  
        if (collection == null)
        {
            throw new IllegalArgumentException(
                "Could not add Promotion.  The Promotion Collection parameter is null.");
        }
        for (Iterator iter = collection.iterator(); iter.hasNext();)
        {
            add((PromotionBy)iter.next());
        }
       
    }

    /**
     * Gets the collection of Accumulator composites.
     *
     * @return The collection of Accumulator composites.
     */
    public Collection getPromotions()
    {
        return composites_;
    }
       

} // class
