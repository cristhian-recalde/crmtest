package com.trilogy.app.crm.blacklist;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

import com.trilogy.app.crm.bean.BlackList;

public class BlackListIdPredicate
   implements Predicate
{
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context ctx, Object obj) throws AbortVisitException {
		return f(obj);
	}
   public BlackListIdPredicate(final int type, final String id)
   {
      if (id == null || id.length() == 0)
      {
         throw new IllegalArgumentException(
               "Could not initialize predicate. The id is invalid");
      }
      idType_ = type;
      idNumber_ = id;
   }

   public boolean f(final Object obj)
   {
      final BlackList item = (BlackList)obj;
      return (item.getIdType()==idType_ && item.getIdNumber().equals(idNumber_));
   }
   
   public String getDefSql()
   {
   	 return "idType = " + idType_ +" AND idNumber = '"+ idNumber_ + "'";
   }

   protected final int idType_;
   protected final String idNumber_;
}
