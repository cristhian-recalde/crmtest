package com.trilogy.app.crm.subscriber.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;

/**
 * @deprecated Use {@link #com.redknee.framework.xhome.elang.EQ}
 */
@Deprecated
public class SubscriberStatePredicate
   implements Predicate
{
   public SubscriberStatePredicate(SubscriberStateEnum state)
   {
      state_ = state;
   }

   public boolean f(Context ctx, Object obj)
   {
      Subscriber sub = (Subscriber)obj;

      return state_.equals(sub.getState());
   }

   protected SubscriberStateEnum state_;
}
