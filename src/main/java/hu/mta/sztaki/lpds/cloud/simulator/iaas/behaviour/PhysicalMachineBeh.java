package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;

public class PhysicalMachineBeh extends PhysicalMachine {

	List<SpreaderBehaviour> behaviours = new LinkedList<SpreaderBehaviour>();

	void setCapacity(ConstantConstraints con) {
		totalCapacities = con;
		setPerTickProcessingPower(con.getTotalProcessingPower());
	}

	public PhysicalMachineBeh(double cores, double perCorePocessing, long memory, Repository disk,
			double[] turnonOperations, double[] switchoffOperations,
			EnumMap<PowerStateKind, EnumMap<State, PowerState>> powerTransitions) {
		super(cores, perCorePocessing, memory, disk, turnonOperations, switchoffOperations, powerTransitions);
		// TODO Auto-generated constructor stub
	}

	public PhysicalMachineBeh(double cores, double perCorePocessing, long memory, Repository disk, int onD, int offD,
			EnumMap<PowerStateKind, EnumMap<State, PowerState>> powerTransitions) {
		super(cores, perCorePocessing, memory, disk, onD, offD, powerTransitions);
		// TODO Auto-generated constructor stub
	}

}
