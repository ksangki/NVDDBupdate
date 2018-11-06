/**
 * @file	Main.java
 * @data	2018/11/02
 * @author	skt.1519040
 * @brief	Make NVD DB tables and update
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 */


package nvddbupdater;


import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/**
 * @class	Main
 * @brief	Main process class
 * @warning	Check ZipTagXml.java first
 */
public class Main {
	static ZipTagXml ztx = new ZipTagXml();
	static Logwriter logwriter = new Logwriter();
	static String nLogFile = "/log.txt";
	static String logError = " Cannot write log.";
	/**
	 * @brief	Main process
	 * @param	args
	 * @throws	Exception
	 */
	public static void main(String[] args) {
		int executeOption = 1;
		if(args.length > 0) {
			if(args[0] == "1") {
				executeOption = 1;
			} else if(args[0] == "2") {
				executeOption = 2;
			} else if(args[0] == "3") {
				executeOption = 3;
			}
			
		}
		else {
		/// Get option from user. If option is 1, initialize DB and set new DB tables
		/// If Option is 2, skip the initialization and run update thread
		
			Scanner inputLine = new Scanner(System.in);
			logwriter.writeConsole(" ________________________________________________\n" +
					           "| NVD DB Update                                  |\n" +
					           "| 1. Initialize all tables and update new tables |\n" +
					           "| 2. Update only modified data                   |\n" +
					           "| 3. Testing                                     |\n" +
					           "|________________________________________________|\n");
			System.out.print(" Choose number: ");
			String msg = inputLine.nextLine();
			
			try {
				executeOption = Integer.parseInt(msg);
				if (executeOption == 1) {
					logwriter.writeConsole(" Initialize all tables ");
				}
				else if (executeOption == 2) {
					if (!ztx.dirExist()) {
						logwriter.writeConsole(" There is no directory. Please intialize all tables first.");
						inputLine.close();
						System.exit(1);
					}
					else {
						System.out.print(" If you do not update for long time from last update, you may not be able to update all data modified before. \n" +
					                       " Start only update anyway? : (y/n) ");
						msg = inputLine.nextLine();
						if (msg.equals("y")||msg.equals("Y")) {
							// nothing to do
						}
						else if (msg.equals("n")||msg.equals("N")) {
							logwriter.writeConsole(" Stop ");
							inputLine.close();
							System.exit(1);
						}
						else {
							logwriter.writeConsole(" Wrong input: " + msg);
							inputLine.close();
							System.exit(1);
						}
					}
					
				}
				else if(executeOption == 3) {
					if (!ztx.dirExist()) {
						logwriter.writeConsole(" There is no directory. Please intialize all tables first.");
						inputLine.close();
						System.exit(1);
					}
					else {
						///Testing
					}
				}
				else {
					logwriter.writeConsole(" You choose wrong one: " + executeOption);
					inputLine.close();
					System.exit(1);
				}
			} catch (Exception e) {
				logwriter.writeConsole(" You choose wrong one: " + msg);
				inputLine.close();
				System.exit(1);
			} finally {
				inputLine.close();	
			}
		}

		
		
		
		if (executeOption == 1) {
			/// Drop all of the table made before and create new table for 2002 ~ thisyear CVEs
			try {
				initNSet();
				runUpdateThread(false);
			} catch (Exception e) {
				logwriter.writeConsole("Initialization failed");
			} finally {
				logwriter.writeConsole(" ...");
			}
			/// Update CVEs using modified files
		}
		else if(executeOption == 2) {
			logwriter.writeConsole(" ");
			logwriter.writeConsole(" Start Update only");
			/// Update CVEs using modified files
			
			runUpdateThread(false);
			
		}
		else if(executeOption == 3) {
			/// Testing
			logwriter.writeConsole(" ");
			logwriter.writeConsole(" Start Testing");
			
			runUpdateThread(true);
			
			
		}
		else {
			logwriter.writeConsole(" ");
			logwriter.writeConsole(" Error!");
		}
		
	}
	

