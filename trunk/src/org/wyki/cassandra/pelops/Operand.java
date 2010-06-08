package org.wyki.cassandra.pelops;

import org.apache.cassandra.thrift.AuthenticationException;
import org.apache.cassandra.thrift.AuthorizationException;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.transport.TTransportException;
import org.wyki.cassandra.pelops.ThriftPool.Connection;

/**
 * Base class for objects operating against a Cassandra keyspace.
 * 
 * @author dominicwilliams
 *
 */
public class Operand {
	
	protected final ThriftPool thrift;
	
	protected Operand(ThriftPool thrift) {
		this.thrift = thrift;
	}
	
	protected Object tryOperation(IOperation operation) throws Exception {
		String lastNode = null;
		Exception lastException = null;
		int retries = 0;
		do {
			// Get a connection to a Cassandra node
			Connection conn = thrift.getConnectionExcept(lastNode);
			lastNode = conn.getNode();
			try {
				// Execute operation
				Object result = operation.execute(conn);
				// Release unbroken connection
				conn.release(false);
				// Return result!
				return result;
			} catch (Exception e) {
				// Is this a logic/application or system error?
				if (e instanceof NotFoundException ||
					e instanceof InvalidRequestException ||
					e instanceof TApplicationException ||
					e instanceof AuthenticationException ||
					e instanceof AuthorizationException) {
					// Yup, so we can release unbroken connection
					conn.release(false);
					// Re-throw application-level exceptions immediately.
					throw e;
				}
				// This connection is "broken" by network timeout or other problem.
				conn.release(true);
				// Should we try again?
				if (e instanceof TimedOutException ||
					e instanceof TTransportException) {
					retries++;
					lastException = e;
				} else // nope, throw
					throw e;
			}
		} while (retries < thrift.getPolicy().getMaxOpRetries());
		
		throw lastException;
	}
	
}
