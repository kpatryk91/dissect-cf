package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader.FreqSyncer;

public interface IStateChangedNotificationObserver<T, P> {
	
	public void notifyObserver(T notificationSource, P notificationState );
	
	public FreqSyncer getObserverInnerSyncer();
}
