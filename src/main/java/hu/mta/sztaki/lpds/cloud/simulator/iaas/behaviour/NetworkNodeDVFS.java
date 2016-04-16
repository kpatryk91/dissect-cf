package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

class NetworkNodeDVFS extends SpreaderBehaviour {

	private final long FREQUENCY = 10000;
	private final long MAXIMUM_CAPACITY;

	public NetworkNodeDVFS(ResourceSpreader spr, long maximumCapacity) {
		super(spr);
		MAXIMUM_CAPACITY = maximumCapacity;
		subscribe(FREQUENCY);
	}

	@Override
	public void tick(long fires) {

		if (lastNotficationTime == fires) {
			return;
		}
		/*
		 * Calculating the the processing power for the spreader. The processing
		 * power cannot rise above the limit and the processing power cannot
		 * fall under the 1 percent of the total processing power.
		 */
		double processingDiff = observed.getTotalProcessed() - lastTotalProcessing;
		double newProcPow = processingDiff * 1.2;
		if (newProcPow > MAXIMUM_CAPACITY) {
			newProcPow = MAXIMUM_CAPACITY;
		}

		if (newProcPow < MAXIMUM_CAPACITY / 100) {
			newProcPow = MAXIMUM_CAPACITY / 100;
		}
		/*
		 * Calculating the new power range for the spreader. Theory: There is a 4
		 * cores CPU with 10 perCorePower => sum 40 total power. The minimum
		 * energy can't change, only the range can change.
		 * The current machine has a minimum 10W/core power + 5W/core range.
		 * If the processing power fall to the half the total processing power,
		 * the new value will be half the power range of the current system.
		 * In this example: 40W minimum + 20W range.
		 * The cpu has changed: 10 perCore power => 5 perCorePower.
		 * From this there will be 40W minimum + 10W range.
		 * 
		 */
		PowerState state = observed.getCurrentPowerBehavior();
		double oldRange = state.getConsumptionRange();
		double newRange = oldRange * (newProcPow / lastTotalProcessing);

		/*
		 * Updating the current fields.
		 */
		lastTotalProcessing = observed.getTotalProcessed();
		lastNotficationTime = Timed.getFireCount();

		/*
		 * Setting in the new values.
		 */
		state.setConsumptionRange(newRange);
		observed.sendNotification(this, newProcPow);

	}

}
