package com.trilogy.app.crm.numbermgn;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.visitor.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.filter.Predicate;

/**
 * This visitor look for the bean with the smallest value sorted by
 * the field supplied by the PropertyInfo.
 * @author candy
 * TODO: make this more generic and harvest the code to Framework
 */
public class LeastRecentVisitor
   extends AbstractValueVisitor
   implements Visitor
{
   protected PropertyInfo pInfo_;

   public LeastRecentVisitor(PropertyInfo pInfo)
   {
      setPropertyInfo(pInfo);
   }

   public PropertyInfo getPropertyInfo()
   {
      return pInfo_;
   }

   public void setPropertyInfo(PropertyInfo pInfo)
   {
      pInfo_ = pInfo;
   }

   public void visit(Context ctx, Object bean)
      throws AgentException, AbortVisitException
   {
      Object field = getPropertyInfo().get(bean);
      // field cannot be compared so just quit
      if (!(field instanceof Comparable))
      {
         throw new AbortVisitException();
      }

      // keeps track of the smallest value
      if (getValue() == null || 
            XBeans.compare(((Comparable)field), ((Comparable)getPropertyInfo().get(getValue()))) == -1)
      {
         setValue(bean);
      }
   }
}
