package com.trilogy.app.crm.bundle;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * This is a version of AdapterHome that only handles the tranformation of
 * an input object of a different type, it does not expect to output to
 * a bean different than the underlying data layer.
 *
 * This is happening because these overriden methods do not adapt the result.
 */
public class WriteOnlyAdapterHome
   extends AdapterHome
{
   public WriteOnlyAdapterHome(Context ctx, Adapter adapter, Home delegate)
   {
      super(ctx, adapter, delegate);
   }

   public Object find(Context ctx, Object key)
      throws HomeException
   {
      return getDelegate().find(ctx, key);
   }

   public Collection select(Context ctx, Object where)
      throws HomeException
   {
      return getDelegate().select(ctx, where);
   }

   public Visitor forEach(Context ctx, Visitor visitor, Object where)
      throws HomeException
   {
      return getDelegate().forEach(ctx, visitor, where);
   }
}
