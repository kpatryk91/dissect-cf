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
	 * This method set the capacity for the physical machine and also update the
	 * <br>
	 * available capacity parameters of the pm.
	 * 
	 * @param con
	 */
	void setCapacity(ConstantConstraints con, SpreaderBehaviour source) {

		if (source instanceof PhysicalMachineCoreOnOff) {

			double coreDiff = ((PhysicalMachineCoreOnOff) source).getCoreDifference();
			if (coreDiff == 0) {
				return;
			}

			ConstantConstraints cc = new ConstantConstraints(coreDiff, 0, 0);

			totalCapacities = con;
			internalAvailableCaps.add(cc);
			internalReallyFreeCaps.add(cc);
			setPerTickProcessingPower(con.getTotalProcessingPower());
		}

		if (source instanceof PhysicalMachineDVFS) {
			totalCapacities = con;
			internalAvailableCaps.setProcessingPower(con.getRequiredProcessingPower());
			internalReallyFreeCaps.setProcessingPower(con.getRequiredProcessingPower());
			// ConstantConstraints cc = new ConstantConstraints(0, con.get, 0);
			setPerTickProcessingPower(con.getTotalProcessingPower());
		}

		/*
		 * Change capacity theory: freeCapacity = newCapacity -
		 * availableCapacity.
		 */
		// double difference = con.getTotalProcessingPower() -
		// internalAvailableCaps.getTotalProcessingPower();
		// setPerTickProcessingPower(con.getTotalProcessingPower());
	}

	/**
	 * Defines a new physical machine, ensures that there are no VMs running so
	 * far
	 * 
	 * @param cores
	 *            defines the number of CPU cores this machine has under control
	 * @param perCorePocessing
	 *            defines the processing capabilities of a single CPU core in
	 *            this machine (in instructions/tick)
	 * @param memory
	 *            defines the total physical memory this machine has under
	 *            control (in bytes)
	 * @param disk
	 *            defines the local physical disk & networking this machine has
	 *            under control
	 * @param onD
	 *            defines the time delay between the machine's switch on and the
	 *            first time it can serve VM requests
	 * @param offD
	 *            defines the time delay the machine needs to shut down all of
	 *            its operations while it does not serve any more VMs
	 * @param powerTransitions
	 *            determines the applied power state transitions while the
	 *            physical machine state changes. This is the principal way to
	 *            alter a PM's energy consumption behavior.
	 */
	public PhysicalMachineBeh(double cores, double perCorePocessing, long memory, Repository disk,
			double[] turnonOperations, double[] switchoffOperations,
			EnumMap<PowerStateKind, EnumMap<State, PowerState>> powerTransitions) {
		super(cores, perCorePocessing, memory, disk, turnonOperations, switchoffOperations, powerTransitions);
	}

	/**
	 * Defines a new physical machine, ensures that there are no VMs running so
	 * far
	 * 
	 * @param cores
	 *            defines the number of CPU cores this machine has under control
	 * @param perCorePocessing
	 *            defines the processing capabilities of a single CPU core in
	 *            this machine (in instructions/tick)
	 * @param memory
	 *            defines the total physical memory this machine has under
	 *            control (in bytes)
	 * @param disk
	 *            defines the local physical disk & networking this machine has
	 *            under control
	 * @param onD
	 *            defines the time delay between the machine's switch on and the
	 *            first time it can serve VM requests
	 * @param offD
	 *            defines the time delay the machine needs to shut down all of
	 *            its operations while it does not serve any more VMs
	 * @param powerTransitions
	 *            determines the applied power state transitions while the
	 *            physical machine state changes. This is the principal way to
	 *            alter a PM's energy consumption behavior.
	 */
	public PhysicalMachineBeh(double cores, double perCorePocessing, long memory, Repository disk, int onD, int offD,
			EnumMap<PowerStateKind, EnumMap<State, PowerState>> powerTransitions) {
		super(cores, perCorePocessing, memory, disk, onD, offD, powerTransitions);
	}

}
