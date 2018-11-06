/**
 * @file	Update_DB.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	The main process of update thread
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 */
package nvddbupdater;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.sql.Connection;
import java.util.Date;

/**
 * @class	Update_DB
 * @brief	This class update the tables and create modified tables
 * @warning	Check ZipTagXml.java and referenced libraries
 */
public class UpdateDBT {
	/**
	 * @brief	Get modified data from nvd and upload, update DB tables
	 * @param	lastyear
	 * 			type: integer
	 * 			The year when the last update was done
	 * @param	newyear
	 * 			type: integer
	 * 			The year when this method is called
	 * @throws	Exception
	 */
	public void update(int lastyear, int newyear, boolean test) throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		DBUploader uploaderDB = new DBUploader();
		
		Logwriter logwriter = new Logwriter();
		System.out.println(" ");
		try {
			File dirLog = new File("./"+ZipTagXml.log);
			if(!dirLog.exists()) {
				dirLog.mkdir();
				System.out.println(" Log directory is created");
			} 
			else {
				// nothing to do 
			}
		} catch (Exception e) {
			System.out.println(" Cannot find or create Log directory");
			throw e;
		}
		
		Date date = new Date();
		System.out.println(" "+dateFormat.format(date) + " DB updater starts.");
		try {
			if (!test) {
				logwriter.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" DB updater starts.");
			} else {
				logwriter.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" DB updater starts for test.");
			}
		} catch (Exception e) {
			System.out.println(" Cannot write log.");
			throw e;
		}
		if (!test) {
			try {
				GetData.getData("nvdcve-modified.xml.zip");
				GetData.makeTranslatedFile("nvdcve-modified");
			} catch (Exception e) {
				System.out.println(" Get modified data failed");
				throw e;
			}
		}
		Connection conn = null;
		try {
			conn = uploaderDB.connectToDB(conn);
			uploaderDB.uploadModifiedBase(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_base.xml",lastyear != newyear, newyear);
			uploaderDB.uploadModifiedRefs(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_refs.xml",lastyear != newyear, newyear);
			uploaderDB.uploadModifiedVuln(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_vuln.xml",lastyear != newyear, newyear);
			uploaderDB.disconnectDB(conn);
			
			try {
				Date date2 = new Date();
				if(!test) {
					logwriter.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date2) +" Update complete.");
					System.out.println(" ");
					System.out.println(" "+dateFormat.format(date2) +" Update complete.");
				} 
				else {
					logwriter.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date2) +" Test Update complete.");
					System.out.println(" ");
					System.out.println(" "+dateFormat.format(date2) +" Test Update complete.");
				}
			} catch (Exception e) {
				throw e;
			} 
		} catch (Exception e){
			System.out.println(" NVD update failed");
			try {
				Date date2 = new Date();
				if(!test) {
					logwriter.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date2) +" Update failed.");
					System.out.println(" ");
					System.out.println(" "+dateFormat.format(date2) +" Update failed.");
				} 
				else {
					logwriter.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date2) +" Test Update failed.");
					System.out.println(" ");
					System.out.println(" "+dateFormat.format(date2) +" Test Update failed.");
				}
			} catch (Exception e2) {
				System.out.println(" Cannot write log.");
				throw e2;
			} 
			
			throw e;
		} 
	}
}