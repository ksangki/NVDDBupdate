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
public class Update_DB {
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
		DBUploader uploader_db = new DBUploader();
		
		Logwriter lw = new Logwriter();
		System.out.println(" ");
		try {
			File dir_log = new File("./"+ZipTagXml.log);
			if(!dir_log.exists()) {
				dir_log.mkdir();
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
				lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" DB updater starts.");
			} else {
				lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date) +" DB updater starts for test.");
			}
		} catch (Exception e) {
			System.out.println(" Cannot write log.");
			throw e;
		}
		if (!test) {
			try {
				Get_data.get_data("nvdcve-modified.xml.zip");
				Get_data.make_translated_file("nvdcve-modified");
			} catch (Exception e) {
				System.out.println(" Get modified data failed");
				throw e;
			}
		}
		Connection conn = null;
		try {
			conn = uploader_db.connect_to_DB(conn);
			uploader_db.upload_base_modified(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_base.xml",lastyear != newyear, newyear);
			uploader_db.upload_refs_modified(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_refs.xml",lastyear != newyear, newyear);
			uploader_db.upload_vuln_modified(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_vuln.xml",lastyear != newyear, newyear);
			uploader_db.disconnect_DB(conn);
			
			try {
				Date date2 = new Date();
				if(!test) {
					lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date2) +" Update complete.");
					System.out.println(" ");
					System.out.println(" "+dateFormat.format(date2) +" Update complete.");
				} 
				else {
					lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date2) +" Test Update complete.");
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
					lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date2) +" Update failed.");
					System.out.println(" ");
					System.out.println(" "+dateFormat.format(date2) +" Update failed.");
				} 
				else {
					lw.write("./"+ZipTagXml.log+"/log.txt",dateFormat.format(date2) +" Test Update failed.");
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