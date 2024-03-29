package org.wyki.cassandra.pelops;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.wyki.portability.SystemProxy;

public class Pelops {

	private static final Logger logger = SystemProxy.getLoggerFromFactory(Pelops.class);
	
	private static ConcurrentHashMap<String, ThriftPool> poolMap = new ConcurrentHashMap<String, ThriftPool>();
	
	/**
	 * Add a new Thrift connection pool and give it a name. The name is later used to identify the pool from which to request
	 * a connection when creating operands such as <code>Mutator</code> and <code>Selector</code>. A pool maintains connections to
	 * a specific Cassandra cluster instance. Typically a pool is created for each Cassandra cluster that must be accessed.
	 * @param poolName				A name used to reference the pool e.g. "MainDatabase" or "LucandraIndexes"
	 * @param contactNodes			An array of IP or DNS addresses identifying "known" nodes in this Cassandra cluster
	 * @param defaultPort			The port upon which Cassandra instances in this cluster will be listening
	 * @param maxOpRetries			If a connection fails while an operation is being performed, the maximum number of retries it may make using a new connection from the pool
	 * @param dynamicNodeDiscovery  Whether the pool should dynamically discover the cluster node members using describe_ring(). Note: due to a Cassandra bug this cannot be used on single node clusters or clusters with a replication factor of 1 prior to 6.1
	 * @param defaultKeyspace		If dynamic cluster node discovery is on, a default keyspace to be queried using describe_ring() to discover the nodes
	 * @param policy				Policy object controlling behavior
	 */
	public static void addPool(String poolName, String[] contactNodes, int defaultPort, boolean dynamicNodeDiscovery, String discoveryKeyspace, Policy policy) {
		logger.info("Pelops adds new pool {}", poolName);
		ThriftPool newPool = new ThriftPool(contactNodes, defaultPort, dynamicNodeDiscovery, discoveryKeyspace, policy);
		poolMap.put(poolName, newPool);
	}
	
	/**
	 * Shutdown Pelops. This proceeds by shutting down all connection pools.
	 */
	public static void shutdown() {
		logger.info("Pelops starting to shutdown...");
		for (ThriftPool pool : poolMap.values())
			pool.shutdown();
		logger.info("Pelops has shutdown");
	}
	
	/**
	 * Create a <code>Selector</code> object.
	 * @param poolName				The name of the connection pool to use (this determines the Cassandra database cluster)
	 * @param keyspace				The keyspace to operate on
	 * @return						A new <code>Selector</code> object
	 */
	public static Selector createSelector(String poolName, String keyspace) {
		return poolMap.get(poolName).createSelector(keyspace);
	}
	
	/**
	 * Create a <code>Mutator</code> object using the current time as the operation time stamp. The <code>Mutator</code> object  
	 * must only be used to execute 1 mutation operation.
	 * @param poolName				The name of the connection pool to use (this determines the Cassandra database cluster)
	 * @param keyspace				The keyspace to operate on
	 * @return						A new <code>Mutator</code> object
	 */
	public static Mutator createMutator(String poolName, String keyspace) {
		return poolMap.get(poolName).createMutator(keyspace);
	}
	
	/**
	 * Create a <code>Mutator</code> object with an arbitrary time stamp. The <code>Mutator</code> object
	 * must only be used to execute 1 mutation operation.
	 * @param poolName				The name of the connection pool to use (this determines the Cassandra database cluster)
	 * @param keyspace				The keyspace to operate on
	 * @param timestamp				The default time stamp to use for operations
	 * @return						A new <code>Mutator</code> object
	 */
	public static Mutator createMutator(String poolName, String keyspace, long timestamp) {
		return poolMap.get(poolName).createMutator(keyspace, timestamp);
	}
	
	/**
	 * Create a <code>KeyDeletor</code> object using the current time as the operation time stamp.
	 * @param poolName				The name of the connection pool to use (this determines the Cassandra database cluster)
	 * @param keyspace				The keyspace to operate on
	 * @return						A new <code>KeyDeletor</code> object
	 */
	public static KeyDeletor createKeyDeletor(String poolName, String keyspace) {
		return poolMap.get(poolName).createKeyDeletor(keyspace);
	}
	
	/**
	 * Create a <code>KeyDeletor</code> object with an arbitrary time stamp.
	 * @param poolName				The name of the connection pool to use (this determines the Cassandra database cluster)
	 * @param keyspace				The keyspace to operate on
	 * @return						A new <code>KeyDeletor</code> object
	 */
	public static KeyDeletor createKeyDeletor(String poolName, String keyspace, long timestamp) {
		return poolMap.get(poolName).createKeyDeletor(keyspace, timestamp);
	}
	
	/**
	 * Create a <code>Metrics</code> object for discovering information about the Cassandra cluster and its contained keyspaces.
	 * @param poolName				The name of the connection pool to use (this determines the Cassandra database cluster)
	 * @return						A new <code>Metrics</code> object
	 */
	public static Metrics createMetrics(String poolName) {
		return poolMap.get(poolName).createMetrics();
	}	
	
	/**
	 * Get a direct reference to a DbConnPool. This should never be needed while using Pelops's <code>Mutator</code> and
	 * <code>Selector</code> in normal usage. The reason this function is provided, is so that the Pelops connection pooling system can be
	 * used in conjunction with other systems such as Lucandra. 
	 * @param poolName				The name of the pool
	 * @return						A direct reference to the specified pool
	 */
	public static ThriftPool getDbConnPool(String poolName) {
		return poolMap.get(poolName);
	}
}
