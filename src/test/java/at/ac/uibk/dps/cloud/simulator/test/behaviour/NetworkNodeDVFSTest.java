package at.ac.uibk.dps.cloud.simulator.test.behaviour;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import at.ac.uibk.dps.cloud.simulator.test.ConsumptionEventAssert;
import at.ac.uibk.dps.cloud.simulator.test.TestFoundation;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour.BehaviourFactory;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour.SpreaderBehaviour;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;

public class NetworkNodeDVFSTest extends TestFoundation {

	private final static long inBW = 10; // bytes/tick
	private final static long outBW = 10; // bytes/tick
	private final static long diskBW = 10; // bytes/tick
	private final static int targetlat = 0; // ticks
	private final static int sourcelat = 0; // ticks
	private final static String sourceName = "Source";
	private final static String targetName = "Target";
	private final static String thirdName = "Unconnected";
	NetworkNode source, target, third;
	private final static long seconds = 10;
	static final long dataToBeSent = seconds * inBW;
	static final long dataToBeStored = seconds * diskBW / 2;
	BehaviourFactory behFac = new BehaviourFactory();
	List<SpreaderBehaviour> n1, n2, n3;

	public static HashMap<String, Integer> setupALatencyMap() {
		HashMap<String, Integer> lm = new HashMap<String, Integer>();
		lm.put(sourceName, sourcelat);
		lm.put(targetName, targetlat);
		return lm;
	}

	@Before
	public void nodeSetup() {
		HashMap<String, Integer> lm = setupALatencyMap();
		source = new NetworkNode(sourceName, inBW, outBW, diskBW, lm);
		target = new NetworkNode(targetName, inBW, outBW, diskBW, lm, true);
		third = new NetworkNode(thirdName, inBW, outBW, diskBW, lm);
		n1 = behFac.getNetworkNodeBehaviour(source, "NN_DVFS");
		//n2 = behFac.getNetworkNodeBehaviour(target, "NN_DVFS");
	}

	@Test(timeout = 100)
	public void checkConstruction() {
		Assert.assertTrue("Node toString does not contain node name:", source.toString().contains(sourceName));
		Assert.assertEquals("Unexpected incoming bandwidth", inBW, source.getInputbw());
		Assert.assertEquals("Unexpected outgoing bandwidth", outBW, source.getOutputbw());
		// Assert.assertEquals("Unexpected disk bandwidth", diskBW,
		// source.getDiskbw());
		Assert.assertEquals("Unexpected name", sourceName, source.getName());
		Assert.assertEquals("Already used some bandwidth without requesting transfers", 0,
				source.inbws.getTotalProcessed() + source.outbws.getTotalProcessed(), 0);
	}

	@Test
	public void checkMaximumCapacity() {
		Assert.assertEquals("Source: The maximum input capacity doesn't match: ", source.getMAX_INBW(), inBW);
		Assert.assertEquals("Source: The maximum output capacity doesn't match: ", source.getMAX_OUTBW(), outBW);
		Assert.assertEquals("Target: The maximum input capacity doesn't match: ", target.getMAX_INBW(), inBW);
		Assert.assertEquals("Target: The maximum input capacity doesn't match: ", target.getMAX_OUTBW(), outBW);

	}

	private void setupTransfer(final long len, final NetworkNode source, final NetworkNode target,
			final long expectedDelay) throws NetworkException {
		NetworkNode.initTransfer(len, ResourceConsumption.unlimitedProcessing, source, target,
				new ConsumptionEventAssert(Timed.getFireCount() + expectedDelay, true));
	}

	private long currentTime = 0;

	@Before
	public void resetCurrentTime() {
		currentTime = 0;
	}

	private void simulateGivenTime(int eventNum) {
		// Timed.simulateUntilLastEvent();
		currentTime += eventNum;
		Timed.simulateUntil(currentTime);
		// Assert.assertEquals("Not enough consumption events received",
		// eventNum,
		// ConsumptionEventAssert.hits.size());
	}

