package kr.co.aim.greenframe.logger.appender;

import java.io.File;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

public class DailySizeRollingFileAppender extends RollingFileAppender {
	private long nextCheck = System.currentTimeMillis() - 1;

	Date now = new Date();

	SimpleDateFormat dirSdf;
	
	SimpleDateFormat sdf;

	private String datePattern;
	
	private String directoryPattern;

	private String scheduledDate;
	
	private String scheduledDirecotoryDate;
	
	@Override
	public void rollOver() {
		super.rollOver();
	}

	@Override
	protected void subAppend(LoggingEvent arg0) {
		String svrName = System.getProperty("svr");
		if ((svrName.equals("FMCsvr") /*|| svrName.equals("QRYsvr")*/) && arg0.getThreadName().contains("THREAD-ID"))
		{
			arg0.getLogger().setLevel(arg0.getLevel().ERROR);
		}

		long n = System.currentTimeMillis();
		if (n >= nextCheck) {
			now.setTime(n);
			nextCheck = getNextCheckMillis(now);
			try {
			    String datedFilename = sdf.format(now);
				rollDayOver();
			} catch (Exception ioe) {
				if (ioe instanceof InterruptedIOException) {
					Thread.currentThread().interrupt();
				}
				LogLog.error("rollOver() failed.", ioe);
			}
		}
		super.subAppend(arg0);
	}
	
	public void setDirectoryPattern(String pattern) {
		directoryPattern = pattern;
		if(!pattern.isEmpty()) {
			dirSdf = new SimpleDateFormat(directoryPattern);
		}
	}
	public void setDatePattern(String pattern) {
		datePattern = pattern;
		if(!pattern.matches(".*\\%.*d.*")) {
			if(datePattern.endsWith("'")) {
				datePattern = datePattern.replaceAll("'$",".%d'");
			}else {
				datePattern = datePattern+"'.%d'";
			}
		}
		sdf = new SimpleDateFormat(datePattern);
	}
	@Override
	public void activateOptions() {
		super.activateOptions();
		File file = new File(fileName);
	    scheduledDate = sdf.format(new Date(file.lastModified()));
	    if(dirSdf != null) {
	    	scheduledDirecotoryDate = dirSdf.format(new Date(file.lastModified()));
	    }
	}
	private void rollDayOver() {
		
	    String datedFilename = sdf.format(now);
	    if (scheduledDate.equals(datedFilename)) {
	      return;
	    }
	    rollOver();
		File target;
		File file;
		File target2;
		File file2;
		File dir = null;

		boolean renameSucceeded = true;
		boolean renameSucceeded2 = true;
		boolean renameDatePatternCompleted = false;
		// If maxBackups <= 0, then there is no file renaming to be done.
		if(scheduledDirecotoryDate != null) {
			dir = new File(new File(fileName).getParent()+"/"+scheduledDirecotoryDate);
			if(!dir.exists()) {
				dir.mkdirs();
			}
		    scheduledDirecotoryDate = dirSdf.format(now);
		}
		
		
		if (maxBackupIndex > 0) {

			// Map {(maxBackupIndex), ..., 2, 1} to {maxBackupIndex.datePattern,..., 3.datePattern, 2.datePattern}
			for (int i = maxBackupIndex; i >= 1 && renameSucceeded; i--) {
				file = new File(fileName + "." + i);
				
				if (file.exists()) {
					for (int j = maxBackupIndex; j >= 1 && renameSucceeded2 && !renameDatePatternCompleted; j--) {
						MessageFormat.format(scheduledDate, j);
						String dataPatternFileName = fileName + String.format(scheduledDate, j);
						file2 = new File(dataPatternFileName);
						if(file2.exists()) {
							String dataPatternFileName2 = fileName + String.format(scheduledDate,(i+j));
							target2 = new File(dataPatternFileName2);
							renameSucceeded2 = file2.renameTo(target2);
						}
					}
					renameDatePatternCompleted = true;
					String dataPatternFileName = fileName + String.format(scheduledDate,i);
					target = new File(dataPatternFileName);
					if(dir != null) {
						String tname = target.getName();
						target = new File(dir.getAbsolutePath()+"/"+tname);
					}
					LogLog.debug("Renaming file " + file + " to " + target);
					renameSucceeded = file.renameTo(target);
				}
			}

		}
	    scheduledDate = datedFilename;

	}

	private long getNextCheckMillis(Date date) {
		// set next hour
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(calendar.HOUR, 1);
		long next = calendar.getTimeInMillis();
		return next;
	}
}
