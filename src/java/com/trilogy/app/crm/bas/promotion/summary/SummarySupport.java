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
package com.trilogy.app.crm.bas.promotion.summary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author kwong
 *
 */
public class SummarySupport 
    implements Summary
{
    /**
      * Creates a new support object.
      */
     public SummarySupport()
     {
         composite_ = new ArrayList();
     }
    
    
     // INHERIT
     public void extractTo(Object bean)
     {
         final Iterator compositeIterator = composite_.iterator();
         while (compositeIterator.hasNext())
         {
             final Summary summary = (Summary)compositeIterator.next();
             summary.extractTo(bean);
         }
     }


     /**
      * Adds the given Summary to this composition.
      *
      * @param Summary The Summary to add to this composition.
      *
      * @exception IllegalArgumentException Thrown if the given Summary
      * parameter is null.
      */
     public void add(final com.redknee.app.crm.bas.promotion.summary.Summary summary)
     {
         if (summary == null)
         {
             throw new IllegalArgumentException(
                 "Could not add Summary.  The Summary parameter is null.");
         }

         composite_.add(summary);
     }


     /**
      * Gets the collection of Summary composites.
      *
      * @return The collection of Summary composites.
      */
     public Collection getSummaries()
     {
         return composite_;
     }
    
    
     /**
      * The objects of which this object is composed.
      */
     protected final ArrayList composite_;
    
 } // class