	/*
	 * Test with full capacity Tested with Spreader freq 5
	 * 
	 */
	@Test
	public void testDVFS() throws NetworkException {
		setupTransfer(dataToBeSent, source, target, 10);
		simulateGivenTime(10);
		/*
		 * There is a transfer => maximum bandwidth.
		 */
		Assert.assertEquals("Source: Expected processingPower: (no changes) ", 10,
				source.outbws.getPerTickProcessingPower(), 0);
		Assert.assertEquals("Target: Expected processingPower: (no changes) ", 10,
				target.inbws.getPerTickProcessingPower(), 0);
		/*
		 * No input connection, no required processing capacity.
		 */
		Assert.assertEquals("Expected source procPower: (1) ", 1, source.inbws.getPerTickProcessingPower(), 0);
		/*
		 * There there is a transfer => maximum bandwidth.
		 */
		Assert.assertEquals("Expected target procPower: ", 1, target.outbws.getPerTickProcessingPower(), 0);

		// simulateGivenTime(5);
	}

	/*
	 * After 6, there is no more consumption. New Power => 0.1
	 */
	@Test
	public void changedFreqTest() throws NetworkException {
		setupTransfer(dataToBeSent, source, target, 10);
		simulateGivenTime(15);
		/*
		 * After transfer => lower capacity
		 */
		Assert.assertEquals("Expected source freq change!", 1, source.outbws.getPerTickProcessingPower(), 0);
		/*
		 * After transfer => lower capacity
		 */
		Assert.assertEquals("Expected target freq change! ", 1, target.inbws.getPerTickProcessingPower(), 0);

	}

	/*
	 * 										10 PP ----- 10PP 					
	 *                                    /                 \
	 * 						 / -- 4.8 cap/					8.4PP
	 *           1PP        /							      	\ 1 PP					
	 *
	 */
	@Test
	public void capacityChangesTest() throws NetworkException {
		/*
		 * Simulate 10 ticks to reduce the ProcPower of the spreader to 1.
		 */
		simulateGivenTime(10);
		/*
		 * Setup the transfer
		 */
		setupTransfer(5, source, target, 5);

		simulateGivenTime(5);
		/*
		 * After the first dvfs. It was 1 PP for every ticks 4 PP *1.2 => 4.8 PP
		 * 
		 */
		Assert.assertEquals("Source: After first dvfs the OUTPUT capacity needs to increase.", 4.8,
				source.outbws.getPerTickProcessingPower(), 0);
		Assert.assertEquals("Target: After first dvfs the INPUT capacity needs to increase.", 4.8,
				target.inbws.getPerTickProcessingPower(), 0);
		Assert.assertEquals("Source: After first dvfs the INPUT capacity needs to be the same. ", 1,
				source.inbws.getPerTickProcessingPower(), 0);
		Assert.assertEquals("Target: After first dvfs the OUTPUT capacity needs to be the same. ", 1,
				target.outbws.getPerTickProcessingPower(), 0);
		/*
		 * Setup an another transfer. 10 ProcPower
		 */
		setupTransfer(10, source, target, 2);

		simulateGivenTime(6);
		/*
		 * The spreader must operate at maximum capacity(10).
		 */
		Assert.assertEquals("Source: The spreader must operate at maximum capacity!", 10,
				source.outbws.getPerTickProcessingPower(), 0);
		Assert.assertEquals("Target: The spreader must operate at maximum capacity!", 10,
				target.inbws.getPerTickProcessingPower(), 0);
		/*
		 * Setup another maximum capacity transfer
		 */
		setupTransfer(10, source, target, 1);

		simulateGivenTime(6);
		/*
		 * The spreader still must operate at maximum capacity.
		 */
		Assert.assertEquals("Source: Again maximum capacity", 10, source.outbws.getPerTickProcessingPower(), 0);
		Assert.assertEquals("Target: Again maximum capacity", 10, target.inbws.getPerTickProcessingPower(), 0);
		/*
		 * Setup a lower transfer.
		 */
		setupTransfer(7, source, target, 1);

		simulateGivenTime(6);
		/*
		 * 7 * 1.2 => 8.4 The spreader must operate at 8.4 procPower.
		 */
		Assert.assertEquals("Source: After lower transfer.", 8.4, source.outbws.getPerTickProcessingPower(), 0);
		Assert.assertEquals("Target: After lower transfer.", 8.4, target.inbws.getPerTickProcessingPower(), 0);
		simulateGivenTime(6);
		/*
		 * No transfer, minimum capacity.
		 */
		Assert.assertEquals("Source: After no transfer, mininmum capacity", 1,
				source.outbws.getPerTickProcessingPower(), 0);
		Assert.assertEquals("Target: After no transfer, mininmum capacity", 1, target.inbws.getPerTickProcessingPower(),
				0);
	}
}
