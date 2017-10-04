package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.home.account.AccountPropertyListeners;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class CRMSpidLazyLoadedPropertyUpdateHome extends AdapterHome
{

    private static final long serialVersionUID = 1L;
    
    
    public CRMSpidLazyLoadedPropertyUpdateHome(final Context ctx, final Home home)
    {
        super(ctx, home, adapterInstance());
    }

    public static Adapter adapterInstance()
    {
        return adapterLazyLoadInstance_;
    }

    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        // DO NOT call super.create(ctx, obj) because in this particular case we have the correct bean with data
        final CRMSpid result = (CRMSpid) getDelegate(ctx).create(ctx, obj);
        
        CRMSpidPropertyListeners listener = (CRMSpidPropertyListeners) result.getCRMSpidLazyLoadedPropertyListener();
        listener.checkLazyLoadedPropertiesInfoChangedFromDefault(result);
        listener.saveChangedInfo(ctx, result);

        result.watchLazyLoadedProperitesChange();
        
        return result;
    }

    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        // DO NOT call super.store(ctx, obj) because in this particular case we have the correct bean with data
        final CRMSpid result = (CRMSpid) getDelegate(ctx).store(ctx, obj);

        result.stopLazyLoadedProperitesChange();
        CRMSpidPropertyListeners listener = (CRMSpidPropertyListeners) result.getCRMSpidLazyLoadedPropertyListener();
        listener.saveChangedInfo(ctx, result);

        result.watchLazyLoadedProperitesChange();
        
        return result;
    }
    
    


    /**
     * Adapter is used because adapt() will be called for all read methods like find() and select()
     */
    static class CRMSpidLazyLoadedPropertyModificaitonAdapter implements Adapter
    {
        public Object adapt(final Context ctx, final Object obj) throws HomeException
        {
            final CRMSpid spidRead = (CRMSpid) obj;

           // accountRead.clearContactInfoChange();
            CRMSpidPropertyListeners listener = (CRMSpidPropertyListeners) spidRead.getCRMSpidLazyLoadedPropertyListener();
            listener.clearPropertyInfoChange();
            spidRead.watchLazyLoadedProperitesChange();

            return spidRead;
        }


        public Object unAdapt(final Context ctx, final Object obj) throws HomeException
        {
            return obj;
        }
    }
    
    private static Adapter adapterLazyLoadInstance_ = new CRMSpidLazyLoadedPropertyModificaitonAdapter();
}