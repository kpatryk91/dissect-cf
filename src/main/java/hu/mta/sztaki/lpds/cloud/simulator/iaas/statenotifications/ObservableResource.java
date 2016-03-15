package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

/*
 * This class task is to define some base methotds to be observable.
 */

public abstract class ObservableResource {
	
	public abstract IObserver getObserver();
	
	public abstract void setObserver(IObserver observer);
	
	public abstract void notifyObserver(String change);
	
	public abstract Object getObserverState(String command);
}
