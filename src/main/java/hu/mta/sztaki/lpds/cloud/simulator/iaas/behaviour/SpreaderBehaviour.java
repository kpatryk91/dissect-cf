package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

/**
 * This is the base class of all the behaviour classes. This class provides the
 * common base to implement further functions.
 * 
 * @author Patrik
 *
 */
public class SpreaderBehaviour extends Timed {

	/*
	 * The last notification time when the class got a tick.
	 */
	protected long lastNotficationTime;

	/*
	 * The total processing in the last tick.
	 */
	protected double lastTotalProcessing;

	/*
	 * The observed spreader.
	 */
	protected final ResourceSpreader observed;

	/*
	 * The time between two ticks.
	 */
	private final long FREQUENCY = 5;

	/*
	 * The maximum capacity of the current spreader.
	 */
	protected double MAXIMUM_CAPACITY;

	/*
	 * The minimum capacity of the spreader.
	 */
	protected double ONE_PERCENT_CAPACITY;

	/*
	 * This value is the possible processingPower of the spreader.
	 */
	protected double nextProcessingPower;

	/*
	 * This value is the possible power range of the spreader.
	 */
	protected double nextPowerRange;

	/*
	 * This value is the possible power minimum of the spreader.
	 */
	protected double nextPowerMin;

	/**
	 * 
	 * @param spr
	 */
	public SpreaderBehaviour(ResourceSpreader spr, double maximumCapacity) {
		observed = spr;
		lastNotficationTime = Timed.getFireCount();
		lastTotalProcessing = spr.getTotalProcessed();
		MAXIMUM_CAPACITY = maximumCapacity;
		subscribe(FREQUENCY);
	}

	/**
	 * 
	 * @param spr
	 */
	public SpreaderBehaviour(ResourceSpreader spr, double maximumCapacity, boolean autoSubscribe) {
		observed = spr;
		lastNotficationTime = Timed.getFireCount();
		lastTotalProcessing = spr.getTotalProcessed();
		MAXIMUM_CAPACITY = maximumCapacity;
		if (autoSubscribe) {
			subscribe(FREQUENCY);
		}

	}

	protected void setMaximumAndPercentCapacity(double newMaximumCapacity) {
		MAXIMUM_CAPACITY = newMaximumCapacity;
	}

	/**
	 * This method calculates the next possibly values of the spreader. This
	 * method changes the following fields:<br>
	 * - nextProcessingPower<br>
	 * - nextPowerRange<br>
	 * - nextPowerMin<br>
	 * 
	 */
	protected void calculateValues() {

		/*
		 * Calculating the the processing power for the spreader. The processing
		 * power cannot rise above the limit and the processing power cannot
		 * fall under 1 percent of the total processing power.
		 */
		nextProcessingPower = (observed.getTotalProcessed() - lastTotalProcessing) * 1.2;
		// nextProcessingPower *= Timed.getFireCount() - lastNotficationTime;
		if (nextProcessingPower > MAXIMUM_CAPACITY) {
			nextProcessingPower = MAXIMUM_CAPACITY;
		}

		if (nextProcessingPower < ONE_PERCENT_CAPACITY) {
			nextProcessingPower = ONE_PERCENT_CAPACITY;
		}
		// because the network spreader
		// proc power cannot be zero.
		if (nextProcessingPower < 1 ) {
			nextProcessingPower = 1;
		}
		
		
		/*
		 * Updating the current fields.
		 */
		// lastTotalProcessing = observed.getTotalProcessed();
		// lastNotficationTime = Timed.getFireCount();

		/*
		 * Setting in the new values.
		 */
		// state.setConsumptionRange(newRange);
		// observed.sendNotification(this, newProcPow);
	}

	protected void calculatePowerBehaviour() {
		/*
		 * Calculating the new power range for the spreader. Theory: There is a
		 * 4 cores CPU with 10 perCorePower => sum 40 total power. The minimum
		 * energy can't change, only the range can change. The current machine
		 * has a minimum 10W/core power + 5W/core range. If the processing power
		 * fall to the half the total processing power, the new value will be
		 * half the power range of the current system. In this example: 40W
		 * minimum + 20W range. The cpu has changed: 10 perCore power => 5
		 * perCorePower. From this there will be 40W minimum + 10W range.
		 * 
		 */
		// NetworkNode doesn't have power beh???
		if (observed.getCurrentPowerBehavior() != null) {
			nextPowerRange = observed.getCurrentPowerBehavior().getConsumptionRange()
					* (nextProcessingPower / observed.getPerTickProcessingPower());
			nextPowerMin = observed.getCurrentPowerBehavior().getMinConsumption();
		}
	}
	
	@Override
	public void tick(long fires) {

		if (lastNotficationTime == fires) {
			return;
		}
		/*
		 * Calculate the new values.
		 */
		calculateValues();
		calculatePowerBehaviour();
		/*
		 * Updating the current fields.
		 */
		lastNotficationTime = fires;
		lastTotalProcessing = observed.getTotalProcessed();

		/*
		 * Setting in the new values.
		 */
		if ((observed.getCurrentPowerBehavior() != null)) {
			observed.getCurrentPowerBehavior().setConsumptionRange(nextPowerRange);
		}
		observed.sendNotification(this, nextProcessingPower);
	}

}
