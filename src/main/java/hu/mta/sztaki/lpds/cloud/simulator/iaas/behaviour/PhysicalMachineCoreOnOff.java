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

	private boolean isSubscribed = false;
	
	private double maxCores = ((PhysicalMachine) observed).getMaximumCapacity().getRequiredCPUs();

	private double nextCores;
	/**
	 * The previous state when the previous event came.
	 */
	private State prevState = State.OFF;

	public PhysicalMachineCoreOnOff(ResourceSpreader spr, double maximumCapacity) {
		super(spr, maximumCapacity);
		stateChanged((PhysicalMachine) spr, prevState, ((PhysicalMachine) spr).getState());
		

	}

	@Override
	protected void calculateValues() {
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

		/*
		 * Getting the needed fields.
		 */
		ResourceConstraints actualCap = ((PhysicalMachine) observed).getCapacities();

		// double oldCpu = actualCap.getRequiredCPUs();

		nextCores = Math.ceil(consumption / actualCap.getRequiredProcessingPower());
		/*
		 * The cpu number cannot rise above the limit and the processing power
		 * cannot fall under one cpu core.
		 */
		if (nextCores > maxCores) {
			nextCores = maxCores;
		}

		if (nextCores < 1) {
			nextCores = 1;
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
		PowerState powerState = observed.getCurrentPowerBehavior();
		nextPowerRange = (powerState.getConsumptionRange() / actualCap.getRequiredCPUs()) * nextCores;
		nextPowerMin = (powerState.getMinConsumption() / actualCap.getRequiredCPUs()) * nextCores;
	}

	@Override
	protected void getMachineCapacity() {
		actualMachineCapacity = (ConstantConstraints) ((PhysicalMachine) observed).getCapacities();
	}

	@Override
	protected void setMachineCapacity() {
		PhysicalMachineBeh pmb = (PhysicalMachineBeh) observed;
		pmb.setCapacity(nextCapacity);
	}

	@Override
	public void tick(long fires) {

		if (lastNotficationTime == fires) {
			return;
		}

		getMachineCapacity();
		calculateValues();
		calculateCapacity();
		lastNotficationTime = fires;
		lastTotalProcessing = observed.getTotalProcessed();
		PowerState powerState = observed.getCurrentPowerBehavior();
		powerState.setConsumptionRange(nextPowerRange);
		powerState.setMinConsumption(nextPowerMin);
		setMachineCapacity();

		// setCapacity(newCpu, actualCap.getRequiredCPUs());
	}

	@Override
	public void stateChanged(PhysicalMachine pm, State oldState, State newState) {
		if (prevState == State.OFF && newState == State.RUNNING) {
			prevState = State.RUNNING;
			lastNotficationTime = Timed.getFireCount();
			lastTotalProcessing = observed.getTotalProcessed();
			isSubscribed = true;
			this.subscribe(FREQUENCY);
		}

		if (prevState == State.RUNNING && newState != State.RUNNING) {
			prevState = State.OFF;
			if (isSubscribed == true) {
				this.unsubscribe();
			}
		}
	}

}
