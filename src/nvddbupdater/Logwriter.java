/**
 * @file	Logwriter.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	Write log file(txt)
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 */
package nvddbupdater;

import java.io.PrintWriter;
import java.io.FileWriter;
/**
 * @class	Logwriter
 * @brief	Open txt file, and write log down
 * @warning	NULL
 */
public class Logwriter {
	/**
	 * @brief	Open txt file, and write log down
	 * @param	fpath
	 * 			type: String
	 * 			The file path of log file
	 * @param	logstring
	 * 			type: String
	 * 			The contents of log
	 * @throws	Exception
	 */
	public void writeFile(String fpath, String logstring) throws Exception {
		
		try (PrintWriter fw = new PrintWriter(new FileWriter(fpath,true))){
			fw.println(logstring);
		} catch (Exception e) {
			System.out.println(" logwrite failed");
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	public void writeConsole(String logstring) {
		System.out.println(logstring);
	}
}