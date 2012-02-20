package russell.john.domain;

public class OutgoingAddressType
{
	private TransactionType transactionType;
	private double value;
	private int n;
	private String scriptPubKey;
	
	public OutgoingAddressType()
	{
		
	}
	
	public OutgoingAddressType(double value, int n, String scriptPubKey)
	{
		this.value = value;
		this.n = n;
		this.scriptPubKey = scriptPubKey;
	}

	/**
	 * 
	 * @return	The BTC sent by this output.
	 */
	public double getValue()
	{
		return value;
	}
	
	/**
	 * The index this outgoing transaction exists in the transaction list.
	 * @return The index
	 */
	public int getN()
	{
		return n;
	}
	
	/**
	 * The index this outgoing transaction exists in the transaction list.
	 * @param The index of where ths outgoing transacction exists in the transaction list.
	 */
	public void setN(int n)
	{
		this.n = n;
	}

	/**
	 * 
	 * @param value The BTC sent by this output.
	 */
	public void setValue(double value)
	{
		this.value = value;
	}

	/**
	 * 
	 * @return	This script specifies the conditions that must be met by someone attempting to redeem this output. Usually it contains a hash160 (Bitcoin address) or a public key.
	 */
	public String getScriptPubKey()
	{
		return scriptPubKey;
	}

	/**
	 * 
	 * @param scriptPubKey This script specifies the conditions that must be met by someone attempting to redeem this output. Usually it contains a hash160 (Bitcoin address) or a public key.
	 */
	public void setScriptPubKey(String scriptPubKey)
	{
		this.scriptPubKey = scriptPubKey;
	}
	
	/**
	 * 
	 * @return The parent of this outgoing address type
	 */
	public TransactionType getTransactionType()
	{
		return transactionType;
	}

	/**
	 * 
	 * @param transactionType The parent of this outgoing address type
	 */
	public void setTransactionType(TransactionType transactionType)
	{
		this.transactionType = transactionType;
	}
	
	
}
