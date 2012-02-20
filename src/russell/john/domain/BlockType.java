package russell.john.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BlockType
{
	private String hash;
	private int version;
	private String prev_block;
	private String mrkl_root;
	private Date time;
	private Long bits;
	private long nonce;
	private int n_tx;
	private long size;
	private List<TransactionType> transactions;

	public BlockType()
	{

	}

	public BlockType(String hash, int version, String prev_block, String mrkl_root, Date time, long bits, long nonce, int n_tx, long size,
			List<TransactionType> transactions)
	{
		this.hash = hash;
		this.version = version;
		this.prev_block = prev_block;
		this.mrkl_root = mrkl_root;
		this.time = time;
		this.bits = bits;
		this.nonce = nonce;
		this.n_tx = n_tx;
		this.size = size;
		this.transactions = transactions;
	}

	/**
	 * 
	 * @return Full hash of this block. Sometimes this is expressed without the
	 *         leading zeroes.
	 */
	public String getHash()
	{
		return hash;
	}

	/**
	 * 
	 * @param hash
	 *            Full hash of this block. Sometimes this is expressed without
	 *            the leading zeroes.
	 */
	public void setHash(String hash)
	{
		this.hash = hash;
	}

	/**
	 * @return
	 */
	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version
	 */
	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * @return Every block builds on another, forming a chain. This is the full
	 *         hash of the previous block.
	 */
	public String getPrev_block()
	{
		return prev_block;
	}

	/**
	 * @param prev_block
	 *            Every block builds on another, forming a chain. This is the
	 *            full hash of the previous block.
	 */
	public void setPrev_block(String prev_block)
	{
		this.prev_block = prev_block;
	}

	/**
	 * @return The root hash in a hash tree of all transactions.
	 */
	public String getMrkl_root()
	{
		return mrkl_root;
	}

	/**
	 * @param mrkl_root
	 *            This is the root hash in a hash tree of all transactions.
	 */
	public void setMrkl_root(String mrkl_root)
	{
		this.mrkl_root = mrkl_root;
	}

	/**
	 * *
	 * 
	 * @return time included in this block. The network's time must not be
	 *         relied upon for precision, but it is generally accurate.
	 */
	public Date getTime()
	{
		return time;
	}

	/**
	 * @param date
	 *            the UTC time included in this block. The network's time must
	 *            not be relied upon for precision, but it is generally
	 *            accurate.
	 */
	public void setTime(Date time)
	{
		this.time = time;
	}

	/**
	 * @return This is the compact form of the 256-bit target used when
	 *         generating. This is included in actual blocks. The difficulty
	 *         number is derived from this.
	 */
	public Long getBits()
	{
		return bits;
	}

	/**
	 * @param bits
	 *            This is the compact form of the 256-bit target used when
	 *            generating. This is included in actual blocks. The difficulty
	 *            number is derived from this.
	 */
	public void setBits(Long bits)
	{
		this.bits = bits;
	}

	/**
	 * @return When generating, Bitcoin starts this number at 1 and increments
	 *         for each hash attempt.
	 */
	public long getNonce()
	{
		return nonce;
	}

	/**
	 * @param nonce
	 *            When generating, Bitcoin starts this number at 1 and
	 *            increments for each hash attempt.
	 */
	public void setNonce(long nonce)
	{
		this.nonce = nonce;
	}

	/**
	 * @return The number of transactions in this block
	 */
	public int getN_tx()
	{
		return n_tx;
	}

	/**
	 * @param n_tx
	 *            The number of transactions in this block
	 */
	public void setN_tx(int n_tx)
	{
		this.n_tx = n_tx;
	}

	/**
	 * @return The data size of this transaction in non-computer bytes. This is
	 *         the number that Bitcoin uses for block size limits and fees -- it
	 *         may not be the actual size on disk. 1 kilobyte = 1000 bytes (this
	 *         is how Bitcoin does it).
	 */
	public long getSize()
	{
		return size;
	}

	/**
	 * @param size
	 *            The data size of this transaction. This is the number that
	 *            Bitcoin uses for block size limits and fees -- it may not be
	 *            the actual size on disk. 1 kilobyte = 1000 bytes (this is how
	 *            Bitcoin does it).
	 */
	public void setSize(long size)
	{
		this.size = size;
	}

	/**
	 * @return A list of transactions associated with this block.
	 */
	public List<TransactionType> getTransactions()
	{
		return transactions;
	}

	/**
	 * @param transactions
	 *            A list of transactions associated with this block.
	 */
	public void setTransactions(List<TransactionType> transactions)
	{
		this.transactions = transactions;
	}

	/**
	 * Given a block converted to a string from blockexplorer.com, this function
	 * will create a BlockType object.
	 * 
	 * @param blockString
	 *            The value of the given page
	 * @throws JSONException
	 */
	public void parseBlock(String blockString) throws JSONException
	{
		// Components of the block
		final String HASH = "hash";
		final String VER = "ver";
		final String PREV_BLOCK = "prev_block";
		final String MRKL_ROOT = "mrkl_root";
		final String TIME = "time";
		final String BITS = "bits";
		final String NONCE = "nonce";
		final String N_TX = "n_tx";
		final String SIZE = "size";
		ArrayList<TransactionType> transactions = new ArrayList<TransactionType>();

		// Components of a transaction. Note: some are shared with the block:
		// version, hash, lock time
		final String TX = "tx";
		final String VIN_SZ = "vin_sz";
		final String VOUT_SZ = "vout_sz";
		final String LOCK_TIME = "lock_time";		

		// Components of an incoming address in a transaction
		final String IN = "in";
		final String PREV_OUT = "prev_out";
		final String N = "n";
		final String SCRIPTSIG = "scriptSig";
		final String COINBASE = "coinbase";

		// Components of an outgoing address in a transaction
		final String OUT = "out";
		final String VALUE = "value";
		final String SCRIPTPUBKEY = "scriptPubKey";

		// We are first going to get the data regarding the block itself
		JSONObject block = new JSONObject(blockString);
		this.setHash(block.getString(HASH));
		this.setVersion(block.getInt(VER));
		this.setPrev_block(block.getString(PREV_BLOCK));
		this.setMrkl_root(block.getString(MRKL_ROOT));
		this.setTime(new Date((block.getLong(TIME)) * 1000));
		this.setBits(block.getLong((BITS)));
		this.setNonce(block.getLong(NONCE));
		this.setN_tx(block.getInt(N_TX));
		this.setSize(block.getLong(SIZE));

		// Next, we are going to get all the transaction information
		JSONArray transactionsArray = block.getJSONArray(TX);
		for (int i = 0; i < transactionsArray.length(); i++)
		{
			TransactionType transaction = new TransactionType();
			JSONObject tranJSON = (JSONObject) transactionsArray.get(i);

			transaction.setHash(tranJSON.getString(HASH));
			transaction.setVersion(tranJSON.getInt(VER));
			transaction.setVin_sz(tranJSON.getInt(VIN_SZ));
			transaction.setVout_sz(tranJSON.getInt(VOUT_SZ));
			transaction.setLock_time(tranJSON.getInt(LOCK_TIME));
			transaction.setSize(tranJSON.getLong(SIZE));

			// Next we are going to get all the incoming transaction address
			// information
			ArrayList<IncomingAddressType> incomingAddresses = new ArrayList<IncomingAddressType>();
			JSONArray incomingTransactionsArray = tranJSON.getJSONArray(IN);
			for (int j = 0; j < incomingTransactionsArray.length(); j++)
			{

				IncomingAddressType incoming = new IncomingAddressType();
				JSONObject incomingJSON = (JSONObject) incomingTransactionsArray.get(j);

				incoming.setHash(incomingJSON.getJSONObject(PREV_OUT).getString(HASH));
				incoming.setN(incomingJSON.getJSONObject(PREV_OUT).getLong(N));

				// Sometimes transactions dont have a coinbase and sometimes
				// they dont have a scriptSig, so we will wrap this in a try so
				// an exception isn't thrown
				try
				{
					incoming.setCoinbase(incomingJSON.getString(COINBASE));
				} catch (Exception e)
				{
					// do nothing, we don't care
				}

				try
				{
					incoming.setScriptSig(incomingJSON.getString(SCRIPTSIG));
				} catch (Exception e)
				{
					// do nothing, we don't care
				}

				// add this incoming address to the array list
				incomingAddresses.add(incoming);
				
				// add the parent TransactionType to the child IncomingAddressType (for one-to-many relationship establishment)
				incoming.setTransactionType(transaction);
			}			

			
			// add all the incoming addresses to the transaction
			transaction.setIncoming_address(incomingAddresses);
			
		

			// Next we are going to get all the outgoing transaction address
			// information
			ArrayList<OutgoingAddressType> outgoingAddresses = new ArrayList<OutgoingAddressType>();
			JSONArray outgoingTransactionsArray = tranJSON.getJSONArray(OUT);
			for (int k = 0; k < outgoingTransactionsArray.length(); k++)
			{
				OutgoingAddressType outgoing = new OutgoingAddressType();
				JSONObject outgoingJSON = (JSONObject) outgoingTransactionsArray.get(k);

				outgoing.setValue(outgoingJSON.getDouble(VALUE));
				outgoing.setN(k);
				outgoing.setScriptPubKey(outgoingJSON.getString(SCRIPTPUBKEY));

				// add this outgoing address to the array list
				outgoingAddresses.add(outgoing);
				
				// add the parent TransactionType to the child OutgoingAddressType (for one-to-many relationship establishment)
				outgoing.setTransactionType(transaction);
			}
			// add all the outgoing addresses to the transaction
			transaction.setOutgoing_address(outgoingAddresses);
			
			// add the parent Blocktype to the child TransactionType (for one-to-many relationship establishment)
			transaction.setBlockType(this);

			// add this entire transaction to the block
			transactions.add(transaction);
		}
		// add all transactions to the block
		this.setTransactions(transactions);
	}
}