package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

public class VirtualMachineDVFS extends MachineBehaviour implements VirtualMachine.StateChange {

	private VirtualMachine.State prevState = State.SHUTDOWN;

	private boolean isSubscribed = false;

	public VirtualMachineDVFS(ResourceSpreader spr) {
		super(spr, 0);
		ResourceAllocation ra = ((VirtualMachine) spr).getResourceAllocation();
		setMaximumAndPercentCapacity(
				ra.getPMFreeCapacities().getTotalProcessingPower() + ra.allocated.getTotalProcessingPower());

	}

	@Override
	protected void getMachineCapacity() {
		actualMachineCapacity = (ConstantConstraints) ((VirtualMachine) observed).getResourceAllocation().allocated;
		/*
		 * ResourceAllocation ra = ((VirtualMachine)
		 * observed).getResourceAllocation(); UnalterableConstraintsPropagator
		 * pmfree = ra.getPMFreeCapacities(); actualMachineCapacity = new
		 * ConstantConstraints(ra.allocated.getRequiredCPUs(),
		 * ra.allocated.getRequiredProcessingPower() +
		 * pmfree.getRequiredProcessingPower(),
		 * ra.allocated.getRequiredMemory());
		 */
	}

	@Override
	protected void setMachineCapacity() {
		PhysicalMachine pm = ((VirtualMachine) observed).getResourceAllocation().getHost();
		try {
			((VirtualMachine) observed).getResourceAllocation().release();
			ResourceAllocation all = pm.allocateResources((ResourceConstraints) nextCapacity, false,
					PhysicalMachine.defaultAllocLen);
			// state = not running ?
			((VirtualMachine) observed).setResourceAllocation(all);
		} catch (VMManagementException e) {

		}
	}

	@Override
	public void stateChanged(VirtualMachine vm, State oldState, State newState) {

		if (newState == State.DESTROYED) {
			if (isSubscribed) {
				isSubscribed = false;
				unsubscribe();
			}
			vm.unsubscribeStateChange(this);
		}

		if (prevState == State.SHUTDOWN && newState == State.RUNNING) {
			lastNotficationTime = Timed.getFireCount();
			lastTotalProcessing = observed.getTotalProcessed();
			isSubscribed = true;
			subscribe(10);
		}

		if (prevState == State.RUNNING && newState != State.RUNNING) {
			isSubscribed = false;
			unsubscribe();
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
		lastNotficationTime = fires;
		lastTotalProcessing = observed.getTotalProcessed();
		observed.getCurrentPowerBehavior().setConsumptionRange(nextPowerRange);
		observed.getCurrentPowerBehavior().setMinConsumption(nextPowerMin);
		setMachineCapacity();

	}

}
