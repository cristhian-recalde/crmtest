package com.trilogy.app.crm.discount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * @author harsh.murumkar
 * @since 10.5
 * 
 *        Visitor responsible to identify entries from DiscountEvent and check whether
 *        records are eligible for Discounting or not.
 */
public class DiscountClassAssignmentVisitor extends AbstractDiscountClassAssignement
{

    private static final long serialVersionUID = 1L;

    public DiscountClassAssignmentVisitor(final LifecycleAgentSupport lifecycleAgent)
    {
        super();
        lifecycleAgent_ = lifecycleAgent;
    }


    /**
     * Return the DiscountClassAssignmentVisitor.
     */
    public DiscountClassAssignmentVisitor getVisitor()
    {
        return visitor_;
    }


    @Override
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(), "AssiginingDiscountClass");
        
        DiscountClassAssignmentThreadPoolVisitor threadPoolVisitor = new DiscountClassAssignmentThreadPoolVisitor(ctx,
                getThreadPoolSize(ctx), getThreadPoolQueueSize(ctx), this, lifecycleAgent_);
        try
        {
        	List<Object> banList = null;
        	Context subContext = ctx.createSubContext();
        	String banParameter = (String)subContext.get(AccountConstants.BAN_FOR_DISCOUNT_EVENT_UPDATE);
        	if(null != banParameter && !banParameter.isEmpty()){
        		banList = new ArrayList<Object>();
        		banList.add(banParameter);
        		subContext.put(DiscountClassContextAgent.DISCOUNT_RULE_VERSIONING_CHECK,false);
        	}else{
        		String ruleVersioningSql = DiscountEventSqlGenerator.getDiscountingSqlGenerator().getDiscountRuleVersionFilter();
        		banList = (List<Object>) AccountSupport.getQueryDataList(subContext, ruleVersioningSql);
        		if(banList.size()>0)
        		{
        			subContext.put(DiscountClassContextAgent.DISCOUNT_RULE_VERSIONING_CHECK,true);
        			// old code 
        			//String mainSql = DiscountEventSqlGenerator.getDiscountingSqlGenerator().getDiscountingBanReevaluationFilter();
        			
        			//Getting the all the target ban from the activity trigger table for the discounting scope feature
        			String mainSql = DiscountEventSqlGenerator.getDiscountingSqlGenerator().getDiscountingScopeBanReevaluationFilter();
            		banList = (List<Object>) AccountSupport.getQueryDataList(subContext, mainSql);
                   
        		}else
        		{
        			//Change the query for the target ban
        			//old code
        			//String mainSql = DiscountEventSqlGenerator.getDiscountingSqlGenerator().getDiscountingFilter();
        			
        			//Fetcing the default targetban from the DiscountActivityTrigger table aligning   to the   
        			String mainSql = DiscountEventSqlGenerator.getDiscountingSqlGenerator().getDiscountingScopeFilter();
            		banList = (List<Object>) AccountSupport.getQueryDataList(subContext, mainSql);
            		subContext.put(DiscountClassContextAgent.DISCOUNT_RULE_VERSIONING_CHECK,false);
        		}
        		
        	}
        	
        	LogSupport.info(subContext, this, "TargetBan list is = [" + banList + " ]");
        	
        	int totalAcctSize = banList.size();
            int initSize = 0;
            int size = getConfiguredFetchSize(subContext);
            if (totalAcctSize < getConfiguredFetchSize(subContext))
            {
                size = totalAcctSize;
            }
            while (size <= totalAcctSize)
            {
                if (totalAcctSize - size < getConfiguredFetchSize(subContext))
                {
                    size = totalAcctSize;
                }
                List<Object> acctChunkList = banList.subList(initSize, size);
                initSize = size;
                size = size + getConfiguredFetchSize(subContext);
                String banString = StringUtils.join(acctChunkList, "','");
                LogSupport.info(subContext, this, "BanString =. " +banString);
                SimpleXStatement predicate = new SimpleXStatement(" BAN IN ( '" + banString + "' ) ");
                try
                {
                	Home accountHome =  (Home) subContext.get(AccountHome.class);
                    Collection<Account> accounts = accountHome.where(subContext, predicate).selectAll(subContext);
                    Iterator<Account> itrator = accounts.iterator();
                    while (itrator.hasNext())
                    {
                        Account account = itrator.next();
                       	LogSupport.info(subContext, this, "Rule Engine evalution for Account=" +account.getBAN());
                        threadPoolVisitor.visit(subContext, account);
                    }
                }
                catch (final Exception e)
                {
                    LogSupport.minor(subContext, this, "Error getting while process account. ", e);
                }
            }
        }
        catch (final HomeException e)
        {
            String cause = "Unable to retrieve accounts";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(": ");
            sb.append(e.getMessage());
            LogSupport.major(ctx, this, sb.toString(), e);
            throw new IllegalStateException("Discount Class Assignment Process" + " failed: " + cause, e);
        }
        catch (final Throwable e)
        {
            String cause = "General error";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(": ");
            sb.append(e.getMessage());
            LogSupport.major(ctx, this, sb.toString(), e);
            throw new IllegalStateException("Discount Class Assignment Process" + " failed: " + cause, e);
        }
        finally
        {
            try
            {
                threadPoolVisitor.getPool().shutdown();
                threadPoolVisitor.getPool().awaitTerminationAfterShutdown(TIME_OUT_FOR_SHUTTING_DOWN);
            }
            catch (final Exception e)
            {
                LogSupport.minor(ctx, this,
                        "Exception catched during wait for completion of all discountClassAssignment threads", e);
            }
        }
        pm.log(ctx);
    }


    /**
     * Get discounting Thread pool size from configuration
     * 
     * @param ctx
     * @return
     */
    private int getThreadPoolSize(Context ctx)
    {
        GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getDiscountingProcessThreads();
    }


    /**
     * Get discounting queue size from configuration
     * 
     * @param ctx
     * @return
     */
    private int getThreadPoolQueueSize(Context ctx)
    {
        GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getDiscountingProcessQueueSize();
    }


    /**
     * Get discounting fetch size from configuration
     * 
     * @param ctx
     * @return
     */
    private int getConfiguredFetchSize(Context ctx)
    {
        GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getDiscountingProcessFetchSize();
    }


    protected LifecycleAgentSupport getLifecycleAgent()
    {
        return lifecycleAgent_;
    }


    private LifecycleAgentSupport lifecycleAgent_;
    private DiscountClassAssignmentVisitor visitor_;
    public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;
}
