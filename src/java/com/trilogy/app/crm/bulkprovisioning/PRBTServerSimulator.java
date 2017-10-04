




package com.trilogy.app.crm.bulkprovisioning;
/*
 * Created 2005-09-08
 * run this java -Djava.ext.dirs=. com.redknee.app.crm.bulkprovisioning.PRBTServerSimulator 5500 0 600
 */

import java.io.*;
import java.net.*;
import java.util.*;
/**
 * 
 * 
 * 
 * This class simulates quite a few PRBT Servers exchanging network messages
 * 
 * Usage: java -Djava.ext.dirs=. com.redknee.app.crm.bulkprovisioning.PRBTServerSimulator-
 *  (Note: make sure you are in the current/lib directory)
 */

public class PRBTServerSimulator 
{
	
	private ServerSocket 	ListenAndAccepter;
	private Vector 			SocketList;
		
	public PRBTServerSimulator(int port, int timeout, int idleTimeout) throws Exception
	{
		SocketList = new Vector();
		
		try 
		{
			ListenAndAccepter = new ServerSocket(port, 5);
			System.out.println("PRBT Server started ..."); 
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return;
		}
		
		while(true)
		{
			Socket singleSocket;
			PRBT_Server_Connection connection; 
		    try 
		    {
			    singleSocket = ListenAndAccepter.accept();
			    System.out.println("ListenAndAccepter.accept()");
			    connection = new PRBT_Server_Connection(singleSocket, port, timeout, idleTimeout );  
			    connection.start();
		    } 
		    catch (Exception e) 
		    {
			    e.printStackTrace();
		    }
		}	
	}


		
	/**
	 * 
	 * @author rdcsobo
	 *
	 * This class simulates a connection with an EMA server.
	 */
	private class PRBT_Server_Connection extends Thread
	{
		private boolean myTimeout = false;
		private boolean myMMLClosed = false;
		private boolean myLinkReading = false;
		private int myIdleTime = 0;
		private APG_TIME_OUT_Timing myTimingThread;
		
		private class APG_TIME_OUT_Timing extends Thread
		{
			private int myTimeoutLength;
			APG_TIME_OUT_Timing ( int sec )
			{
				myTimeoutLength = sec;
			}
			
			public void run()
			{
				myIdleTime = 0;
				while ( myMMLClosed == false )
				{
					
					try 
					{
						Thread.sleep( 1000 );
					} 
					catch (InterruptedException e) 
					{
							// TODO Auto-generated catch block
							e.printStackTrace();
					}
					
					
						ToTimeout();
					
				}
				System.out.println("PRBT Timeout timing thread will exit...\n");
			}
			
			synchronized void ToTimeout()
			{
				if( myTimeout == false && myLinkReading == true )
				{
					myIdleTime += 1;
					System.out.println("PRBT Timeout timing...");
					System.out.print("The idle time is: " );
					System.out.println( myIdleTime );
					if( myIdleTime >= myTimeoutLength )
					{
						System.out.println("PRBT Timeout exceeded.\n");
						myTimeout = true;
						myBufferedString += "\r\n\r\nTIME OUT\r\n\r\n";
						
					}
				}
			}
		}
		
		private boolean myLinkClosed = false;
		private String myBufferedString;
		private Buffered_Printer myBufferedPrinter;
		private class Buffered_Printer extends Thread
		{
			private int myTimer;
			
			Buffered_Printer ()
			{
				myBufferedString = new String("");
			}
			
			public void run()
			{
				myTimer = 0;
				while ( myLinkClosed == false )
				{
					try 
					{
						Thread.sleep( 100 );
					} 
					catch (InterruptedException e) 
					{
							// TODO Auto-generated catch block
							e.printStackTrace();
					}
					toPrintBufferedString();
				}
				System.out.println("PRBT Buffered Printer thread will exit...\n");
			}
			
			synchronized void toPrintBufferedString()
			{
				if( myBufferedString.length() == 0 )
				{
					myTimer = 0;
				}
				else
				{
					myTimer += 1;
				}
				
				if( myBufferedString.length() > 8 || myTimer >= 5 )
				{
					pw.print( myBufferedString );
					pw.flush();
					System.out.print("String in buffer sent out: ");
					System.out.println( myBufferedString );
					myBufferedString = "";
					myTimer = 0;
				}
			}
		}
		
		
		private Socket LocalSocket=null;
		private PrintWriter pw;
		private BufferedReader br;
		private String prompt ="PRBT>";
	
