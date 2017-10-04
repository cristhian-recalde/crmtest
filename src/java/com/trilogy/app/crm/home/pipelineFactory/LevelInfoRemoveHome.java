package com.trilogy.app.crm.home.pipelineFactory;


import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.app.crm.dunning.DunningLevelXMLHome;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.LevelInfo;
import com.trilogy.app.crm.dunning.DunningLevelXInfo;
/**
 * @author Prahlad Kumar
 *
 */
public class LevelInfoRemoveHome extends HomeProxy{
	
	String seqName;

	/**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    public LevelInfoRemoveHome(final Context ctx,final Home delegate)
    {
    	
    	super(ctx, delegate);
    }

    @Override
    public void remove(Context ctx, Object obj) throws HomeException,
    		HomeInternalException 
    {
    	
    	 if (isLevelUsed(ctx,((LevelInfo)obj)))
         {
             throw new HomeException("Cannot delete, this Level Info is being used by DunningPolicy.");
         }
    	super.remove(ctx, obj);
    }

	private boolean isLevelUsed(Context ctx,
			LevelInfo levelInfo) throws HomeInternalException, HomeException 
	{
		Home home = (Home)ctx.get(DunningLevel.class);
		if(home == null )
		{
			home = new DunningLevelXMLHome(ctx,CoreSupport.getFile(ctx,"DunningLevelConfig.xml"));
		}
		Object dunningLevel = null;
		dunningLevel = home.find(ctx, new EQ(DunningLevelXInfo.LEVEL,levelInfo.getId()));
		return (dunningLevel!=null); 
	}
    

    

    

}
