package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

public class VirtualMachineDVFS extends MachineBehaviour implements VirtualMachine.StateChange {

	private VirtualMachine.State prevState = State.SHUTDOWN;

	public VirtualMachineDVFS(ResourceSpreader spr) {
		super(spr);

	}

	@Override
	public void stateChanged(VirtualMachine vm, State oldState, State newState) {
		if (prevState == State.SHUTDOWN && newState == State.RUNNING) {

		}

		if (prevState == State.RUNNING && newState == State.SHUTDOWN) {

		}

	}

	@Override
	public void tick(long fires) {

	}

}
