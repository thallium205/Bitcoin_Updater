package russell.john;

import java.sql.SQLException;
import java.util.logging.Logger;

public class Main
{
	private static final Logger log = Logger.getLogger(BlockchainFetcher.class.getName());

	/**
	 * @param args
	 *            -b: fetches the blockchain -i: iterates through the entire
	 *            blockchain. adding missing links -c: only adds the most recent
	 *            blocks, stopping once the first existing link is found
	 *            (DEFAULT BEHAVIOR) -h: fetches historical market data
	 */
	public static void main(String[] args)
	{
		String jdbc = null;
		String user = null;
		String pass = null;
		boolean initialBlock = false;
		boolean continueBlock = false;
		boolean continueMarket = false;

		// Error checking
		if (args.length < 1)
		{
			errorMessage();
			return;
		}

		// Grab arguments		
		jdbc = args[0];
		user = args[1];
		pass = args[2];
		
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
		Database db;
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
		
		else
		{
			errorMessage();
		}
	}

	private static void errorMessage()
	{
		log.severe("Usage:\n" + "[jdbc], [user], [pass], params..."
				+ "\t -i: iterates through the entire blockchain. adding missing links along the way\n"
				+ "\t -c: only adds the most recent blocks, stopping once the first existing link is found (default) \n"
				+ "-h: fetches historical market data, stopping once the first existing link is found");
	}
}
