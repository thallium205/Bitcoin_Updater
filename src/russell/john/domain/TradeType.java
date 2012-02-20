package russell.john.domain;

import java.util.Date;

/**
 * This is a data object representing a single row in the historical data api from http://bitcoincharts.com/about/markets-api/
 * @author John
 *
 */
public class TradeType
{
	Date time;
	double price;
	double amount;
	
	public TradeType()
	{
		
	}
	
	public Date getTime()
	{
		return time;
	}
	public void setTime(Date time)
	{
		this.time = time;
	}
	public double getPrice()
	{
		return price;
	}
	public void setPrice(double price)
	{
		this.price = price;
	}
	public double getAmount()
	{
		return amount;
	}
	public void setAmount(double amount)
	{
		this.amount = amount;
	}
	
	
}
