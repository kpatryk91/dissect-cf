package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader.FreqSyncer;

/**
 * The base generics notification interface.
 * 
 * @author Patrik
 *
 * @param <T> The notification's source
 * @param <P> The notification's state
 */

public interface IStateChangedNotificationObserver<T, P> {
	
	/**
	 * Notify the observer.
	 * @param notificationSource
	 * @param notificationState
	 */
	
	public void notifyObserver(T notificationSource, P notificationState );
	
	/**
	 * Returns the inner inner syncer.
	 * @return the inner syncer object
	 */
	public FreqSyncer getObserverInnerSyncer();
}
