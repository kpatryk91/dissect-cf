package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

class PhysicalMachineCoreOnOff extends MachineBehaviour implements PhysicalMachine.StateChangeListener {

	/**
	 * The time between two ticks.
	 */
	private final long FREQUENCY = 10000;

	/**
	 * The previous state when the previous event came.
	 */
	private State prevState = State.OFF;

	public PhysicalMachineCoreOnOff(ResourceSpreader spr) {
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
		double oldCpu = actualCap.getRequiredCPUs();

		/*
		 * CoreOnOff theory: First, we need to get the consumer's consumption
		 * need. Then we divide with the perCorePower to calculate how many cpus
		 * we need to serve the needed capacity.
		 * 
		 */
		double consumption = 0;
		for (ResourceConsumption con : observed.underProcessing) {
			consumption += con.getRealLimit();
		}

		double newCpu = Math.ceil(consumption / actualCap.getRequiredProcessingPower());
		/*
		 * The cpu number cannot rise above the limit and the processing power
		 * cannot fall under one cpu core.
		 */
		if (newCpu > ((PhysicalMachine) observed).getMaximumCapacity().getRequiredCPUs()) {
			newCpu = ((PhysicalMachine) observed).getMaximumCapacity().getRequiredCPUs();
		}

		if (newCpu < 1) {
			newCpu = 1;
		}

		/*
		 * Calculating the new power range for the spreader. Theory: There is a
		 * 4 cores CPU with 10 perCorePower => sum 40 total power. The minimum
		 * energy can change and the range can change, too. The current machine
		 * has a minimum 10W/core power + 5W/core range. If the new cpu number
		 * is half the old cpu number, then the new value will be half the
		 * current power. In this example: 40W minimum + 20W range. The cpu has
		 * changed: from 4 to 2 cpus. From this there will be 20W minimum + 10W
		 * range.
		 * 
		 */
		double oldPerCoreWattRange = powerState.getConsumptionRange() / actualCap.getRequiredCPUs();
		double oldPerCoreWattMin = powerState.getMinConsumption() / actualCap.getRequiredCPUs();

		/*
		 * Updating the current fields.
		 */
		lastNotficationTime = fires;
		lastTotalProcessing = observed.getTotalProcessed();

		/*
		 * Update with the new datas.
		 */
		powerState.setConsumptionRange(oldPerCoreWattRange * newCpu);
		powerState.setMinConsumption(oldPerCoreWattMin * newCpu);
		setCapacity(newCpu, actualCap.getRequiredCPUs());
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
