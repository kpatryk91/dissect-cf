package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

/**
 * This interface defines the methods to 
 * @author Patrik
 *
 */

public interface IObservable {
	
	IObserver getMyObserver();
	
	void setMyObserver(IObserver watcher);
	
	// TODO: change Observer
}
