package com.trilogy.app.crm.account.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;

public class AccountStatePredicate
   implements Predicate
{
   public AccountStatePredicate(AccountStateEnum state)
   {
      state_ = state;
   }

   public boolean f(Context ctx, Object obj)
   {
      Account account = (Account)obj;

      return state_.equals(account.getState());
   }

   protected AccountStateEnum state_;
}