	/**
	 * @brief	Drop all tables on NVD DB and upload new data from NVD data feed
	 * @throws	Exception
	 */
	private static void initNSet () throws Exception {
		Date date = new Date();
		DBUploader uploaderDB = new DBUploader();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		int thisyear = Calendar.getInstance().get(Calendar.YEAR);
		ztx.makeDir();
		if(!writeFileLog("./"+ZipTagXml.log+nLogFile,dateFormat.format(date) +" Start initializing NVD.")) {
			throw new Exception();
		}
		
		logwriter.writeConsole(" ");
		logwriter.writeConsole(" "+dateFormat.format(date) +" Start initializing NVD.");
		
		for(int i = 2002; i <= thisyear; i++) {
			try {
				GetData.getData("nvdcve-"+i+".xml.zip");
				GetData.makeTranslatedFile("nvdcve-"+i);
			} catch (Exception e) {
				date = new Date();
				writeFileLog("./"+ZipTagXml.log+nLogFile,dateFormat.format(date) +" Get_data for initialization failed.");
				logwriter.writeConsole(" "+dateFormat.format(date) + " Get_Data for initialization failed.");
				logwriter.writeConsole(" ");
				throw e;
			}
		}
		logwriter.writeConsole(" ");
		Connection conn = null;
		try {	
			conn = uploaderDB.connectToDB(conn);
		
			for(int i = 2002; i <= thisyear ; i++) {
				uploaderDB.initNUploadBase(conn,"./"+ZipTagXml.translated+"/nvdcve-"+i+"_base.xml");
				uploaderDB.initNUploadRefs(conn,"./"+ZipTagXml.translated+"/nvdcve-"+i+"_refs.xml");
				uploaderDB.initNUploadVuln(conn,"./"+ZipTagXml.translated+"/nvdcve-"+i+"_vuln.xml");
				
			}
			uploaderDB.setTestingTable(conn);
			uploaderDB.disconnectDB(conn);
			date = new Date();
			try {
				logwriter.writeFile("./"+ZipTagXml.log+nLogFile,dateFormat.format(date) +" All NVD tables are Dropped and created.");
				
			} catch (Exception e) {
				logwriter.writeConsole(logError);
				throw e;
			}
		} catch (Exception e) {
			date = new Date();
			writeFileLog("./"+ZipTagXml.log+nLogFile,dateFormat.format(date) +" Get_data for initialization failed.");
			throw e;
		}
		
		logwriter.writeConsole(" "+dateFormat.format(date) + " All NVD tables are Dropped and created.");
		logwriter.writeConsole(" ");

	}


	/**
	 * @brief	Make new update thread and run it. This Thread will run every 4 a.m. If you want to change the time, edit the variable 'update_time_24'
	 * @throws	Exception
	 */
	private static void runUpdateThread (boolean test) {
		DateFormat sDataform = new SimpleDateFormat("yyyy/MM/dd");
		UpdateDBT updaterDB = new UpdateDBT();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final long minute = (long) 60000; // 1 minute
		final long timeInterval = 24*60*minute;	// 1 day
		Runnable runnable = new Runnable() {
	
			public void run() {
				Calendar timeIndicator = Calendar.getInstance();
				
				Date date = null;
				int lastyear = timeIndicator.get(Calendar.YEAR);
				int newyear = 0;
				int updateTime24 = 4; // the time when update begin. if update_time_24 = 4, it starts at 4am. if update_time_24 = 18, it starts at 6pm.
				int anHour = 3600000;
				while (!Thread.currentThread().isInterrupted()) {

					try {
						newyear = Calendar.getInstance().get(Calendar.YEAR);
						updaterDB.update(lastyear,newyear,test);						
					} catch (Exception e) {
						logwriter.writeConsole(" Cannot update modified data.");
						date = new Date();
						writeFileLog("./"+ZipTagXml.log+nLogFile,dateFormat.format(date) +" Update failed. (Update DB failed)");
						Thread.currentThread().interrupt();
					}
					try {
						lastyear = newyear;
						timeIndicator = Calendar.getInstance();
						Date date2 = timeIndicator.getTime();
						try {
							timeIndicator.setTime(sDataform.parse(sDataform.format(date2)));
						} catch (Exception e) {
							date = new Date();
							writeFileLog("./"+ZipTagXml.log+nLogFile,dateFormat.format(date) +" Thread could not sleep. (Failed to set time)");
							Thread.currentThread().interrupt();
						}
						date2 = timeIndicator.getTime();
						Date date3 = new Date();
						
						logwriter.writeConsole(" Updater sleeps.");
						if (!test) {
							logwriter.writeConsole(" Updater will run at "+updateTime24+":00\n");
							Thread.sleep(timeInterval-(date3.getTime()-date2.getTime())+updateTime24*anHour); // thread will awake at midnight(04:00)
						} else {
							logwriter.writeConsole(" Updater will run 3 minute later\n");
							Thread.sleep((long) 180000);
							
						}
					} catch (InterruptedException e) {
						date = new Date();
						writeFileLog("./"+ZipTagXml.log+nLogFile,dateFormat.format(date) +" Thread could not sleep. (Failed to call sleep method)");
						logwriter.writeConsole(" Quit update thread");
						Thread.currentThread().interrupt();
					}
				
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();	

	}
	
	private static boolean writeFileLog (String fpath, String log){
		try {
			logwriter.writeFile(fpath,log);
			return true;
		} catch (Exception e) {
			logwriter.writeConsole(logError);
			return false;
		}
	}
}
