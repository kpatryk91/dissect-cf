package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import java.util.Set;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.StateChangeListener;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

class PhysicalMachineDVFS extends MachineBehaviour implements StateChangeListener {

	/**
	 * The time between two ticks.
	 */
	private final long FREQUENCY = 5;

	private boolean isSubscribed = false;

	/**
	 * The previous state when the previous event came.
	 */
	private State prevState = State.OFF;

	public PhysicalMachineDVFS(PhysicalMachine spr, double maximumCapacity) {
		super(spr, maximumCapacity);
		stateChanged((PhysicalMachine) spr, prevState, ((PhysicalMachine) spr).getState());

	}

	@Override
	protected void calculateValues() {
		// TODO Auto-generated method stub
		super.calculateValues();

		/*
		 * PM power minimum 1 per core.
		 */
		if (nextProcessingPower / actualMachineCapacity.getRequiredCPUs() < 1) {
			nextProcessingPower = 1 * actualMachineCapacity.getRequiredCPUs();
			if (observed.getCurrentPowerBehavior() != null) {
				nextPowerRange = observed.getCurrentPowerBehavior().getConsumptionRange()
						* (nextProcessingPower / observed.getPerTickProcessingPower());
				//nextPowerMin = observed.getCurrentPowerBehavior().getMinConsumption();
			}
		}
		// The cpu corepower cannot be lower than the offered capacity to the
		// vms.
		// ???
		//
		boolean calculate = true;
		if (calculate) {
			Set<VirtualMachine> vms = ((PhysicalMachine) observed).publicVms;
			double perCoreNext = nextProcessingPower / actualMachineCapacity.getRequiredCPUs();
			double maxcap = 0;
			for (VirtualMachine vm : vms) {
				ResourceAllocation allocation = vm.getResourceAllocation();
				if (allocation != null) {
					double vmcap = allocation.getRealAllocatedCorePower();
					if (maxcap < vmcap) {
						maxcap = vmcap;
					}
				}
			}
			if (maxcap > perCoreNext) {
				perCoreNext = maxcap;
			}
			/*
			 * Can be thw vm perCore power greater than the maximum power of the
			 * pm?
			 */
			if (perCoreNext > ((PhysicalMachine) observed).getMaximumCapacity().getRequiredProcessingPower()) {
				perCoreNext = ((PhysicalMachine) observed).getMaximumCapacity().getRequiredProcessingPower();
			}
			nextProcessingPower = perCoreNext * actualMachineCapacity.getRequiredCPUs();
		}
	}

	@Override
	protected void getMachineCapacity() {
		// TODO Auto-generated method stub
		actualMachineCapacity = ((PhysicalMachine) observed).getCapacities();
	}

	@Override
	protected void setMachineCapacity() {
		// TODO Auto-generated method stub
		PhysicalMachineBeh pmb = (PhysicalMachineBeh) observed;
		pmb.setCapacity(nextCapacity, this);
	}

	
	
	@Override
	public void tick(long fires) {

		if (lastNotficationTime == fires) {
			return;
		}
		getMachineCapacity();
		calculateValues();
		calculatePowerBehaviour();
		calculateCapacity();
		lastNotficationTime = fires;
		lastTotalProcessing = observed.getTotalProcessed();
		if (observed.getCurrentPowerBehavior() != null) {
			observed.getCurrentPowerBehavior().setConsumptionRange(nextPowerRange);
		}
		setMachineCapacity();

		//////////////////////////////////////////////////////////////////
		/*
		 * Getting the needed fields.
		 * 
		 * PowerState powerState = observed.getCurrentPowerBehavior();
		 * ResourceConstraints actualCap = ((PhysicalMachine)
		 * observed).getCapacities();
		 * 
		 * Calculating the new processing power. DVFS theory: We set the next
		 * capacity after the last processed volume. We have a cpu with 4 cores
		 * and 10 perCoreCap => Sum 40 totalCap. The new processed capacity is
		 * 13. With this algorithm the new totalProcPower will be 16.
		 * 
		 * double procDifference = observed.getTotalProcessed() -
		 * lastTotalProcessing; double newperCorePower =
		 * Math.ceil((procDifference * 1.2) / actualCap.getRequiredCPUs());
		 * double newTotalProcPower = newperCorePower *
		 * actualCap.getRequiredCPUs();
		 * 
		 * 
		 * The processing power cannot rise above the limit and the processing
		 * power cannot fall under the 1 percent of the total processing power.
		 * 
		 * if (newTotalProcPower > ((PhysicalMachine)
		 * observed).getMaximumCapacity().getTotalProcessingPower()) {
		 * newTotalProcPower = ((PhysicalMachine)
		 * observed).getMaximumCapacity().getTotalProcessingPower(); } if
		 * (newTotalProcPower < ((PhysicalMachine)
		 * observed).getMaximumCapacity().getTotalProcessingPower() / 100) {
		 * newTotalProcPower = ((PhysicalMachine)
		 * observed).getMaximumCapacity().getTotalProcessingPower() / 100; }
		 * 
		 * 
		 * Calculating the new power range for the spreader. Theory: There is a
		 * 4 cores CPU with 10 perCorePower => sum 40 total power. The minimum
		 * energy can't change, only the range can change. The current machine
		 * has a minimum 10W/core power + 5W/core range. If the processing power
		 * fall to the half the total processing power, the new value will be
		 * half the power range of the current system. In this example: 40W
		 * minimum + 20W range. The cpu has changed: 10 perCore power => 5
		 * perCorePower. From this there will be 40W minimum + 10W range.
		 * 
		 * 
		 * double newPerCoreWatt = powerState.getConsumptionRange()
		 * (newTotalProcPower / actualCap.getTotalProcessingPower());
		 * 
		 * 
		 * Updating the current fields.
		 * 
		 * lastNotficationTime = fires; lastTotalProcessing =
		 * observed.getTotalProcessed();
		 * 
		 * 
		 * Update with the new datas.
		 * 
		 * powerState.setConsumptionRange(newPerCoreWatt *
		 * actualCap.getRequiredCPUs());
		 * setCapacity(actualCap.getRequiredCPUs(), newTotalProcPower /
		 * actualCap.getRequiredCPUs());
		 */
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
				isSubscribed = false;
				this.unsubscribe();
			}
		}
	}

}
