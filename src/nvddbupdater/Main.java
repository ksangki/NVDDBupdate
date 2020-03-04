/**
 * @file	Main.java
 * @data	2018/11/02
 * @author	skt.1519040
 * @brief	Make NVD DB tables and update
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 * 	- 2018. 12.	14	Joh Rang Hyun	StAX version
 */


package nvddbupdater;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * @class	Main
 * @brief	Main process class
 * @warning	Check ZipTagXml.java first
 */
public class Main {
	static ZipTagXml ztx = new ZipTagXml();
	
	static String nLogFile = "/log.txt";
	static String pLog = "./"+ZipTagXml.log+nLogFile;
	static String logError = " Cannot write log.";
	static String nvdPrefix = "/nvdcve-";
	/**
	 * @brief	Main process
	 * @param	args
	 * @throws	Exception
	 */
	public static void main(String[] args) {
		int executeOption = 1;
		Logwriter.logger.addHandler(Logwriter.getHandler());
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
			Logwriter.writeConsole(" ________________________________________________\n" +
					           "| NVD DB Update                                  |\n" +
					           "| 1. Initialize all tables and update new tables |\n" +
					           "| 2. Update only modified data                   |\n" +
					           "| 3. Testing                                     |\n" +
					           "|________________________________________________|\n");
			Logwriter.writeConsole(" Choose number: ");
			String msg = inputLine.nextLine();
			
			try {
				executeOption = Integer.parseInt(msg);
				if (executeOption == 1) {
					Logwriter.writeConsole(" Initialize all tables\n");
				}
				else if (executeOption == 2) {
					if (!ztx.dirExist()) {
						Logwriter.writeConsole(" There is no directory. Please intialize all tables first.\n");
						inputLine.close();
						System.exit(1);
					}
					else {
						Logwriter.writeConsole(" If you do not update for long time from last update, you may not be able to update all data modified before. \n" +
					                       " Start only update anyway? : (y/n) ");
						msg = inputLine.nextLine();
						if (msg.equals("y")||msg.equals("Y")) {
							// nothing to do
						}
						else if (msg.equals("n")||msg.equals("N")) {
							Logwriter.writeConsole(" Stop\n");
							inputLine.close();
							System.exit(1);
						}
						else {
							Logwriter.writeConsole(" Wrong input: " + msg+"\n");
							inputLine.close();
							System.exit(1);
						}
					}
					
				}
				else if(executeOption == 3) {
					if (!ztx.dirExist()) {
						Logwriter.writeConsole(" There is no directory. Please intialize all tables first.\n");
						inputLine.close();
						System.exit(1);
					}
					else {
						///Testing
					}
				}
				else {
					Logwriter.writeConsole(" You choose wrong one: " + executeOption+"\n");
					inputLine.close();
					System.exit(1);
				}
			} catch (Exception e) {
				Logwriter.writeConsole(" You choose wrong one: " + msg+"\n");
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
				Logwriter.writeConsole(" Initialization failed\n"+e);
			} finally {
				Logwriter.writeConsole(" ...");
			}
			/// Update CVEs using modified files
		}
		else if(executeOption == 2) {
			Logwriter.writeConsole(" \n");
			Logwriter.writeConsole(" Start Update only\n");
			/// Update CVEs using modified files
			
			runUpdateThread(false);
			
		}
		else if(executeOption == 3) {
			/// Testing
			Logwriter.writeConsole(" \n");
			Logwriter.writeConsole(" Start Testing\n");
			
			runUpdateThread(true);
			
			
		}
		else {
			Logwriter.writeConsole(" \n");
			Logwriter.writeConsole(" Error!\n");
		}
		
	}
	
	/**
	 * @brief	Drop all tables on NVD DB and upload new data from NVD data feed
	 * @throws	IOException
	 * @throws	SQLException
	 */
	private static void initNSet () throws IOException, SQLException {
		Date date = new Date();
		DBUploader uploaderDB = new DBUploader();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		int thisyear = Calendar.getInstance().get(Calendar.YEAR);
//		int thisyear = 2002;
		ztx.makeDir();
		if(!writeFileLog(pLog,dateFormat.format(date) +" Start initializing NVD.\n")) {
			throw new IOException();
		}
		
		Logwriter.writeConsole(" \n");
		Logwriter.writeConsole(" "+dateFormat.format(date) +" Start initializing NVD.\n");
		Logwriter.writeConsole(" \n");
		for(int i = 2002; i <= thisyear; i++) {
			try {
				//GetData.getData("nvdcve-"+i+".xml.zip");
				GetData.getData("nvdcve-1.1-"+i+".json.zip");
				
			} catch (IOException e) {
				date = new Date();
				writeFileLog(pLog,dateFormat.format(date) +" getData for initialization failed.\n");
				Logwriter.writeConsole(" "+dateFormat.format(date) + " getData for initialization failed.\n");
				Logwriter.writeConsole(" \n");
				throw e;
			}
		}
		Logwriter.writeConsole(" \n");
		Logwriter.writeConsole(" here");
		for(int i = 2002; i <= thisyear; i++) {
			Logwriter.writeConsole(" here");
			if (GetData.makeTranslatedFile("nvdcve-"+i)) {
				Logwriter.writeConsole(" Success Translate nvdcve-"+i+" to nvdcve-"+i+" base, refs, vuln file.\n");
			} else {
				date = new Date();
				writeFileLog(pLog,dateFormat.format(date) +" makeTranslatedFile for initialization failed.\n");
				Logwriter.writeConsole(" "+dateFormat.format(date) + " makeTranslatedFile for initialization failed.\n");
				Logwriter.writeConsole(" \n");
				System.exit(1);
			}
		}
		Logwriter.writeConsole(" \n");
		
		try {	
			Connection conn = uploaderDB.connectToDB();
		
			for(int i = 2002; i <= thisyear ; i++) {
				uploaderDB.initNUploadBase(conn,"./"+ZipTagXml.translated+nvdPrefix+i+"_base.xml");
				uploaderDB.initNUploadRefs(conn,"./"+ZipTagXml.translated+nvdPrefix+i+"_refs.xml");
				uploaderDB.initNUploadVuln(conn,"./"+ZipTagXml.translated+nvdPrefix+i+"_vuln.xml");
				
			}
			uploaderDB.setTestingTable(conn);
			conn.close();
			date = new Date();
			writeFileLog(pLog,dateFormat.format(date) +" All NVD tables are Dropped and created.");
			
		} catch (SQLException e) {
			date = new Date();
			writeFileLog(pLog,dateFormat.format(date) +" Cannot connect to DB.");
			throw e;
		} 
		
		Logwriter.writeConsole(" "+dateFormat.format(date) + " All NVD tables are Dropped and created.\n");
		Logwriter.writeConsole(" \n");

	}


	
	/**
	 * @brief	Make new update thread and run it. This Thread will run every 4 a.m. If you want to change the time, edit the variable 'update_time_24'
	 * @param	test
	 * 			type: boolean
	 * 			true if this process is executed for test. if not, false
	 */
	private static void runUpdateThread (boolean test) {
		DateFormat sDataform = new SimpleDateFormat("yyyy/MM/dd");
		UpdateDBT updaterDB = new UpdateDBT();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final long minute = (long) 60000; // 1 minute
		final long timeInterval = 24*60*minute;	// 1 day
		new Thread(()->{
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
				} catch (SQLException|IOException|SAXException|ParserConfigurationException|TransformerException|ClassNotFoundException e) {
					Logwriter.writeConsole(" Cannot update modified data.\n");
					date = new Date();
					writeFileLog(pLog,dateFormat.format(date) +" Update failed. (Update DB failed)");
					Thread.currentThread().interrupt();
				}
				
				lastyear = newyear;
				timeIndicator = Calendar.getInstance();
				Date date2 = timeIndicator.getTime();
				try {
					timeIndicator.setTime(sDataform.parse(sDataform.format(date2)));
				} catch (java.text.ParseException e) {
					date = new Date();
					writeFileLog(pLog,dateFormat.format(date) +" Thread could not sleep. (Failed to set time)");
					Thread.currentThread().interrupt();
				}
				date2 = timeIndicator.getTime();
				Date date3 = new Date();
				
				Logwriter.writeConsole(" Updater sleeps.\n");
				try {
					if (!test) {
						Logwriter.writeConsole(" Updater will run at "+updateTime24+":00\n");
						Thread.sleep(timeInterval-(date3.getTime()-date2.getTime())+updateTime24*anHour); // thread will awake at midnight(04:00)
					} else {
						Logwriter.writeConsole(" Updater will run 3 minute later\n");
						Thread.sleep((long) 180000);
						
					}
				} catch (InterruptedException e) {
					date = new Date();
					writeFileLog(pLog,dateFormat.format(date) +" Thread could not sleep. (Failed to call sleep method)");
					Logwriter.writeConsole(" Quit update thread\n");
					Thread.currentThread().interrupt();
				}
			
			}
		}).start();	

	}
	
	/**
	 * @brief	write log message into file by logger
	 * @param	fpath
	 * 			type: String
	 * 			file path of txt log file
	 * @param	log
	 * 			type: String
	 * 			log message
	 * @return	true if success. if not, false
	 */
	private static boolean writeFileLog (String fpath, String log){
		try {
			Logwriter.writeFile(fpath,log);
			return true;
		} catch (Exception e) {
			Logwriter.writeConsole(logError);
			return false;
		}
	}
}
