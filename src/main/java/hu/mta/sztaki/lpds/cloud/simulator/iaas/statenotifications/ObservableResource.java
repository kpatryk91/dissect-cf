package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

/*
 * This class task is to define some base methotds to be observable.
 */

public abstract class ObservableResource {
	
	public abstract IObserver getMyObserver();
	
	public abstract void setMyObserver(IObserver watcher);
}
