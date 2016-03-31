package at.ac.uibk.dps.cloud.simulator.test.simple;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import at.ac.uibk.dps.cloud.simulator.test.PMRelatedFoundation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader.FreqSyncer;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;


public class PhysicalMachinaStateChangeTest {

	private PhysicalMachine pm = null;
	private VirtualMachine vm = null;
	private Repository storage = null;
	private double coreNumber;
	private double perCoreProcPow;
	private long memory;
	private long storageCapacity;
	private long diskBW;
	private long netBW;
	
	
	@Before
	public void initialize() {
		coreNumber = 4;
		perCoreProcPow = 10000;
		memory = 16 * 1024 * 1024 * 1024;
		storageCapacity = 16 * 1024 * 1024 * 1024 * 1024;
		diskBW = 500 * 1024 * 1024;
		netBW = 100 * 1024 * 1024;
		
		
		storage = new Repository(storageCapacity, "TheDisk", netBW, netBW, diskBW, null);
		pm = new PhysicalMachine(coreNumber, perCoreProcPow, memory, storage,
				1, 1, PMRelatedFoundation.defaultTransitions);
		
		//vm = new VirtualMachine(va);
		//FreqSyncer syncer = new FreqSyncer(pm, vm);
		
		
	}
	
	
	@Test
	public void test() {
		
		org.junit.Assert.assertTrue(true);;
	}

}
