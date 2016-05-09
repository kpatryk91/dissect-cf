package at.ac.uibk.dps.cloud.simulator.test.behaviour;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.ac.uibk.dps.cloud.simulator.test.IaaSRelatedFoundation;
import at.ac.uibk.dps.cloud.simulator.test.PMRelatedFoundation;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour.BehaviourFactory;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour.PhysicalMachineBeh;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;

public class PhysicalMachinaBehaviourTest extends IaaSRelatedFoundation {

	private PhysicalMachineBeh pm = null;
	private VirtualMachine vm = null;
	private Repository storage = null;
	private double coreNumber;
	private double perCoreProcPow;
	private long memory;
	private long storageCapacity;
	private long diskBW;
	private long netBW;
	private BehaviourFactory behFac = new BehaviourFactory();

	@Before
	public void initialize() {
		coreNumber = 4;
		perCoreProcPow = 10;
		memory = 100;
		storageCapacity = 1000;
		diskBW = 10;
		netBW = 10;

		storage = new Repository(storageCapacity, "TheDisk", netBW, netBW, diskBW, new HashMap<String, Integer>());
		pm = new PhysicalMachineBeh(coreNumber, perCoreProcPow, memory, storage, 1, 1,
				PMRelatedFoundation.defaultTransitions);
	}

	private void initializeWithVM() {

	}

	private void setDVFSBeh() {
		behFac.getPhysicalMachineBehaviour(pm, "DVFS");
	}

	private void setCoreOnOffBeh() {
		behFac.getPhysicalMachineBehaviour(pm, "CoreOnOff");
	}

	@Test
	public void timeWithoutAnyConsumption() {
		setDVFSBeh();
		pm.turnon();
		double PrepowerMin = pm.getCurrentPowerBehavior().getMinConsumption();
		double PrepowerRange = pm.getCurrentPowerBehavior().getConsumptionRange();

		Timed.simulateUntil(15);
		/*
		 * No consumption => proPower min minimum consumption is 4
		 * 
		 */
		Assert.assertEquals("PM/DVFS after DVFS totalProcessing!", 4, pm.getPerTickProcessingPower(), 0);
		/*
		 * After DVFS percore number was 10 => mininimum 1
		 */
		Assert.assertEquals("PM/DVFS after DVFS perCorePower", 1, pm.getCapacities().getRequiredProcessingPower(), 0);
		/*
		 * After DVFS free cap.
		 */
		Assert.assertEquals("PM/DVFS after DVFS freeCap perCorePower", 1,
				pm.freeCapacities.getRequiredProcessingPower(), 0);
		/*
		 * After DVFS available caps perPower.
		 */
		Assert.assertEquals("PM/DVFS after DVFS availableCap perCorePower", 1,
				pm.availableCapacities.getRequiredProcessingPower(), 0);
		/*
		 * After DVFS the core number must be the same.
		 */
		Assert.assertEquals("PM/DVFS after DVFS cores", 4, pm.getCapacities().getRequiredCPUs(), 0);
		/*
		 * Max capacity doesn't change
		 */
		Assert.assertEquals("PM/DVFS after DVFS maximumCapacity cores!", 4, pm.getMaximumCapacity().getRequiredCPUs(),
				0);
		/*
		 * Max capacity doesn't change
		 */
		Assert.assertEquals("PM/DVFS  after DVFS maximumCapacity perCorePower", 10,
				pm.getMaximumCapacity().getRequiredProcessingPower(), 0);
		// power meter error?!
		/*
		 * Power min doesn't change
		 */
		Assert.assertEquals("Physical machine DVFS consumption min", PrepowerMin,
				pm.getCurrentPowerBehavior().getMinConsumption(), 0);
		/*
		 * No consumption => powerRange decreased
		 */
		Assert.assertEquals("Physical machine DVFS consumption range", PrepowerRange / 10,
				pm.getCurrentPowerBehavior().getConsumptionRange(), 0);
	}

	@Test
	public void OfflinePMDVFS() {
		setDVFSBeh();
		Timed.simulateUntil(10);
		Assert.assertEquals("PM/DVFS total capacity", 40, pm.getPerTickProcessingPower(), 0);
		Assert.assertEquals("PM/DVFS per core capacity", 10, pm.getCapacities().getRequiredProcessingPower(), 0);

	}

	@Test
	public void OfflinePMCoreOnOff() {
		setCoreOnOffBeh();
		Timed.simulateUntil(10);
		Assert.assertEquals("PM/CoreOnOff total capacity", 40, pm.getPerTickProcessingPower(), 0);
		Assert.assertEquals("PM/CoreOnOff core capacity", 4, pm.getCapacities().getRequiredCPUs(), 0);

	}

	@Test
	public void CoreOnOffBaseTest() {
		setCoreOnOffBeh();
		pm.turnon();
		double PrepowerMin = pm.getCurrentPowerBehavior().getMinConsumption();
		double PrepowerRange = pm.getCurrentPowerBehavior().getConsumptionRange();

		Timed.simulateUntil(10);
		/*
		 * No consumption => pro power min without 3 cores => 10 total power
		 */
		Assert.assertEquals("PM/CoreOnOff after ticks!", 10, pm.getPerTickProcessingPower(), 0);
		/*
		 * The actual cores after the ticks.
		 */
		Assert.assertEquals("PM/CoreOnOff after ticks actual cores", 1, pm.getCapacities().getRequiredCPUs(), 0);
		/*
		 * The perCore power after the ticks.
		 */
		Assert.assertEquals("PM/CoreOnOff after ticks perCorePower", 10,
				pm.getCapacities().getRequiredProcessingPower(), 0);
		/*
		 * Max capacity doesn't change
		 */
		Assert.assertEquals("Physical machine CoreOnOff MaxCap cores", 4, pm.getMaximumCapacity().getRequiredCPUs(), 0);
		/*
		 * Max capacity doesn't change
		 */
		Assert.assertEquals("Physical machine CoreOnOff MaxCap corePower", 10,
				pm.getMaximumCapacity().getRequiredProcessingPower(), 0);
		/*
		 * After ticks remaining available cores.
		 */
		Assert.assertEquals("PM/DVFS after DVFS freeCaps cores", 1, pm.freeCapacities.getRequiredCPUs(), 0);
		/*
		 * After ticks remaining available cores.
		 */
		Assert.assertEquals("PM/DVFS after DVFS availableCaps cores", 1, pm.availableCapacities.getRequiredCPUs(), 0);
		/*
		 * Power minimum changed
		 */
		Assert.assertEquals("Physical machine ConOnOff consumption min", PrepowerMin / 4.0,
				pm.getCurrentPowerBehavior().getMinConsumption(), 0);
		/*
		 * Power Range Changed
		 */
		Assert.assertEquals("Physical machine CoreOnOff consumption range", PrepowerRange / 4.0,
				pm.getCurrentPowerBehavior().getConsumptionRange(), 0);
		// powermanagement error again!
	}

}
