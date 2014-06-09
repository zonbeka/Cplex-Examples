import java.net.InetAddress;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private Connection connection;
	private Statement statement;
	private StopWatch st;
	private String session;
	private String pc_name;
	private class StopWatch {
		  private long startTime = 0;
		  private long stopTime = 0;
		  private boolean running = false;
		  public void start() {
		    this.startTime = System.currentTimeMillis();
		    this.running = true;
		  }
		  public void stop() {
		    this.stopTime = System.currentTimeMillis();
		    this.running = false;
		  }
		  //elapsed time in seconds
		  public long elapsedTimeSec() {
		    long elapsed;
		    if (running) {
		      elapsed = ((System.currentTimeMillis() - startTime) / 1000);
		    }
		    else {
		      elapsed = ((stopTime - startTime) / 1000);
		    }
		    return elapsed;
		  }
		} 
	public Logger() {
		try {
			connection = getConnectionToDB();
			statement = connection.createStatement();
			st = new StopWatch();
			st.start();
			DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
			Date session_date = new Date();
			session = dateFormat.format(session_date);
			pc_name = InetAddress.getLocalHost().getHostName();
		}
		catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	public Connection getConnectionToDB() {
		try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		    	String db_connect_string = "jdbc:sqlserver://ultraman1.eng.buffalo.edu;instance=SQLEXPRESSHC;databaseName=Seminar";
		        return DriverManager.getConnection(db_connect_string,"open_user","bell");
		}
		catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
	}
	public void closeConnection() {
		try {
			connection.close();
		}
		catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	public boolean executeSql(String sqlStr) {
		try {
			statement.execute(sqlStr);
			return true;
		}
		catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
	}
	public double timeStamp() {
		return st.elapsedTimeSec();
	}
    public void writeLog(String instance, double obj, double bb) {
    	executeSql("INSERT INTO Logs VALUES ('" + session + "','" + instance + "'," + timeStamp() + "," + obj + "," + bb + ",'" + pc_name + "');");
    }
}