/**
 * @file	UpdateDBT.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	The main process of update thread
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 * 	- 2018. 11. 14	Joh Rang Hyun	Refactoring
 */
package nvddbupdater;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 * @class	UpdateDBT
 * @brief	This class update the tables and create modified tables
 * @warning	Check ZipTagXml.java and referenced libraries
 */
public class UpdateDBT {
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	String logFilePath = "log.txt";
	
	/**
	 * @brief	Get modified data from nvd and upload, update DB tables
	 * @param	lastyear
	 * 			type: int
	 * 			The year when the last update was done
	 * @param	newyear
	 * 			type: int
	 * 			The year when this method is called
	 * @param	test
	 * 			type: boolean
	 * 			if true, run test thread, if false, run update thread
	 * @throws	SQLException
	 * @throws	IOException
	 * @throws	SAXException
	 * @throws	ParserConfigurationException
	 * @throws	TransformerException
	 * @throws	ClassNotFoundException
	 */
	public void update(int lastyear, int newyear, boolean test) throws SQLException, IOException, SAXException, ParserConfigurationException, TransformerException, ClassNotFoundException {
		
		DBUploader uploaderDB = new DBUploader();
		
		
		Logwriter.writeConsole(" \n");
		try {
			File dirLog = new File("./"+ZipTagXml.log);
			if(!dirLog.exists()) {
				dirLog.mkdir();
				Logwriter.writeConsole(" Log directory is created\n");
			} 
			else {
				// nothing to do 
			}
		} catch (Exception e) {
			Logwriter.writeConsole(" Cannot find or create Log directory\n");
			throw e;
		}
		//StAX version
		Date date = new Date();
		Logwriter.writeConsole(" "+dateFormat.format(date) + " DB updater starts.\n");
		try {
			if (!test) {
				Logwriter.writeFile("./"+ZipTagXml.log+File.separator+logFilePath,dateFormat.format(date) +" DB updater starts.");
			} else {
				Logwriter.writeFile("./"+ZipTagXml.log+File.separator+logFilePath,dateFormat.format(date) +" DB updater starts for test.");
			}
		} catch (Exception e) {
			Logwriter.writeConsole(" Cannot write log.\n");
			throw e;
		}
		if (!test) {
			try {
				
				GetData.getData("nvdcve-1.1-modified.json.zip");
				GetData.makeTranslatedFile("nvdcve-modified");
			} catch (Exception e) {
				Logwriter.writeConsole(" Get modified data failed\n"+e);
				System.exit(1);
			}
		}
		
		try {
			Connection conn = uploaderDB.connectToDB();
			uploaderDB.uploadModifiedBase(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_base.xml",lastyear != newyear, newyear);
			uploaderDB.uploadModifiedRefs(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_refs.xml",lastyear != newyear, newyear);
			uploaderDB.uploadModifiedVuln(conn,"./" + ZipTagXml.translated + "/nvdcve-modified_vuln.xml",lastyear != newyear, newyear);
			conn.close();
			uploaderDB.deleteTempPost();
			Date date2 = new Date();
			writeLog(test, date2, true);
		} catch (Exception e){
			Logwriter.writeConsole(" NVD update failed\n"+e);
			Date date2 = new Date();
			writeLog(test, date2, false);
			System.exit(1);
		} 
	}
	
	/**
	 * @brief	write log
	 * @param	test
	 * 			type: boolean
	 * 			if true, write test log, if false, write update log
	 * @param	date2
	 * 			type: Date
	 * 			the date on which this method is called
	 * @param	success
	 * 			type: boolean
	 * 			if the process is successfully done, true. else, false
	 */
	public void writeLog (boolean test, Date date2, boolean success) {
		try {
			if(!test) {
				if (success) {
					Logwriter.writeFile("./"+ZipTagXml.log+File.separator+logFilePath,dateFormat.format(date2) +" Update complete.");
					Logwriter.writeConsole(" \n");
					Logwriter.writeConsole(" "+dateFormat.format(date2) +" Update complete.\n");
				} else {
					Logwriter.writeFile("./"+ZipTagXml.log+File.separator+logFilePath,dateFormat.format(date2) +" Update failed.");
					Logwriter.writeConsole(" \n");
					Logwriter.writeConsole(" "+dateFormat.format(date2) +" Update failed.\n");
				}
			} 
			else {
				if(success) {
					Logwriter.writeFile("./"+ZipTagXml.log+File.separator+logFilePath,dateFormat.format(date2) +" Test Update complete.");
					Logwriter.writeConsole(" \n");
					Logwriter.writeConsole(" "+dateFormat.format(date2) +" Test Update complete.\n");
				} else {
					Logwriter.writeFile("./"+ZipTagXml.log+File.separator+logFilePath,dateFormat.format(date2) +" Test Update failed.");
					Logwriter.writeConsole(" \n");
					Logwriter.writeConsole(" "+dateFormat.format(date2) +" Test Update failed.\n");
				}
			}
		} catch (Exception e) {
			Logwriter.writeConsole(" Cannot write log.\n");
		}
	}
}