## Summary
This application can download the entire bitcoin blockchain, and keep it updated, using blockexplorer.com API.  It can also download all the market data from the API of bitcoincharts.com and keep it up to date as well.  It will store the information into a (somewhat) optimized MYSQL database.

## Usage
[jdbc], [user], [pass], [schema_filepath (optional)] params...
-i: iterates through the entire blockchain. adding missing links along the way
-c: only adds the most recent blocks, stopping once the first existing link is found
-h: fetches historical market data, stopping once the first existing link is found
-b: builds the database schema. Must pass schema filepath to work.

## Examples
First time usage: 
1) Create an empty MYSQL database, and change the jdbc path as necessary.
2) java -jar Bitcoin_Updater.jar jdbc:mysql://localhost:3306/Bitcoin, username, password, schema.txt, -b

Usage examples:
To build the blockchain from scratch.  If the block already exists in the database, it will skip it and continue.  You can think of this option as a kind of consistency checker.
java -jar Bitcoin_Updater.jar jdbc:mysql://localhost:3306/Bitcoin, username, password, -i

To update the blockchain.  If the block already exists in the database, the program will terminate
java -jar Bitcoin_Updater.jar jdbc:mysql://localhost:3306/Bitcoin, username, password, -c

To update/build the historical market data.  If the historical market data already exists, the program will terminate and go to the next market.
java -jar Bitcoin_Updater.jar jdbc:mysql://localhost:3306/Bitcoin, username, password, -h

##Schema
<img src="https://github.com/thallium205/Bitcoin_Updater/raw/master/schema/schema.png"/>