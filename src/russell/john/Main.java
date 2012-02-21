package russell.john;

import java.sql.SQLException;
import java.util.logging.Logger;

public class Main
{
	private static final Logger log = Logger.getLogger(BlockchainFetcher.class.getName());

	/**
	 * @param args
	 *            -i: iterates through the entire
	 *            blockchain. adding missing links -c: only adds the most recent
	 *            blocks, stopping once the first existing link is found
	 *            -h: fetches historical market data -b: builds database
	 */
	public static void main(String[] args)
	{
		String jdbc = null;
		String user = null;
		String pass = null;
		String schemaFilePath = null;
		boolean initialBlock = false;
		boolean continueBlock = false;
		boolean continueMarket = false;
		boolean buildDatabase = false;

		// Error checking
		if (args.length < 1)
		{
			errorMessage();
			return;
		}

		// Grab arguments	
		try
		{
		jdbc = args[0];
		user = args[1];
		pass = args[2];
		
		if (args.length > 3)
			schemaFilePath = args[3];
		}
		
		catch (Exception e)
		{
			log.severe(e.getMessage());
			errorMessage();
			return;
		}
		
		for (int i = 3; i < args.length; i++)
		{
			try
			{
				if (args[i].contains("-i"))
					initialBlock = true;
				if (args[i].contains("-c"))
					continueBlock = true;
				if (args[i].contains("-h"))
					continueMarket = true;
				if (args[i].contains("-b"))
					buildDatabase = true;
			}

			catch (Exception e)
			{
				log.severe(e.getMessage());
				errorMessage();
				return;
			}
		}

		// More error checking
		if (jdbc == null || user == null || pass == null)
		{
			errorMessage();
			return;
		}

		else if (initialBlock == true && continueBlock == true)
		{
			log.severe("-i and -c are mutually exclusive.");
			errorMessage();
			return;
		}
		
		// Instantiate database object
		Database db = null;
		try
		{
			db = new Database(jdbc, user, pass);
		} 
		
		catch (SQLException e)
		{
			log.severe(e.getMessage());
			errorMessage();
			return;
		}
		

		// Run program
		if (initialBlock)
		{
			log.info("Initial Blockchain Build");
			BlockchainFetcher blockchainFetcher = new BlockchainFetcher(db);
			blockchainFetcher.execute(BlockchainFetcher.INITIAL);
			log.info("Full blockchain update completed");

		}

		else if (continueBlock)
		{
			log.info("Continue Blockchain Build");
			BlockchainFetcher blockchainFetcher = new BlockchainFetcher(db);
			blockchainFetcher.execute(BlockchainFetcher.CONTINUE);
			log.info("Quick blockchain update completed");
		}

		else if (continueMarket)
		{
			log.info("Continue Historical Build");
			MarketFetcher marketFetcher = new MarketFetcher(db);
			marketFetcher.execute();
			log.info("Trade update completed");
		}
		
		else if (buildDatabase)
		{
			log.info("Building the database");
			try
			{
				db.createSchema(schemaFilePath);
			} 
			
			catch (SQLException e)
			{
				log.severe(e.getMessage());
				errorMessage();
				return;
			}				
		}
		
		else
		{
			errorMessage();
		}
	}

	private static void errorMessage()
	{
		log.severe("Usage:\n" + "[jdbc], [user], [pass], [schema_filepath (optional)] params...\n"
				+ "-i: iterates through the entire blockchain. adding missing links along the way\n"
				+ "-c: only adds the most recent blocks, stopping once the first existing link is found\n"
				+ "-h: fetches historical market data, stopping once the first existing link is found\n" +
				"-b: builds the database schema. Must pass schema filepath to work.");
	}
}
