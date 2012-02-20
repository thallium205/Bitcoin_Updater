package russell.john;

import java.io.*;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * Creates JDBC connections to a MySQL server through an SSH tunnel.
 */
public class MysqlTunnelSession
{
	private final String sshHost, sshUsername, sshPassword;
	private final int sshPort;

	private final String dbHost, dbUsername, dbPassword;
	private final int dbPort;

	private JSch jsch;
	private Session session;

	public MysqlTunnelSession(String sshHost, int sshPort, String sshUsername, String sshPassword, String dbHost, int dbPort, String dbUsername,
			String dbPassword)
	{
		// SSH tunnel
		this.sshHost = sshHost;
		this.sshPort = sshPort;
		this.sshUsername = sshUsername;
		this.sshPassword = sshPassword;

		// Remote database
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;

		jsch = new JSch();
		try
		{
			session = openSession();
		} catch (JSchException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Closes the SSH session.
	 * 
	 * Calling this will render all JDBC Connection objects instantiated through
	 * this object disconnected and unusable.
	 */
	public void close()
	{
		if (session != null)
		{
			session.disconnect();
			session = null;
		}
	}

	/**
	 * Establishes a new SSH session.
	 * 
	 * @returns connected SSH session.
	 * @throws JSchException
	 *             when there was a connection or configuration problem.
	 */
	private Session openSession() throws JSchException
	{
		Session session = jsch.getSession(sshUsername, sshHost, sshPort);
		session.setPassword(sshPassword);

		// Not providing an interactive session. Just return defaults.
		session.setUserInfo(new UserInfo()
		{
			public String getPassphrase()
			{
				return null;
			}

			public String getPassword()
			{
				return null;
			}

			public boolean promptPassword(String message)
			{
				return true;
			}

			public boolean promptPassphrase(String message)
			{
				return true;
			}

			public boolean promptYesNo(String message)
			{
				return true;
			}

			public void showMessage(String message)
			{
			}
		});

		session.connect();
		return session;
	}

	/**
	 * Gets an available local port by binding to port 0. The OS will allocate
	 * the next available port.
	 * 
	 * @throws IOException
	 */
	private int getAvailableLocalPort() throws IOException
	{
		ServerSocket socket = new ServerSocket(0);
		int port = socket.getLocalPort();
		socket.close();
		return port;
	}

	/**
	 * Establishes a JDBC Connection via the tunnel.
	 * 
	 * @param database
	 *            the database to get a Connection to.
	 * @returns a usable JDBC Connection.
	 */
	public Connection getConnection(String database)
	{
		try
		{
			// Forward remote a local port to the remote database
			int localPort = getAvailableLocalPort();
			session.setPortForwardingL(localPort, dbHost, dbPort);

			// Establish JDBC connection to remote database
			return DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s", dbHost, localPort, database), dbUsername, dbPassword);
		} catch (Exception ex)
		{
			throw new RuntimeException("SSH tunnel failure: " + ex.getMessage(), ex);
		}
	}
}
