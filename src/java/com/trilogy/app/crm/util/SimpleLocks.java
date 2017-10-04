package com.trilogy.app.crm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class SimpleLocks 
{
	
	private static final ArrayList<SimpleLocks> lockCache_ = new ArrayList<SimpleLocks>(10);
	private static final ScheduledExecutorService cleanUp_= Executors.newSingleThreadScheduledExecutor();
	private static final long CLEAN_UP_INTERVAL = 600;
	
	
	// clean up individual lock cache
    static
    {
        cleanUp_.scheduleWithFixedDelay(new Runnable(){
            public void run()
            {
                synchronized(lockCache_)
                {
                    try
                    {
                        for (SimpleLocks lock : lockCache_)
                        {
                            lock.cleanUp();
                        }
                    }
                    catch (Exception e)
                    {
                        //eat all exceptions to ensure the clean up thread running
                    }
                }
            }}, CLEAN_UP_INTERVAL, CLEAN_UP_INTERVAL, TimeUnit.SECONDS);
    }
    
    
    private SimpleLocks(){}
    
    public static SimpleLocks newLock()
    {
    	SimpleLocks lock = new SimpleLocks();
        synchronized (lockCache_)
        {
            lockCache_.add(lock);
        }
        return lock;
    }
    


    public void lock(Object id)
    {
         ReentrantLock individualLock = null;
         cleanLock_.readLock().lock();         
         if ( !lockMap_.containsKey(id))
         { 	 
        	 individualLock =  new ReentrantLock();
        	 cleanLock_.readLock().unlock(); 
        	 cleanLock_.writeLock().lock(); 
        	 lockMap_.put(id, individualLock);
             cleanLock_.writeLock().unlock(); 
             individualLock.lock();
         } else 
         {
        	 individualLock = lockMap_.get(id); 
        	 cleanLock_.readLock().unlock(); 
        	 individualLock.lock(); 
         }

    }

    protected void cleanUp()
    {
       
    	Collection<Object> locks = new HashSet<Object>(); 
        locks.addAll(lockMap_.keySet()); 	

        for (Object id : locks)
        {
           	cleanLock_.readLock().lock(); 
            ReentrantLock lock =  lockMap_.get(id);
            cleanLock_.readLock().unlock();
            if (!(lock.isLocked()||lock.hasQueuedThreads()))
            {                	 
                cleanLock_.writeLock().lock();
                lockMap_.remove(id); 
                cleanLock_.writeLock().unlock(); 
            }	
                    
        }
       
        
    }

    public void unlock(Object id)
    {
        cleanLock_.readLock().lock();
        ReentrantLock lock = lockMap_.get(id);
        cleanLock_.readLock().unlock();
        
        if (lock!=null && lock.isHeldByCurrentThread()) 
        {  	 
        	lock.unlock();
        }
    }

    
    /**
     *  need call this method explicitly to prevent memory leak
     *  in case the instance will be abandoned.  
     */   
    protected void disable() throws Throwable
    {  	
      	// this will prevent memory leaking.
    	synchronized (lockCache_)
        {
            lockCache_.remove(this);
          }
    }

    
	 private final ConcurrentHashMap<Object, ReentrantLock> lockMap_ = 
	        new ConcurrentHashMap<Object, ReentrantLock>();
	    
	 private final ReentrantReadWriteLock cleanLock_ = new ReentrantReadWriteLock();


}