		public PRBT_Server_Connection(Socket socket, int port, int timeout, int idleTimeout ) throws Exception
		{
			socket.setSoTimeout(timeout);
			LocalSocket = socket;
			pw = new PrintWriter(LocalSocket.getOutputStream(), true);
			br = new BufferedReader(new InputStreamReader(LocalSocket.getInputStream()));
			myTimingThread = new APG_TIME_OUT_Timing( idleTimeout );
			myBufferedPrinter = new Buffered_Printer();
		}
		

        public void Login_Procedure()
        {
            try
            {
                System.out.println("loging procedure!");
                StringBuffer loginString = new StringBuffer(
                        "Welcome to the Unified Personalized RingbackTone Provisioning Interface.");
                loginString.append("\r\n");
                loginString.append("Provisioning Interface version 2.01");
                loginString.append("\r\n");
                loginString.append("Please input your username and password to proceed.");
                loginString.append("\r\n");
                log(loginString.toString());
                pw.println(loginString.toString());
                pw.flush();
                pw.println("Login:");
                pw.flush();
                String username = readLine(br);
                log("Login: " + username);
                
                pw.println("Password:");
                pw.flush();
                String password = readLine(br);
                log("Password: " + password);
                log("Login Successful.");
                pw.println("Login Successful.");
                pw.flush();
            }
            catch (Exception ex)
            {
                log(" Unable to authenicate on PRBT server ", ex);
            }
        }
        public String readLine(BufferedReader reader)
        throws IOException
        {
            String ret = reader.readLine(); 
            while(ret != null && ret.trim().length() < 1)
            {
                ret = reader.readLine();   
            }
            return ret; 
        }
        
        private void log(String message)
        {
            log(message, null);
        }

        private void log(String message, Throwable t)
        {
            System.out.println(message);
            if(t != null)
                t.printStackTrace();    
        }
		
		public String readLine_Procedure( int loopTimes )throws IOException
		{
		    log(prompt);
		    pw.print(prompt);
		    pw.flush();
			myLinkReading = true;
			
			String command = readLine(br);

				
			log("Received:" + command);
			
			return command;
		}
		
