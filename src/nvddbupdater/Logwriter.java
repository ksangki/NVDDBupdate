/**
 * @file	Logwriter.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	Write log file(txt)
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 * 	- 2018. 11. 14	Joh Rang Hyun	Refactoring
 */
package nvddbupdater;

import java.io.PrintWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.io.FileWriter;
/**
 * @class	Logwriter
 * @brief	write log on console or txt file
 * @warning	NULL
 */
public class Logwriter {
	
	public static final Logger logger = Logger.getGlobal();
	
	
	/**
	 * @brief	constructor of this class, nothing to do
	 */
	private Logwriter() {
		///nothing to do
	}
	
	/**
	 * @class	FormatTransfer
	 * @brief	make custom format of logger
	 * @warning	NULL
	 */
	static class FormatTransfer extends Formatter {
		private StringBuilder stringb = new StringBuilder();
		public String format(LogRecord logmsg) {
			stringb.setLength(0);
			String msg = formatMessage(logmsg);
			stringb.append(msg);
			
			return stringb.toString();
		}
		
	}
	
	/**
	 * @brief	get console handler that is set custom format
	 * @return	Handler
	 */
	//StAX version
	public static Handler getHandler() {
		LogManager.getLogManager().reset();
		Handler[] handlers = logger.getHandlers();
		
		if (handlers.length > 0 && handlers[0] instanceof ConsoleHandler) {
			logger.removeHandler(handlers[0]);
		}
		LogRecord logr = new LogRecord(Level.ALL,"");
		Formatter formatter = new Logwriter.FormatTransfer();
		formatter.formatMessage(logr);
		Handler cHandle = new ConsoleHandler();
		cHandle.setFormatter(formatter);
		return cHandle;
	}
	
	/**
	 * @brief	write log on txt file
	 * @param	fpath
	 * 			type: String
	 * 			the file path of txt file
	 * @param	logstring
	 * 			type: String
	 * 			the log contents
	 */
	public static void writeFile(String fpath, String logstring)  {
		
		try (PrintWriter fw = new PrintWriter(new FileWriter(fpath,true))){
			fw.println(logstring);
			
		} catch (Exception e) {
			logger.info(" logwrite failed\n");
		} 
	}
	
	/**
	 * @brief	write log on console
	 * @param	logstring
	 * 			type: String
	 * 			the log contents
	 */
	public static void writeConsole(String logstring) {
		logger.info(logstring);
	}
}