package russell.john;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import russell.john.domain.BlockType;
import russell.john.domain.IncomingAddressType;
import russell.john.domain.OutgoingAddressType;
import russell.john.domain.TradeType;
import russell.john.domain.TransactionType;

public class Database
{
	private MysqlTunnelSession sshMysqlTunnel = null;
	private Connection connection = null;

	// For SSH connections:
	/*
	 * private static final String server = "192.168.1.1"; private static final
	 * int ssh_port = 22; private static final String ssh_user = "user"; private
	 * static final String ssh_pass = "pass"; private static final String
	 * localhost = "127.0.0.1"; private static final int db_port = 3306; private
	 * static final String db_user = "db_user"; private static final String
	 * db_pass = "db_pass"; private static final String db_name = "db_name";
	 */

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static final Logger log = Logger.getLogger(BlockchainFetcher.class.getName());

	/**
	 * Creates a new database instance, creates a connection to the database.
	 * @throws SQLException 
	 */
	public Database(String cs, String user, String pass) throws SQLException
	{
		/*
		 * sshMysqlTunnel = new MysqlTunnelSession(server, ssh_port, ssh_user,
		 * ssh_pass, localhost, db_port, db_user, db_pass); connection =
		 * sshMysqlTunnel.getConnection(db_name);
		 */

		connection = DriverManager.getConnection(cs, user, pass);
	}

	/**
	 * Checks to see if a block is in the database
	 * 
	 * @param hash
	 *            - the given block hash
	 * @return True if it is in the database, false if it is not in the
	 *         database.
	 * @throws SQLException
	 */
	public Boolean isBlockStored(String hash) throws SQLException
	{
		log.info("Checking to see if this block exists...");
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		String query = "SELECT hash FROM Block WHERE hash = (?)";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, hash);
		resultSet = preparedStatement.executeQuery();

		return resultSet.next();
	}

	/**
	 * Adds a block to the database
	 * 
	 * @param block
	 */
	public void addBlock(BlockType block)
	{
		log.info("Storing block to the database...");
		PreparedStatement preparedStatement = null;
		String blockQuery = "INSERT INTO Block (hash, version, prev_block, mrkl_root, time, bits, nonce, n_tx, size) VALUES (?,?,?,?,?,?,?,?,?)";
		String transactionQuery = "INSERT INTO Transaction (hash, version, vin_sz, vout_sz, lock_time, size, Block_hash) VALUES (?,?,?,?,?,?,?)";
		String incomingQuery = "INSERT INTO Incoming (prev_out, n, scriptSig, coinbase, Transaction_hash) VALUES (?,?,?,?,?)";
		String outgoingQuery = "INSERT INTO Outgoing (scriptPubKey, n, value, Transaction_hash) VALUES (?,?,?,?)";

		try
		{
			// Store the block
			preparedStatement = connection.prepareStatement(blockQuery);
			preparedStatement.setString(1, block.getHash());
			preparedStatement.setInt(2, block.getVersion());
			preparedStatement.setString(3, block.getPrev_block());
			preparedStatement.setString(4, block.getMrkl_root());
			preparedStatement.setString(5, dateFormat.format(block.getTime()));
			preparedStatement.setLong(6, block.getBits());
			preparedStatement.setLong(7, block.getNonce());
			preparedStatement.setInt(8, block.getN_tx());
			preparedStatement.setLong(9, block.getSize());
			preparedStatement.executeUpdate();
			preparedStatement.close();

			// For each transaction in the block
			for (Iterator<TransactionType> tranIter = block.getTransactions().iterator(); tranIter.hasNext();)
			{
				TransactionType transaction = tranIter.next();

				// Store transaction
				preparedStatement = connection.prepareStatement(transactionQuery);
				preparedStatement.setString(1, transaction.getHash());
				preparedStatement.setInt(2, transaction.getVersion());
				preparedStatement.setInt(3, transaction.getVin_sz());
				preparedStatement.setInt(4, transaction.getVout_sz());
				preparedStatement.setInt(5, transaction.getLock_time());
				preparedStatement.setLong(6, transaction.getSize());
				preparedStatement.setString(7, transaction.getBlockType().getHash());
				preparedStatement.executeUpdate();
				preparedStatement.close();

				// For each incoming address in the transaction

				for (Iterator<IncomingAddressType> incIter = transaction.getIncoming_address().iterator(); incIter.hasNext();)
				{
					IncomingAddressType incoming = incIter.next();

					// Store incoming addresses
					preparedStatement = connection.prepareStatement(incomingQuery);
					preparedStatement.setString(1, incoming.getHash());
					preparedStatement.setLong(2, incoming.getN());
					preparedStatement.setString(3, incoming.getScriptSig());
					preparedStatement.setString(4, incoming.getCoinbase());
					preparedStatement.setString(5, incoming.getTransactionType().getHash());
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}

				// For each outgoing address in the transaction
				for (Iterator<OutgoingAddressType> outIter = transaction.getOutgoing_address().iterator(); outIter.hasNext();)
				{
					OutgoingAddressType outgoing = outIter.next();

					// Store outgoing address
					preparedStatement = connection.prepareStatement(outgoingQuery);
					preparedStatement.setString(1, outgoing.getScriptPubKey());
					preparedStatement.setInt(2, outgoing.getN());
					preparedStatement.setDouble(3, outgoing.getValue());
					preparedStatement.setString(4, outgoing.getTransactionType().getHash());
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
			}
		}

		catch (SQLException e)
		{
			log.severe(e.getMessage());
			return;
		}
	}

	/**
	 * Fetches the latest historical market date in the database given a market
	 * 
	 * @param market
	 *            - the market.
	 * 
	 * @return The most recent data point in the database.
	 */
	public long latestMarketDate(String market)
	{
		long time = -1;
		String query = "SELECT Unix_Timestamp(time) as time FROM Trade WHERE Market_symbol = ? ORDER BY time DESC LIMIT 1";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try
		{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, market);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next())
				time = resultSet.getLong("time");
			else
				time = 0;
		}

		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return time;
	}

	/**
	 * Stores a trade tuple to the database.
	 * 
	 * @param trade
	 * @param market
	 *            - The market
	 */
	public void addTrade(TradeType trade, String market)
	{
		log.info("Storing trade to the database...");
		PreparedStatement preparedStatement = null;
		String tradeQuery = "INSERT INTO Trade (time, price, amount, Market_symbol) VALUES (?,?,?,?)";

		try
		{
			// Store the trade
			preparedStatement = connection.prepareStatement(tradeQuery);
			preparedStatement.setString(1, dateFormat.format(trade.getTime()));
			preparedStatement.setDouble(2, trade.getPrice());
			preparedStatement.setDouble(3, trade.getAmount());
			preparedStatement.setString(4, market);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		}

		catch (SQLException e)
		{
			log.severe(e.getMessage());
		}

	}

	/**
	 * Returns the version of the mysql server
	 */
	public void getVersion()
	{
		Statement statement = null;
		ResultSet resultSet = null;

		try
		{
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT VERSION()");
		}

		catch (SQLException e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}

		finally
		{
			try
			{
				if (resultSet.next())
					log.info(resultSet.getString(1));

				if (resultSet != null)
					resultSet.close();
				if (statement != null)
					statement.close();
			}

			catch (SQLException e)
			{

			}
		}
	}

	/**
	 * Closes the connection to the database and to the SSH tunnel.
	 */
	public void closeConnection()
	{
		try
		{
			if (connection != null)
				connection.close();
			if (sshMysqlTunnel != null)
				sshMysqlTunnel.close();
		}

		catch (SQLException e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
