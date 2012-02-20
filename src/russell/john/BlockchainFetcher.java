package russell.john;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.json.JSONException;

import russell.john.domain.BlockType;

public class BlockchainFetcher
{
	Database db = null;

	public static final String INITIAL = "initial";
	public static final String CONTINUE = "continue";

	public static final String LATESTBLOCKCOUNTADDRESS = "http://blockexplorer.com/q/nextretarget";
	public static final String LATESTHASHADDRESS = "http://blockexplorer.com/q/latesthash";
	public static final String RAWBLOCKADDRESS = "http://blockexplorer.com/rawblock/";
	public static final String GENESISBLOCK = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";

	private static final Logger log = Logger.getLogger(BlockchainFetcher.class.getName());

	/**
	 * Creates an SSH connection to the SQL server
	 */

	public BlockchainFetcher(Database db)
	{
		this.db = db;
	}

	/*
	 * public BlockType getBlockFromHash(String hash) throws IOException,
	 * JSONException { // Download the block from blockexplorer String rawBlock
	 * = getBlock(hash);
	 * 
	 * // Parse the block return parseBlock(rawBlock); }
	 */

	/**
	 * Will fetch the latest hash on the block chain and work backwards in the
	 * black chain, adding them only if they do not exist yet.
	 * 
	 * @param option
	 *            - Given INITIAL, this function will iterate through the entire
	 *            block chain, only skipping blocks if they already exist in the
	 *            datastore, but then will continue. Use this only when first
	 *            building the blockchain in the datastore, or if you suspect
	 *            the blockstore is incomplete. Given CONTINUE, it will only add
	 *            them backward until it finds a match, then stop. Use this when
	 *            the initial blockchain has been downloaded already.
	 * @throws SocketTimeoutException
	 */

	public boolean execute(String option)
	{
		BlockType block = new BlockType();
		int blockcount = 0;
		int counter = 0;
		String hash = null;
		String rawBlock = null;
		Boolean keepDownloading = true;

		int retry = 0;

		// Initial data download
		try
		{

			// Download block count
			blockcount = getLatestBlockCount();
			log.info("Total amount of blocks: " + blockcount);

			// Download latest hash
			hash = getLatestHash();

			// Download the block from blockexplorer
			rawBlock = getBlock(hash);

			// Parse the block
			block = parseBlock(rawBlock);
		}

		catch (IOException e)
		{
			log.severe(e.getMessage());
		} 
		
		catch (JSONException e)
		{
			log.severe(e.getMessage());
		}

		// Return the bloack
		while (!block.getHash().equals(GENESISBLOCK) && keepDownloading && retry < 10)
		{

			try
			{
				// Check to see if we have already stored it
				if (!db.isBlockStored(hash))
				{
					// if it returns empty we have not already stored it
					// Store the block in the database
					db.addBlock(block);

					// Increment counter
					counter++;

					// Print counter
					log.info(Integer.toString(counter));

					// Fetch the next block
					rawBlock = getBlock(block.getPrev_block());

					// Reset retry to 0
					retry = 0;

					// Parse the newly fetched block
					block = parseBlock(rawBlock);

					// Set the hash
					hash = block.getHash();
				}

				else
				{

					if (option.equals(CONTINUE))
					{
						// we are done, exit
						log.info("Duplicate block found.  Exiting.");
						keepDownloading = false;
						db.closeConnection();
						return true;
					}

					else if (option.equals(INITIAL))
					{
						log.info("Duplicate block found.  Checking the rest of the chain...");

						// Increment counter
						counter++;

						// Fetch the next block
						rawBlock = getBlock(block.getPrev_block());

						// Reset retry to 0
						retry = 0;

						// Parse the newly fetched block
						block = parseBlock(rawBlock);

						// Set the hash
						hash = block.getHash();
					}
				}
			}

			catch (SocketTimeoutException e)
			{
				retry++;
				log.warning(e.getMessage());
				log.warning("Time out... retrying " + retry + "/10");
			}

			catch (SQLException e)
			{
				retry++;
				log.warning(e.getMessage());
				log.warning("Error.  Retrying:  " + retry + "/10");
			} 
			
			catch (IOException e)
			{
				log.severe(e.getMessage());
			} 
			
			catch (JSONException e)
			{
				log.severe(e.getMessage());
			}

		}

		// download the genesis block if it hasnt already been stored already
		try
		{
			log.info("Fetching the genesis block");
			rawBlock = getBlock(GENESISBLOCK);
			block = parseBlock(rawBlock); // TODO - This failed :(
			if (db.isBlockStored(block.getHash()))
				db.addBlock(block);

		}

		catch (IOException e)
		{
			log.severe(e.getMessage());
		} 
		
		catch (JSONException e)
		{
			log.severe(e.getMessage());
		} 
		
		catch (SQLException e)
		{
			log.severe(e.getMessage());
		}

		db.closeConnection();
		return true;
	}

	/**
	 * Grabs the latest block count from the blockchain.
	 * 
	 * @return The number of blocks existing in the chain.
	 * @throws IOException
	 */
	public static int getLatestBlockCount() throws IOException
	{
		log.info("Fetching the latest count from blockchain...");
		URL url = new URL(LATESTBLOCKCOUNTADDRESS);
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder builder = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null)
		{
			builder.append(line);
		}

		reader.close();
		return Integer.parseInt(builder.toString());
	}

	/**
	 * Grabs the latest hash from the blockchain.
	 * 
	 * @return The latest hash address from the blockchain.
	 * @throws IOException
	 */
	public static String getLatestHash() throws IOException
	{
		log.info("Fetching the latest hash from blockchain...");
		URL url = new URL(LATESTHASHADDRESS);
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder builder = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null)
		{
			builder.append(line);
		}

		reader.close();
		return builder.toString();
	}

	/**
	 * 
	 * @param hash
	 *            - A bitcoin address hash.
	 * @return - A block.
	 * @throws IOException
	 */
	private static String getBlock(String hash) throws IOException
	{
		log.info("Downloading block: " + hash + " from the blockchain");
		URL url = new URL(RAWBLOCKADDRESS + hash);
		URLConnection connection;
		connection = url.openConnection();
		// 10 second timeout... the maximum
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null)
		{
			builder.append(line + "\n");
		}

		reader.close();
		return builder.toString();
	}

	/**
	 * Converts a raw block in string form into a BlockType
	 * 
	 * @param block
	 *            A string returned from blockexplorer
	 * @return A blocktype
	 * @throws JSONException
	 */
	private BlockType parseBlock(String block) throws JSONException
	{
		log.info("Parsing block");
		BlockType b = new BlockType();
		b.parseBlock(block);
		return b;
	}
}
