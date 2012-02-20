package russell.john.domain;

public class IncomingAddressType
{

	private TransactionType transactionType;
	private String prev_out_tran_hash;
	private long n;
	private String scriptSig;
	private String coinbase;
	
	public IncomingAddressType()
	{
		
	}
	
	public IncomingAddressType(String prev_out_tran_hash, long n, String scriptSig, String coinbase)
	{
		this.prev_out_tran_hash = prev_out_tran_hash;
		this.n = n;
		this.scriptSig = scriptSig;
		this.coinbase = coinbase;
	}

	/**
	 * 
	 * @return Full hash of this transaction
	 */
	public String getHash()
	{
		return prev_out_tran_hash;
	}

	/**
	 * 
	 * @param hash
	 *            Full hash of this transaction
	 */
	public void setHash(String prev_out_tran_hash)
	{
		this.prev_out_tran_hash = prev_out_tran_hash;
	}

	/**
	 * 
	 * @return The index of where this particular address resides in the
	 *         transaction list.
	 */
	public long getN()
	{
		return n;
	}

	/**
	 * 
	 * @param l
	 *            The index of where this particular address resides in the
	 *            transaction list.
	 */
	public void setN(long l)
	{
		this.n = l;
	}

	/**
	 * 
	 * @return This script is matched with the referenced output's scriptPubKey.
	 *         It usually contains a signature, and possibly a public key.
	 *         ScriptSigs of generation inputs are sometimes called the
	 *         'coinbase' parameter, and they contain the current compact target
	 *         and the extraNonce variable
	 */
	public String getScriptSig()
	{
		return scriptSig;
	}

	/**
	 * 
	 * @param scriptSig
	 *            This script is matched with the referenced output's
	 *            scriptPubKey. It usually contains a signature, and possibly a
	 *            public key. ScriptSigs of generation inputs are sometimes
	 *            called the 'coinbase' parameter, and they contain the current
	 *            compact target and the extraNonce variable
	 */
	public void setScriptSig(String scriptSig)
	{
		this.scriptSig = scriptSig;
	}
	
	public String getCoinbase()
	{
		return coinbase;
	}

	public void setCoinbase(String coinbase)
	{
		this.coinbase = coinbase;
	}
	
	/**
	 * 
	 * @return the parent of this incoming address type
	 */
	public TransactionType getTransactionType()
	{
		return transactionType;
	}

	/**
	 * 
	 * @param transactionType The parent of this incoming address type
	 */
	public void setTransactionType(TransactionType transactionType)
	{
		this.transactionType = transactionType;
	}

}
