package russell.john;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import russell.john.domain.TradeType;

public class MarketFetcher
{
	private static final Logger log = Logger.getLogger(BlockchainFetcher.class.getName());
	private static final String HISTORICALMARKETADDRESS = "http://bitcoincharts.com/t/trades.csv";

	public static final String[] markets = { "mtgoxUSD", "thUSD", "intrsngGBP", "cryptoxUSD", "mtgoxEUR", "virwoxSLL", "mtgoxGBP", "btcdeEUR",
			"virtexCAD", "btceUSD", "intrsngUSD", "btcexUSD", "intrsngEUR", "mtgoxPLN", "cryptoxAUD", "cbxUSD", "btcnCNY", "mtgoxAUD", "wbxAUD",
			"mrcdBRL", "mtgoxCAD", "rockSLL", "bitstampUSD", "thEUR", "thAUD", "intrsngPLN", "mtgoxCNY", "mtgoxCHF", "bitnzNZD", "mtgoxSEK",
			"mtgoxJPY", "thCLP", "thLRUSD", "bitfloorUSD", "mtgoxNZD", "mtgoxRUB", "bitmarketEUR", "mtgoxSGD", "rockEUR", "mtgoxDKK", "ruxumUSD",
			"vcxUSD", "thINR", "bitmarketPLN", "bitmarketUSD", "imcexUSD", "rockUSD", "b2cUSD", "imcexEUR", "vcxEUR", "mtgoxHKD" };

	Database db = null;

	MarketFetcher(Database db)
	{
		this.db = db;		
	}

	/**
	 * Starts the process of fetching the latest market data.
	 * 
	 * @return True if successful
	 */
	public boolean execute()
	{
		// get the market data for all the markets
		for (int i = 0; i < markets.length; i++)
		{

			long latestDate = 0;
			// First query database to see latest market data
			log.info("Fetching/Storing: " + markets[i]);
			latestDate = db.latestMarketDate(markets[i]);

			// Increment the date by one second since we already have the latest
			// date
			latestDate++;

			// Find the latest time value we have
			try
			{
				for (Iterator<TradeType> iter = getMarketData(latestDate, markets[i]).iterator(); iter.hasNext();)
				{
					db.addTrade(iter.next(), markets[i]);
				}
			}

			catch (IOException e)
			{
				log.severe(e.getMessage());
				db.closeConnection();
				return false;
			}
		}
		db.closeConnection();
		return true;
	}

	/**
	 * Get's the market data of USD MtGox
	 * 
	 * @param startTime
	 *            The unix time stamp from when to get data to the present time
	 * @return An arraylist of tradetype tuples consisting of [time, price,
	 *         amount]
	 * @throws IOException
	 */
	private ArrayList<TradeType> getMarketData(Long startTime, String market) throws IOException
	{
		log.info("Downloading latest market data");
		URL url = new URL(HISTORICALMARKETADDRESS + "?symbol=" + market + "&start=" + startTime);
		URLConnection connection;

		StringTokenizer tokenizer = null;
		ArrayList<TradeType> trades = new ArrayList<TradeType>();
		TradeType trade = null;

		connection = url.openConnection();
		// 10 second timeout... the maximum
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;

		log.info("Parsing latest market data");
		while ((line = reader.readLine()) != null)
		{
			tokenizer = new StringTokenizer(line, ",");
			while (tokenizer.hasMoreTokens())
			{
				trade = new TradeType();
				trade.setTime(new Date(Long.parseLong(tokenizer.nextToken()) * 1000));
				trade.setPrice(Double.parseDouble(tokenizer.nextToken()));
				trade.setAmount(Double.parseDouble(tokenizer.nextToken()));
				trades.add(trade);
			}
		}

		reader.close();
		return trades;
	}
}
