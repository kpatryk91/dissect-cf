package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.StateChangeListener;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

class PhysicalMachineDVFS extends MachineBehaviour implements StateChangeListener {

	/**
	 * The time between two ticks.
	 */
	private final long FREQUENCY = 10000;

	/**
	 * The previous state when the previous event came.
	 */
	private State prevState = State.OFF;

	public PhysicalMachineDVFS(ResourceSpreader spr) {
		super(spr);
		stateChanged((PhysicalMachine) spr, prevState, ((PhysicalMachine) spr).getState());

	}

	@Override
	public void tick(long fires) {

		if (lastNotficationTime == fires) {
			return;
		}
		/*
		 * Getting the needed fields.
		 */
		PowerState powerState = observed.getCurrentPowerBehavior();
		ResourceConstraints actualCap = ((PhysicalMachine) observed).getCapacities();
		/*
		 * Calculating the new processing power. DVFS theory: We set the next
		 * capacity after the last processed volume. We have a cpu with 4 cores
		 * and 10 perCoreCap => Sum 40 totalCap. The new processed capacity is
		 * 13. With this algorithm the new totalProcPower will be 16.
		 */
		double procDifference = observed.getTotalProcessed() - lastTotalProcessing;
		double newperCorePower = Math.ceil((procDifference * 1.2) / actualCap.getRequiredCPUs());
		double newTotalProcPower = newperCorePower * actualCap.getRequiredCPUs();

		/*
		 * The processing power cannot rise above the limit and the processing
		 * power cannot fall under the 1 percent of the total processing power.
		 */
		if (newTotalProcPower > ((PhysicalMachine) observed).getMaximumCapacity().getTotalProcessingPower()) {
			newTotalProcPower = ((PhysicalMachine) observed).getMaximumCapacity().getTotalProcessingPower();
		}
		if (newTotalProcPower < ((PhysicalMachine) observed).getMaximumCapacity().getTotalProcessingPower() / 100) {
			newTotalProcPower = ((PhysicalMachine) observed).getMaximumCapacity().getTotalProcessingPower() / 100;
		}

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
		double newPerCoreWatt = powerState.getConsumptionRange()
				* (newTotalProcPower / actualCap.getTotalProcessingPower());

		/*
		 * Updating the current fields.
		 */
		lastNotficationTime = fires;
		lastTotalProcessing = observed.getTotalProcessed();

		/*
		 * Update with the new datas.
		 */
		powerState.setConsumptionRange(newPerCoreWatt * actualCap.getRequiredCPUs());
		setCapacity(actualCap.getRequiredCPUs(), newTotalProcPower / actualCap.getRequiredCPUs());
	}

	private void setCapacity(double cores, double perCorePower) {

		if (cores < 0 || perCorePower < 0) {
			throw new IllegalStateException(
					"ERROR: Invalid argument value: " + "cores: " + cores + " perCorePower: " + perCorePower);
		}
		PhysicalMachineBeh pmb = (PhysicalMachineBeh) observed;
		double newCore = pmb.getCapacities().getRequiredCPUs();
		double newPerCore = pmb.getCapacities().getRequiredProcessingPower();
		if (cores != 0) {
			newCore = cores;
		}

		if (perCorePower != 0) {
			newPerCore = perCorePower;
		}
		ConstantConstraints newState = new ConstantConstraints(newCore, newPerCore,
				pmb.getCapacities().getRequiredMemory());
		pmb.setCapacity(newState);
	}

	@Override
	public void stateChanged(PhysicalMachine pm, State oldState, State newState) {
		if (prevState == State.OFF && newState == State.RUNNING) {
			prevState = State.RUNNING;
			lastNotficationTime = Timed.getFireCount();
			lastTotalProcessing = observed.getTotalProcessed();
			this.subscribe(FREQUENCY);
		}

		if (prevState == State.RUNNING && newState == State.OFF) {
			prevState = State.RUNNING;
			this.unsubscribe();
		}
	}

}
