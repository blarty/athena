
package org.jini.projects.athena.leasing;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.LeaseMapException;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.export.Exporter;
import net.jini.id.ReferentUuid;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

import org.jini.projects.athena.connection.AbstractRConnectionImpl;

import com.sun.jini.constants.TimeConstants;
import com.sun.jini.landlord.FixedLeasePeriodPolicy;
import com.sun.jini.landlord.Landlord;
import com.sun.jini.landlord.LeaseFactory;
import com.sun.jini.landlord.LeasePeriodPolicy;
import com.sun.jini.landlord.LeasedResource;

/**
 * Defines a Leasing landlord for managing connections
 * 
 * @author calum
 */
public class ConnectionLandlord implements Landlord, ReferentUuid, Remote {
	protected Map leases = new HashMap();
	private Logger log = Logger.getLogger("org.jini.projects.athena.leasing");
	Uuid myId = null;
	protected LeaseFactory myFactory;
	protected LeasePeriodPolicy myGrantPolicy = new FixedLeasePeriodPolicy(1 * TimeConstants.MINUTES, 30 * TimeConstants.SECONDS);
	private Reaper reaper = new Reaper();

	/**
	 * Create the landlord and export the factory
	 */
	public ConnectionLandlord() {
		super();
		// URGENT Complete constructor stub for ChangeLandlord
		myId = UuidFactory.generate();
		Exporter exp = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory());
		try {
			myFactory = new LeaseFactory((Landlord) exp.export(this), myId);
		} catch (ExportException e) {
			e.printStackTrace();
		}
		reaper.start();
		Logger.getAnonymousLogger().fine("ConnectionLandlord Initialised");
	}

	/**
	 * Kill an existing lease
	 * 
	 * @param obj
	 * @throws net.jini.core.lease.UnknownLeaseException
	 */
	public void killLease(Uuid obj) throws net.jini.core.lease.UnknownLeaseException {
		log.finer("Killing Lease ID: " + obj.toString());
		if (leases.containsKey(obj))
			leases.remove(obj);
		else
			throw new UnknownLeaseException("Unknown lease");
	}

	/**
	 * Create a new lease with a given duration for the connection
	 * 
	 * @param conn
	 * @param reqduration
	 * @return @throws
	 *              LeaseDeniedException
	 */
	public Lease newLease(AbstractRConnectionImpl conn, long reqduration) throws LeaseDeniedException {
		//System.out.println("New Lease Requested");

		Uuid regCookie = UuidFactory.generate();
		conn.setCookie(regCookie);
		ConnectionResource res = new ConnectionResource(conn, regCookie);
		LeasePeriodPolicy.Result r = myGrantPolicy.grant(res, reqduration);
		res.setExpiration(r.expiration);
		leases.put(regCookie, res);
		//handlers.put(regCookie, handler);
		// System.out.println("Cookie:" + regCookie);
		if (conn == null)
			System.out.println("No Connection passed");
		// System.out.println("Creating new Lease from Policy now");
		return myFactory.newLease(regCookie, res.getExpiration());
	}

	/**
	 * Allow a client Renew an existing lease
	 */
	public long renew(Uuid cookie, long duration) throws LeaseDeniedException, UnknownLeaseException, RemoteException {
		if (!leases.containsKey(cookie))
			throw new UnknownLeaseException();
		LeasedResource res = (LeasedResource) leases.get(cookie);
		LeasePeriodPolicy policy = myGrantPolicy;
		log.finer("Renewing a lease");
		synchronized (res) {
			if (res.getExpiration() <= System.currentTimeMillis()) {
				// Lease has expired, don't renew
				throw new UnknownLeaseException("Lease expired");
			}
			// No one can expire the lease, so it is safe to update.
			final LeasePeriodPolicy.Result r = policy.renew(res, duration);
			res.setExpiration(r.expiration);
			return r.duration;
		}
	}

	/**
	 * Cancel an existing lease
	 */
	public void cancel(Uuid cookie) throws UnknownLeaseException, RemoteException {
		// TODO Complete method stub for cancel
		killLease(cookie);
	}

	/**
	 * Renew a set of leases
	 */
	public RenewResults renewAll(Uuid[] cookies, long[] durations) throws RemoteException {
		boolean somethingDenied = false;
		long[] granted = new long[cookies.length];
		Exception[] denied = new Exception[cookies.length + 1];
		for (int i = 0; i < cookies.length; i++) {
			try {
				granted[i] = renew(cookies[i], durations[i]);
				denied[i] = null;
			} catch (Exception ex) {
				somethingDenied = true;
				granted[i] = -1;
				denied[i] = ex;
			}
		}
		if (!somethingDenied)
			denied = null;
		return new Landlord.RenewResults(granted, denied);
	}

	/**
	 * Cancel a set of leases
	 */
	public Map cancelAll(Uuid[] cookies) throws RemoteException {
		Map exMap = null;
		LeaseMapException lmEx = null;
		for (int i = 0; i < cookies.length; i++) {
			try {
				cancel(cookies[i]);
			} catch (Exception e) {
				if (lmEx == null) {
					exMap = new HashMap();
					lmEx = new LeaseMapException(null, exMap);
				}
				exMap.put(myFactory.newLease(cookies[i], 0), e);
			}
		}
		if (lmEx != null)
			return exMap;
		else
			return null;
	}
	/**
	 * Removes expired leases, unexports and deallocates any associated connections.
	 * Due to Athena being able to support multiple concurrent connections, a grace period
	 * of 15 seconds is added to expirations, in order to allow for some degree of bottleneck in the network
	 */

	public class Reaper extends Thread {
		/**
		 * Run the xpiration checking, removing any expired leases
		 */
		public void run() {
			for (;;) {
				synchronized (leases) {
					try {
						List keystoremove = Collections.synchronizedList(new ArrayList());
						Iterator iter = leases.entrySet().iterator();

						while (iter.hasNext()) {
							Map.Entry leaseEntry = (Map.Entry) iter.next();
							Object obj = leaseEntry.getValue();
							//System.out.println("LeaseValue name
							// "+obj.getClass().getName());
							LeasedResource lconn = (LeasedResource) obj;
							if ((lconn.getExpiration() + (15 * TimeConstants.SECONDS)) < System.currentTimeMillis()) {
								Uuid leaseKey = (Uuid) leaseEntry.getKey();

								log.finer("...expiring a lease with key " + leaseKey);
								keystoremove.add(leaseKey);
							}
						}
						for (Iterator removeIter = keystoremove.iterator(); removeIter.hasNext();) {

							log.finer("Deallocating");
							Uuid ref = (Uuid) removeIter.next();
							ConnectionResource res = (ConnectionResource) leases.remove(ref);
							//If we cannot deallocate - put it back in the
							// leases Set;
							if (!res.forceDeallocate()) {
								//System.out.println("Returning to existing
								// lease set");
								leases.put(ref, res);
							}
						}
						keystoremove.clear();
					} catch (Exception ex) {
						System.out.println(new java.util.Date() + ": Err: " + ex.getMessage());
						ex.printStackTrace();
					}
				}
				try {
					Thread.sleep(10 * 1000L);
					//Logger.getLogger("Reaper").info("waiting....");
					//					ExporterManager expMgr =
					// DefaultExporterManager.getManager("default");
					//					System.out.println("Total number of items exported is: "
					// + expMgr.getCount());
					//					System.out.print("\tResultSet:" +
					// expMgr.getCount("ResultSet"));
					//					System.out.print(" Connection:" +
					// expMgr.getCount("Connection"));
					//					System.out.print(" Participants:" +
					// expMgr.getCount("Participants"));
					//					System.out.print(" Service:" +
					// expMgr.getCount("Service"));
					//					System.out.println(" ThorReg:" +
					// expMgr.getCount("ThorListener"));
				} catch (Exception ex) {
					System.out.println(new java.util.Date() + ": Err: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
	}

	/*
	 * @see net.jini.id.ReferentUuid#getReferentUuid()
	 */
	public Uuid getReferentUuid() {
		// TODO Complete method stub for getReferentUuid
		return myId;
	}
}