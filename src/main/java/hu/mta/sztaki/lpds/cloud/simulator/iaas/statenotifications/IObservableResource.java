package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader.FreqSyncer;

/**
 * This interface declares those methods what an object needs to implement to become an observable resource.
 * @author Patrik
 *
 * @param <T> This is the type of the observer object.
 * @param <S> This is the type of the state.
 */
public interface IObservableResource<T, S> {
	
	/**
	 * This method gives the observer back.
	 * @return <T> the observer
	 */
	public T getStateObserver();
	
	/**
	 * TThis method set the observer.
	 * @param stateObserver
	 */
	public void setStateObserver(T stateObserver);
	
	/**
	 * Send a notification to the observer.
	 * @param state The cause of the notification.
	 */
	public void notifyStateObserver(S state);
	
	/**
	 * This method gives the inner Syncer of the observer back.
	 * @return The inner FreqSyncer object.
	 */
	public FreqSyncer getStateObserverSyncer();
	
}
