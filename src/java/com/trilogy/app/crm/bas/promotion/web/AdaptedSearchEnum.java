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

import java.util.Iterator;

import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

/**
 * @author kwong
 *
 */ 
public class AdaptedSearchEnum 
    extends AbstractEnum
{
    public static EnumCollection COLLECTION;
    
    private AdaptedSearchEnum(int index, String desc)
    {
       super((short)index, desc);
    }

    public AdaptedSearchEnum(EnumCollection enumeration)
    {
        super((short)-1, "---");
        COLLECTION = getCollection(enumeration);
    }


    public final static AdaptedSearchEnum NONE = new AdaptedSearchEnum(-1, "---");
    
    static public EnumCollection getCollection(EnumCollection enumeration)
    {
        Iterator iter = enumeration.iterator();
        Enum[] col = new Enum[enumeration.size()+1];
        col[0] = NONE;
        int i=1;
        while (iter.hasNext())
        {
            Enum curr = (Enum)iter.next();
            col[i++] = new AdaptedSearchEnum(curr.getIndex(), curr.getDescription());
        }
        return new EnumCollection(col);
    }
    
	public EnumCollection getCollection() {
		return COLLECTION;
	}  
	
	public static Enum get(short index)
	{
		return COLLECTION.get(index);
	}
  

}
