package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;

public class PhysicalMachineBeh extends PhysicalMachine {

	List<SpreaderBehaviour> behaviours = new LinkedList<SpreaderBehaviour>();
	
	/**
	 * This method set the capacity for the physical machine and also update the<br>
	 * available capacity parameters of the pm.
	 * @param con
	 */
	void setCapacity(ConstantConstraints con, SpreaderBehaviour source) {
		
		if (source instanceof PhysicalMachineCoreOnOff) {
			totalCapacities = con;
			ConstantConstraints cc = new ConstantConstraints(((PhysicalMachineCoreOnOff)source).getCoreDifference(), 0, 0);
			internalAvailableCaps.add(cc);
			internalReallyFreeCaps.add(cc);
			setPerTickProcessingPower(con.getTotalProcessingPower());
		}
		
		if (source instanceof PhysicalMachineDVFS) {
			totalCapacities = con;
			internalAvailableCaps.setProcessingPower(con.getRequiredProcessingPower());
			internalReallyFreeCaps.setProcessingPower(con.getRequiredProcessingPower());
			//ConstantConstraints cc = new ConstantConstraints(0, con.get, 0);
			setPerTickProcessingPower(con.getTotalProcessingPower());
		}
		
		/*
		 * Change capacity theory:
		 * freeCapacity = newCapacity - availableCapacity.
		 */
		//double difference = con.getTotalProcessingPower() - internalAvailableCaps.getTotalProcessingPower();
		//setPerTickProcessingPower(con.getTotalProcessingPower());
	}

	public PhysicalMachineBeh(double cores, double perCorePocessing, long memory, Repository disk,
			double[] turnonOperations, double[] switchoffOperations,
			EnumMap<PowerStateKind, EnumMap<State, PowerState>> powerTransitions) {
		super(cores, perCorePocessing, memory, disk, turnonOperations, switchoffOperations, powerTransitions);
	}

	public PhysicalMachineBeh(double cores, double perCorePocessing, long memory, Repository disk, int onD, int offD,
			EnumMap<PowerStateKind, EnumMap<State, PowerState>> powerTransitions) {
		super(cores, perCorePocessing, memory, disk, onD, offD, powerTransitions);
	}

}
