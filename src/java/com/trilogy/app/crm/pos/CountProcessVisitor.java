package com.trilogy.app.crm.pos;

public interface CountProcessVisitor 
{

	/**
     * Returns the number of beans processed by the visitor
     * for which cashier records were successfully made
     * @return
     */
	public int getNumberProcessed();
	/**
     * Returns the number of beans processed by the visitor
     * @return
     */
	public int getNumberSuccessfullyProcessed();
    /**
     * Returns the number of beans visited by the visitor
     * @return
     */
    public int getNumberVisited();
}
