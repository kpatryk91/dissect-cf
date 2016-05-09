package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

public class VirtualMachineDVFS extends MachineBehaviour implements VirtualMachine.StateChange {

	private VirtualMachine.State prevState = State.SHUTDOWN;

	private boolean isSubscribed = false;

	private ResourceAllocation allocation;

	private boolean isCalculable = false;

	public VirtualMachineDVFS(ResourceSpreader spr) {
		super(spr, 0);
		stateChanged((VirtualMachine) observed, prevState, ((VirtualMachine) observed).getState());
	}

	@Override
	protected void getMachineCapacity() {
		// get the resources of the virtual machine
		allocation = ((VirtualMachine) observed).getResourceAllocation();
		// get the real allocated resources of the virtual machine
		actualMachineCapacity = (ResourceConstraints) allocation.getRealAllocated();
		// maximum capacity setting
		setMaximumAndPercentCapacity(actualMachineCapacity.getTotalProcessingPower() * 1.2);
	}

	@Override
	protected void setMachineCapacity() {

		allocation.getHost().reallocateResources((VirtualMachine) observed, nextCapacity);
		allocation = null;
		nextCapacity = null;
	}

	@Override
	protected void calculateValues() {
		// TODO Auto-generated method stub
		super.calculateValues();

		/*
		 * VM power minimum 1 per core.
		 */
		if (actualMachineCapacity.getRequiredCPUs() < 1) {
			nextProcessingPower = actualMachineCapacity.getRequiredCPUs();
			if (observed.getCurrentPowerBehavior() != null) {
				nextPowerRange = observed.getCurrentPowerBehavior().getConsumptionRange()
						* (nextProcessingPower / observed.getPerTickProcessingPower());
				// nextPowerMin =
				// observed.getCurrentPowerBehavior().getMinConsumption();
			}
		}
	}

	@Override
	protected void calculateCapacity() {
		// TODO Auto-generated method stub
		/*
		 * If newPower < realAllocated total power
		 */
		if (nextProcessingPower <= actualMachineCapacity.getTotalProcessingPower()) {

			/*
			 * The machine has no consumption.
			 */
			if (nextProcessingPower == ONE_PERCENT_CAPACITY
					|| nextProcessingPower == actualMachineCapacity.getRequiredCPUs()) {
				nextCapacity = new ConstantConstraints(1, nextProcessingPower / actualMachineCapacity.getRequiredCPUs(),
						actualMachineCapacity.getRequiredMemory());
				return;
			}

			/*
			 * Set the machine capacity lower.
			 */
			double newPowerPower = nextProcessingPower / actualMachineCapacity.getRequiredCPUs();
			nextCapacity = new ConstantConstraints(actualMachineCapacity.getRequiredCPUs(), newPowerPower,
					actualMachineCapacity.getRequiredMemory());
			return;
			/*
			 * If newPower > allocation - realAllocated total power
			 */
		} else {
			// Available maximum frequency.
			double maxFreq = Math.min(allocation.getAllocatedResources().getRequiredProcessingPower(),
					allocation.getHost().getCapacities().getRequiredProcessingPower());

			double calcFreq = nextProcessingPower = actualMachineCapacity.getRequiredCPUs();
			/*
			 * Can I server the processing need with greater freq?
			 */
			if (calcFreq <= maxFreq) {
				nextCapacity = new ConstantConstraints(actualMachineCapacity.getRequiredCPUs(), calcFreq,
						actualMachineCapacity.getRequiredMemory());
			} else {
				// Virtually allocated cpus, max cpus
				double maxVirtCpu = allocation.getAllocatedResources().getRequiredCPUs();
				/*
				 * Is another capacity that I can use?
				 */
				double freeCpus = allocation.getHost().freeCapacities.getRequiredCPUs();
				double freeCpusAllocated = 0;
				if (freeCpus > 0) {
					for (int i = 1; i <= freeCpus; i++) {
						double changedCores = i + actualMachineCapacity.getRequiredCPUs();
						if (changedCores <= allocation.getAllocatedResources().getRequiredCPUs()) {
							if (changedCores * maxFreq >= nextProcessingPower) {
								nextCapacity = new ConstantConstraints(changedCores, maxFreq,
										actualMachineCapacity.getRequiredMemory());
								break;
							}
						} else {
							break;
						}
					}
					nextCapacity = new ConstantConstraints(actualMachineCapacity.getRequiredCPUs() + freeCpusAllocated,
							maxFreq, actualMachineCapacity.getRequiredMemory());
				} else {
					/*
					 * No other cpus, maximum capacity available with maxFreq.
					 */
					nextCapacity = new ConstantConstraints(actualMachineCapacity.getRequiredCPUs(), maxFreq,
							actualMachineCapacity.getRequiredMemory());
				}
			}

		}

	}

	@Override
	public void stateChanged(VirtualMachine vm, State oldState, State newState) {

		if (newState == State.DESTROYED) {
			if (isSubscribed) {
				isSubscribed = false;
				unsubscribe();
			}
			prevState = State.DESTROYED;
			vm.unsubscribeStateChange(this);
			return;
		}

		if (prevState != State.RUNNING && newState == State.RUNNING) {
			if (((VirtualMachine) observed).getResourceAllocation() != null) {
				lastNotficationTime = Timed.getFireCount();
				lastTotalProcessing = observed.getTotalProcessed();
				isSubscribed = true;
				prevState = State.RUNNING;
				subscribe(10);
			}
			return;
		}

		if (prevState == State.RUNNING && newState != State.RUNNING) {
			isSubscribed = false;
			unsubscribe();
			prevState = newState;
			return;
		}

	}

	@Override
	public void tick(long fires) {

		if (lastNotficationTime == fires) {
			return;
		}

		if (((VirtualMachine) observed).getResourceAllocation() == null) {
			return;
		}
		getMachineCapacity();
		calculateValues();
		calculateCapacity();
		calculatePowerBehaviour();
		lastNotficationTime = fires;
		lastTotalProcessing = observed.getTotalProcessed();
		PowerState state = observed.getCurrentPowerBehavior();
		if (state != null) {
			state.setConsumptionRange(nextPowerRange);
			state.setMinConsumption(nextPowerMin);
		}
		setMachineCapacity();

	}

}
