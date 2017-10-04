package com.trilogy.app.crm.xhome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.account.BANAware;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.MsisdnEntryTypeEnum;
import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupNotFoundException;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupSupport;
import com.trilogy.app.transferfund.rmi.data.ContractGroup;

public class MsisdnOwnershipAdapterHomeProxy extends HomeProxy
{
    // Delegate would be the MsisdnHome
    public MsisdnOwnershipAdapterHomeProxy(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj)
        throws HomeException
    {
        return obj;
    }

    @Override
    public Object store(Context ctx, Object obj)
        throws HomeException
    {
        return obj;
    }

    @Override
    public void remove(Context ctx, Object obj)
        throws HomeException
    {
        
    }
    
    private MsisdnOwnership unadapt(Context ctx, Msisdn msisdn)
    {
        if (msisdn != null)
        {
            MsisdnOwnership ownership = new MsisdnOwnership();
            ownership.setBAN(msisdn.getBAN());
            ownership.setOriginalMsisdn(msisdn.getMsisdn());
            ownership.setMsisdn(msisdn.getMsisdn());
            ownership.setSubscriberType(msisdn.getSubscriberType());
            ownership.setOriginalSubscriberType(msisdn.getSubscriberType());
            ownership.setTechnology(msisdn.getTechnology());
            ownership.setSpid(msisdn.getSpid());
            ownership.setMSISDNGroup(msisdn.getGroup());
            ownership.setMsisdnEntryType(msisdn.isExternal() ? MsisdnEntryTypeEnum.EXTERNAL_INDEX : MsisdnEntryTypeEnum.MSISDN_GROUP_INDEX);
            ContractGroup[] contractGroups = null;
            try
            {
                contractGroups = MemberGroupSupport.retrieveContractGroupsForMember(ctx, ownership.getMsisdn());
            }            
            catch (MemberGroupNotFoundException nfe)
            {
                // this is thrown if the member currently does not belong to any contract groups
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "[MsisdnOwnershipHome::unadapt failed to retrieve contract groups for MSISDN on TFA "
                        + "[msisdn=" + ownership.getMsisdn() + "]", e).log(ctx);
            }
            if (contractGroups != null)
            {
                String publicGroupOwnerName = "";
                try
                {
                    publicGroupOwnerName = TransferContractSupport.getPublicGroupOwnerName(ctx);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Public Group Owner name not found", e).log(ctx);
                }
                
                for (ContractGroup group: contractGroups)
                {
                    if (group.getGroupOwner().equals(publicGroupOwnerName))
                    {
                        ownership.setPublicGroupId(group.getGroupId());
                    }
                    else
                    {
                        ownership.setPrivateGroupId(group.getGroupId());
                    }
                }
            }
            return ownership;
        }
        else
        {
            return null;
        }
    }
    
    

    @Override
    public Object find(Context ctx, Object obj) throws HomeException
    {
        return unadapt(ctx, (Msisdn) getDelegate(ctx).find(ctx, obj));
    }

    @Override
    public Collection select(Context ctx, Object obj) throws HomeException
    {
        // retrieve the result set from the BAN
        String ban = getBan(ctx);
        
        Collection<Msisdn> resultSet;
        if( ban != null && !ban.equals(""))
        {
            resultSet = MsisdnSupport.getAcquiredAndHeldMsisdn(ctx, ban);
        }
        else
        {
            new MinorLogMsg(this, "Unexpected query on the MsisdnOwnershipHome.  Received [where=" + obj + "] and could resolve a BAN to filter on from the context.  Failing query!", null).log(ctx);
            throw new UnsupportedOperationException("Unexpected query on the MsisdnOwnershipHome.");
        }
        
        Predicate p = (Predicate) XBeans.getInstanceOf(ctx, obj, Predicate.class);
        List<MsisdnOwnership> list = new ArrayList<MsisdnOwnership>();
        for (Msisdn msisdn : resultSet)
        {
            MsisdnOwnership msisdnOwnership = unadapt(ctx, msisdn);
            if(p == null || p.f(ctx, msisdnOwnership))
            {
                list.add(msisdnOwnership);
            }
        }
        
        return list;
    }

    private String getBan(Context ctx)
    {
        if( ctx.has(Account.class))
        {
            return ((Account)ctx.get(Account.class)).getBAN();   
        }
        else if (ctx.has(Subscriber.class))
        {
            return ((Subscriber)ctx.get(Subscriber.class)).getBAN();
        }
        else if (ctx.has(AbstractWebControl.BEAN))
        {
            Object bean = ctx.get(AbstractWebControl.BEAN);
            if(bean instanceof BANAware)
            {
                return ((BANAware)bean).getBAN();
            }
        }
        return null;
    }

}
