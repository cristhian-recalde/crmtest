package com.trilogy.app.crm.bulkloader.transaction;

public class CommitRatioCounter 
{

	private final int commitRatio_;
	private int count_ = 0;
	
	public CommitRatioCounter(int commitRatio)
	{
		commitRatio_ = commitRatio;
	}
	
	public boolean incrementAndCheck()
	{
		boolean result = (++count_ == commitRatio_);
		
		if(result)
		{
			resetCounter();
		}
		
		return result;
	}
	
	protected void resetCounter()
	{
		count_ = 0;
	}
}
