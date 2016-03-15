package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

/**
 * This is the interface what the observable class should implement.
 * @author Patrik
 *
 */

public interface IObservable {
	
	/**
	 * 
	 * @return IObservable
	 */
	public IObserver getObserver();
	
	public void setObserver(IObserver observer);
	
	public void notifyObserver(String change);
	
	public Object getObserverState(String command);
	
}
