package com.trilogy.app.crm.client.urcs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.urcs.promotion.v2_0.CounterProfile;
import com.trilogy.app.urcs.promotion.v2_0.Promotion;
import com.trilogy.app.urcs.promotion.v2_0.Counter;
import com.trilogy.app.urcs.promotion.v2_0.CounterDelta;
import com.trilogy.app.urcs.promotion.v2_0.PromotionType;
import com.trilogy.app.urcs.promotion.v2_0.SubscriberIdentity;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;

/**
 * Test version of PromotionManagementClientV2, provides transient storage for counters.
 * 
 * @author amahmood
 * @since 8.5
 */

public class PromotionManagementClientV2ImplTest implements PromotionManagementClientV2
{

    Map<CounterKey, Counter> _counters = new HashMap<CounterKey, Counter>();  

    
    public PromotionManagementClientV2ImplTest(final Context ctx)
    {
        return;
    }    


    @Override
    public String version()
    {
        return "Stub V2";
    }

    @Override
    public Counter retrieveCounterForSub(Context ctx, String msisdn, int subscriptionType, long counterProfileID)
        throws RemoteServiceException
    {
        final CounterKey key = new CounterKey(msisdn, subscriptionType, counterProfileID);
        final Counter counter = _counters.get(key); 

        new InfoLogMsg(this.getClass(), "Retrieved for msisdn=" + msisdn + 
                ", counter = " + counterToString(counter), null).log(ctx);
        
        return _counters.get(key);
    }

   
    @Override
    public Collection<Counter> updateCounters(Context ctx, String msisdn, int subscriptionType, Collection<CounterDelta> deltas)
         throws RemoteServiceException
    {
        if (msisdn == null || deltas == null)
        {
            return null;
        }
        
        final List<Counter> updated = new ArrayList<Counter>();
        
        for (CounterDelta delta : deltas)
        {
            CounterKey key = new CounterKey(msisdn, subscriptionType, delta.counterId);
            Counter counter = _counters.get(key);
            
            if (counter == null)
            {
                counter = createCounter(msisdn, subscriptionType, delta.counterId, delta.delta);
                _counters.put(key, counter);
            }
            else
            {
                counter.value += delta.delta;
            }
            new InfoLogMsg(this.getClass(), "For msisdn=" + msisdn + ", updated counter=" + counterToString(counter), null).log(ctx);
            updated.add(counter);
        }
    
        return updated;
    }
    
    @Override
    public void deleteCounter(Context ctx, String msisdn, int subscriptionType, long counterProfileID)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void deleteAllCountersForSub(Context ctx, String msisdn, int subscriptionType)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setSubscriberOptions(Context ctx, SubscriberIdentity subscriberId, 
            Collection<Long> addOptions, Collection<Long> removeOptions)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public CounterProfile createSystemCounter(Context ctx, CounterProfile counterProfile)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promotion retrievePromotion(Context ctx, long promotionId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public CounterProfile retrieveCounterProfile(Context ctx, long profileId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Counter> retrieveAllCountersForSub (Context ctx, String msisdn, int subscriptionType)
    {
        throw new UnsupportedOperationException();
    }
    
    private Counter createCounter( String msisdn, int subscriptionType, long counterProfileID, long count)
    {
        Counter counter = new Counter();
        
        counter.msisdn = msisdn;
        counter.subscriptionType = subscriptionType;
        counter.counterProfileID = counterProfileID;
        counter.value = count;
        
        return counter;
    }
    
    private String counterToString(final Counter counter)
    {
        return counter == null ? "null" : 
            String.format("Counter(msisdn=%s, subType=%d, counterProfile=%d, count=%d)", 
                    counter.msisdn, counter.subscriptionType, counter.counterProfileID, counter.value);
    }

    
    private static class CounterKey {
        public String msisdn;
        public int subscriptionType;
        public long counterProfileID;

        public CounterKey(String msisdn, int subscriptionType, long counterProfileID)
        {
            this.msisdn = msisdn;
            this.subscriptionType = subscriptionType;
            this.counterProfileID = counterProfileID;
        }

        public boolean equals(Object o)
        {
           if ( o == this )                  return true;
           if ( o == null )                  return false;
           if ( o.getClass() != getClass() ) return false;

           CounterKey other = (CounterKey)o; 
           return this.msisdn.equals(other.msisdn) &&
                   this.subscriptionType == other.subscriptionType &&
                   this.counterProfileID == other.counterProfileID;
        }
        
        public int hashCode()
        {           
           int h = 17;         
           h = h * 37 + hashCode(msisdn);
           h = h * 37 + hashCode(subscriptionType);
           h = h * 37 + hashCode(counterProfileID);

           return h;
        }
        
        private int hashCode(Object o)
        {
            return o != null ? o.hashCode() : 0;
        }
        
        public String toString()
        {
            return String.format("CounterKey(msisdn=%s, subType=%d, counterProfile=%d)", msisdn, subscriptionType, counterProfileID);
        }
    }


    @Override
    public Collection<Promotion> listAllPromotionsForSpid(Context ctx, int spid, PromotionType type)
            throws RemoteServiceException
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Promotion retrievePromotionWithSPID(Context ctx, int spid, long promotionId) throws RemoteServiceException
    {
        // TODO Auto-generated method stub
        return null;
    }


    
    
}
