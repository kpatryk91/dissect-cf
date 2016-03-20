package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

/**
 * 
 * This interface defines the methods to the 
 * 
 * @author Patrik
 * @category Interface
 * @deprecated 
 */
public interface IObserver {
	
	void update(ResourceSpreader source, String command);
	
	Object getState(String command);
}
