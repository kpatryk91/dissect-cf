package hu.mta.sztaki.lpds.cloud.simulator.iaas.statenotifications;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

/**
 * 
 * This interface defines the methods to the 
 * 
 * @author Patrik
 * @category Interface 
 */
public interface IObserver {
	
	void processingPowerChanged(ResourceSpreader source, double oldProcessingPower);
	
	void unsubscribeObserver();
	
	int getDepgroupLength();
	
	ResourceSpreader getObserverDependecyGroupMember(int i);
	
	void nudgeObserver();
	
	boolean observerRegularFreqMode();
	
	int observerGetFirstConsumerID();
	
	boolean observerIsSubscribed();
	
	ResourceSpreader[] observerGetDependencyGroup();
	
}
