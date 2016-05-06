package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

/**
 * 
 * @author Patrik
 *
 */
public abstract class MachineBehaviour extends SpreaderBehaviour {

	protected ResourceConstraints actualMachineCapacity = null;

	protected ConstantConstraints nextCapacity = null;

	public MachineBehaviour(ResourceSpreader spr, double maximumCapacity) {
		super(spr, maximumCapacity, false);
	}

	protected abstract void getMachineCapacity();

	protected abstract void setMachineCapacity();

	/**
	 * This method calculate the next capacity of the current spreader.
	 */
	protected void calculateCapacity() {
		// ResourceConstraints cap = ((PhysicalMachine)
		// observed).getCapacities();
		nextCapacity = new ConstantConstraints(actualMachineCapacity.getRequiredCPUs(),
				nextProcessingPower / actualMachineCapacity.getRequiredCPUs(),
				actualMachineCapacity.getRequiredMemory());
	}

	@Override
	public abstract void tick(long fires);
}