		public void APG_Wakeup()
		{
			System.out.println("PRBT awaken !");
			myIdleTime = 0;
			myTimeout = false;
			String response = "";
			response = "\r\nWO      HL2101 013/044/0300J11    AD-1017 TIME 070308 0854  PAGE    1";
			myBufferedString += response;
			
			try 
			{
				Thread.sleep( 200 );
			} 
			catch (InterruptedException e) 
			{
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			myBufferedString += "\r\n";
			myBufferedString += prompt;
		}
		
		
		

        public void handleProvision(String command)
        {
            System.out.println("PROVISION command received: \n" + command);
            String response = new String("");
            if (command.endsWith(("fail;")) || command.endsWith("FAIL;"))
            {
                long curTime = System.currentTimeMillis();
                
                if ( curTime % 3 == 0)
                {
                    response="0:MESSAGE_ROUTER_PROVISIONING_FAILED:Failed;\r\n";
                }
                else if ( curTime % 3 == 1)
                {
                    response="2:MESSAGE_ROUTER_CONNECTION_FAILED:Failed;\r\n";
                }
                else
                {
                    response="4:PROCESS_COMMANDUNDEFINED:Undefine or invalid command;\r\n";                    
                }
                    
            }
            else
            {
                response="0:PROCESS_OK:Commad Accepted;\r\n";
            }
            log(response);
            pw.print(response);
            pw.flush();
            
        }


        public void handleUnProvision(String command)
        {
            System.out.println("UNPROVISOIN command received: \n" + command);
            String response = "";
            if (command.endsWith(("fail")) || command.endsWith("FAIL"))
            {
                long curTime = System.currentTimeMillis();
                
                if ( curTime % 3 == 0)
                {
                    response="0:MESSAGE_ROUTER_PROVISIONING_FAILED:Failed";
                }
                else if ( curTime % 3 == 1)
                {
                    response="2:MESSAGE_ROUTER_CONNECTION_FAILED:Failed";                    
                }
                else
                {
                    response="4:PROCESS_COMMANDUNDEFINED:Undefine or invalid command;";                    
                }
                    
            }
            else
            {
                response= "0:PROCESS_OK:Commad Accepted;";
            }
            log(response);
            pw.print(response);
            pw.flush();
        }
		
		
		public void handle_NON_print_command ( String command )
		{
		    log("Noncommand_Received: " + command);
			log("PRBT>");
			try 
			{
				Thread.sleep( 100 );
			} 
			catch (InterruptedException e) 
			{
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			pw.print("PRBT>\r\n");
			pw.flush();
			
		}
		

        public void handle_EXIT(String command)
        {
            log("Command EXIT;\n");
            myMMLClosed = true;
            try
            {
                myTimingThread.join();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();            
            }
            
            myLinkClosed = true;
            try
            {
                myBufferedPrinter.join();
            }
            catch (InterruptedException e3)
            {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }
            try
            {
                pw.close();
                br.close();
                LocalSocket.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


		public void handle_EXIT_( String command )
		{
			log("EXITING \n");
			
			String response = "";
			response += "EXIT;";
			
			myBufferedString += response;
			
			try 
			{
				pw.close();
				br.close();
				LocalSocket.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
		}
		
		public void run()
		{
		//	myBufferedPrinter.start();
			Login_Procedure();
			//myTimingThread.start();
			
			String command = null;
			
			int loopTimes = 0;
		    while(true)
			{	
		    	try
		    	{
		    		command = readLine_Procedure( loopTimes );
		    	}
		    	catch (IOException e) 
				{
					e.printStackTrace();
					myMMLClosed = true;
					myLinkClosed = true;
			
					
					try 
					{
						pw.close();
						br.close();
						LocalSocket.close();
					} 
					catch (Exception e1) 
					{
						e1.printStackTrace();
					}
					System.err.println("Exception occorred, thread will terminate." + " Loop: " + loopTimes);
					break;
				}
				
		    	if( command == null )
				{
		    		try 
					{
						pw.close();
						br.close();
						LocalSocket.close();
					} 
					catch (Exception e1) 
					{
						e1.printStackTrace();
					}
					System.err.println ("Received command is null.");
					break;
				}
		    	
		    	
				if( myTimeout == true )
				{
					APG_Wakeup();
				}
				else
				{
					if(command.startsWith("exit") || command.startsWith("EXIT"))
					{
						handle_EXIT( command );
						break;
					}
					else if (command.startsWith("p_subs"))
					{
					    handleProvision(command);					    
					}
					else if (command.startsWith("r_sub"))
					{
					    handleUnProvision(command);
					}
					else if( command.length() == 0 )
					{
						//handle_NON_print_command( command );
						log( "Command received: \n" + command );
						String response = "";
						response +=	(char)0x0d; response += (char)0x0a;
					
						myBufferedString += response; 
						myBufferedString += prompt;
					
						log( "Response Package Sent: \n" + response );
					}
					else
					{
						handle_NON_print_command( command );
					}
				}

				loopTimes ++;
			}
		    
		    
			System.out.println( "Link closed: " + command );
		}
	}

	public static void printUsage()
	{
		System.out.println("Usage: java EmaServer [port] [timeout in second]  or");
		System.out.println("Usage: java EmaServer [port] or");
		System.out.println("Usage: java EmaServer");
		System.exit(1);
	}		
		
	public static void main(String args[])
	{

		int port 	= 52894;
		int timeout = 0;
		int idleTimeout = 40;
	
		if ( args.length > 3 )
		{
			printUsage();
		}
		if (args.length == 3)
		{
			try
			{
				port 	= Integer.parseInt(args[0]);
				timeout = Integer.parseInt(args[1]) * 1000;
				idleTimeout = Integer.parseInt(args[2]);
			}
			catch (Exception e)
			{
				System.out.println("Error on input parameter(s)...");
				printUsage();
			}

		}
		if (args.length == 2)
		{
			try
			{
				port 	= Integer.parseInt(args[0]);
				timeout = Integer.parseInt(args[1]) * 1000;
			}
			catch (Exception e)
			{
				System.out.println("Error on input parameter(s)...");
				printUsage();
			}

		}
		if (args.length == 1)
		{
			try
			{
				port 	= Integer.parseInt(args[0]);
			}
			catch (Exception e)
			{
				System.out.println("Error on input parameter(s)...");
				printUsage();
			}
		}
		
		try 
		{
			new PRBTServerSimulator(port, timeout, idleTimeout);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
