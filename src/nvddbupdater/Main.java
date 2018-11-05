/**
 * @file	Main.java
 * @data	2018/11/02
 * @author	skt.1519040
 * @brief	Make NVD DB tables and update
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 */


package nvddbupdater;


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

	/**
	 * @brief	Main process
	 * @param	args
	 * @throws	Exception
	 */
	public static void main(String[] args) {
		int chs = 1;
		if(args.length > 0) {
			if(args[0] == "1") {
				chs = 1;
			} else if(args[0] == "2") {
				chs = 2;
			} else if(args[0] == "3") {
				chs = 3;
			}
			
		}
		else {
		/// Get option from user. If option is 1, initialize DB and set new DB tables
		/// If Option is 2, skip the initialization and run update thread
		
			Scanner inputline = new Scanner(System.in);
			System.out.println(" ________________________________________________\n" +
					           "| NVD DB Update                                  |\n" +
					           "| 1. Initialize all tables and update new tables |\n" +
					           "| 2. Update only modified data                   |\n" +
					           "| 3. Testing                                     |\n" +
					           "|________________________________________________|\n");
			System.out.print(" Choose number: ");
			String msg = inputline.nextLine();
			
			try {
				chs = Integer.parseInt(msg);
				if (chs == 1) {
					System.out.println(" Initialize all tables ");
				}
				else if (chs == 2) {
					if (!ztx.dir_exist()) {
						System.out.println(" There is no directory. Please intialize all tables first.");
						inputline.close();
						System.exit(1);
					}
					else {
						System.out.print(" If you do not update for long time from last update, you may not be able to update all data modified before. \n" +
					                       " Start only update anyway? : (y/n) ");
						msg = inputline.nextLine();
						if (msg.equals("y")||msg.equals("Y")) {
							// nothing to do
						}
						else if (msg.equals("n")||msg.equals("N")) {
							System.out.println(" Stop ");
							inputline.close();
							System.exit(1);
						}
						else {
							System.out.println(" Wrong input: " + msg);
							inputline.close();
							System.exit(1);
						}
					}
					
				}
				else if(chs == 3) {
					if (!ztx.dir_exist()) {
						System.out.println(" There is no directory. Please intialize all tables first.");
						inputline.close();
						System.exit(1);
					}
					else {
						///Testing
					}
				}
				else {
					System.out.println(" You choose wrong one: " + chs);
					inputline.close();
					System.exit(1);
				}
			} catch (Exception e) {
				System.out.println(" You choose wrong one: " + msg);
				inputline.close();
				System.exit(1);
			} finally {
				inputline.close();	
			}
		}

		
		
		
		if (chs == 1) {
			/// Drop all of the table made before and create new table for 2002 ~ thisyear CVEs
			try {
				Init_N_Setdb();
				Run_update_thread(false);
			} catch (Exception e) {
				System.out.println("Initialization failed");
			} finally {
				System.out.println(" ...");
			}
			/// Update CVEs using modified files
		}
		else if(chs == 2) {
			System.out.println(" ");
			System.out.println(" Start Update only");
			/// Update CVEs using modified files
			
			Run_update_thread(false);
			
		}
		else if(chs == 3) {
			/// Testing
			System.out.println(" ");
			System.out.println(" Start Testing");
			
			Run_update_thread(true);
			
			
		}
		else {
			System.out.println(" ");
			System.out.println(" Error!");
		}
		
	}
	

	/**
	 * @brief	Drop all tables on NVD DB and upload new data from NVD data feed
	 * @throws	Exception
	 */
	private static void Init_N_Setdb () throws Exception {
		Date date = new Date();
		DBUploader uploader_db = new DBUploader();
		Logwriter lw = new Logwriter();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		int thisyear = Calendar.getInstance().get(Calendar.YEAR);
		ztx.make_dir();
		
		try {
			lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" Start initializing NVD.");
			
		} catch (Exception e) {
			System.out.println(" Cannot write log.");
			throw e;
		} 
		System.out.println(" ");
		System.out.println(" "+dateFormat.format(date) +" Start initializing NVD.");
		
		for(int i = 2002; i <= thisyear; i++) {
			try {
				Get_data.get_data("nvdcve-"+i+".xml.zip");
				Get_data.make_translated_file("nvdcve-"+i);
			} catch (Exception e) {
				date = new Date();
				try {
					lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" Get_data for initialization failed.");
				} catch (Exception e2) {
					System.out.println(" Cannot write log.");
					throw e2;
				}
				System.out.println(" "+dateFormat.format(date) + " Get_Data for initialization failed.");
				System.out.println(" ");
				throw e;
			}
		}
		System.out.println(" ");
		try {	
			uploader_db.connect_to_DB();
		
			for(int i = 2002; i <= thisyear ; i++) {
				uploader_db.init_and_upload_base("./"+ZipTagXml.translated+"/nvdcve-"+i+"_base.xml");
				uploader_db.init_and_upload_refs("./"+ZipTagXml.translated+"/nvdcve-"+i+"_refs.xml");
				uploader_db.init_and_upload_vuln("./"+ZipTagXml.translated+"/nvdcve-"+i+"_vuln.xml");
				
			}
			uploader_db.set_testing_table();
			uploader_db.disconnect_DB();
			date = new Date();
			try {
				lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" All NVD tables are Dropped and created.");
				
			} catch (Exception e) {
				System.out.println(" Cannot write log.");
				throw e;
			}
		} catch (Exception e) {
			date = new Date();
			try {
				lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" Connection to DB for initialization failed.");
			} catch (Exception e2) {
				System.out.println(" Cannot write log.");
				throw e2;
			}
			
			throw e;
		}
		
		System.out.println(" "+dateFormat.format(date) + " All NVD tables are Dropped and created.");
		System.out.println(" ");

	}


	/**
	 * @brief	Make new update thread and run it. This Thread will run every 4 a.m. If you want to change the time, edit the variable 'update_time_24'
	 * @throws	Exception
	 */
	private static void Run_update_thread (boolean test) {
		DateFormat s_dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Update_DB upd = new Update_DB();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final long minute = (long) 60000; // 1 minute
		final long timeInterval = 24*60*minute;	// 1 day
		Runnable runnable = new Runnable() {
	
			public void run() {
				Calendar cal = Calendar.getInstance();
				Logwriter lw = new Logwriter();
				Date date = null;
				int lastyear = cal.get(Calendar.YEAR);
				int newyear = 0;
				int update_time_24 = 4; // the time when update begin. if update_time_24 = 4, it starts at 4am. if update_time_24 = 18, it starts at 6pm.
				int hour_t = 3600000;
				while (!Thread.currentThread().isInterrupted()) {

					try {
						newyear = Calendar.getInstance().get(Calendar.YEAR);
						upd.update(lastyear,newyear,test);						
					} catch (Exception e) {
						System.out.println(" Cannot update modified data.");
						date = new Date();
						try {
							lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" Update failed. (Update DB failed)");
							
						} catch (Exception e2) {
							System.out.println(" Cannot write log.");
							Thread.currentThread().interrupt();
						}
						
						Thread.currentThread().interrupt();
					}
					try {
						lastyear = newyear;
						cal = Calendar.getInstance();
						Date date2 = cal.getTime();
						try {
							cal.setTime(s_dateFormat.parse(s_dateFormat.format(date2)));
						} catch (Exception e) {
							date = new Date();
							try {
								lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" Thread could not sleep. (Failed to set time)");
								
							} catch (Exception e2) {
								System.out.println(" Cannot write log.");
								Thread.currentThread().interrupt();
							}
							
							Thread.currentThread().interrupt();
						}
						date2 = cal.getTime();
						Date date3 = new Date();
						
						System.out.println(" Updater sleeps.");
						if (!test) {
							System.out.println(" Updater will run at "+update_time_24+":00\n");
							Thread.sleep(timeInterval-(date3.getTime()-date2.getTime())+update_time_24*hour_t); // thread will awake at midnight(04:00)
						} else {
							System.out.println(" Updater will run 3 minute later\n");
							Thread.sleep((long) 180000);
							
						}
					} catch (InterruptedException e) {
						date = new Date();
						try {
							lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" Thread could not sleep. (Failed to call sleep method)");
							
						} catch (Exception e2) {
							System.out.println(" Cannot write log.");
							Thread.currentThread().interrupt();
						}
						System.out.println(" Quit update thread");
						Thread.currentThread().interrupt();
					}
				
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();	

	}
}
