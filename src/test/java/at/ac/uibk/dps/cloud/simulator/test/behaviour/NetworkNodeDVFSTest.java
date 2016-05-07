package at.ac.uibk.dps.cloud.simulator.test.behaviour;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.ac.uibk.dps.cloud.simulator.test.ConsumptionEventAssert;
import at.ac.uibk.dps.cloud.simulator.test.ConsumptionEventFoundation;
import at.ac.uibk.dps.cloud.simulator.test.TestFoundation;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour.BehaviourFactory;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour.SpreaderBehaviour;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;

public class NetworkNodeDVFSTest extends TestFoundation {

	public final static long inBW = 10; // bytes/tick
	public final static long outBW = 10; // bytes/tick
	public final static long diskBW = 5; // bytes/tick
	public final static int targetlat = 3; // ticks
	public final static int sourcelat = 2; // ticks
	public final static String sourceName = "Source";
	public final static String targetName = "Target";
	public final static String thirdName = "Unconnected";
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
		target = new NetworkNode(targetName, inBW, outBW, diskBW, lm);
		third = new NetworkNode(thirdName, inBW, outBW, diskBW, lm);
		n1 = behFac.getNetworkNodeBehaviour(source, "NN_DVFS");
		n2 = behFac.getNetworkNodeBehaviour(target, "NN_DVFS");
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
		Assert.assertEquals("The maximum input capacity doesn't match: ", source.getMAX_INBW(), inBW);
		Assert.assertEquals("The maximum output capacity doesn't match: ", source.getMAX_OUTBW(), outBW);
		Assert.assertEquals("The maximum input capacity doesn't match: ", target.getMAX_INBW(), inBW);
		Assert.assertEquals("The maximum input capacity doesn't match: ", target.getMAX_OUTBW(), outBW);

	}

	private void setupTransfer(final long len, final NetworkNode source, final NetworkNode target,
			final long expectedDelay) throws NetworkException {
		NetworkNode.initTransfer(len, ResourceConsumption.unlimitedProcessing, source, target,
				new ConsumptionEventAssert(Timed.getFireCount() + expectedDelay, true));
	}

	private void simulateGivenTime(int eventNum) {
		// Timed.simulateUntilLastEvent();
		Timed.simulateUntil(eventNum);
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
		setupTransfer(dataToBeStored, source, target, 6);
		simulateGivenTime(8);
		/*
		 * there is a transfer => maximum bandwidth.
		 */
		Assert.assertEquals("Expected processingPower: (no changes) ", 10, source.outbws.getPerTickProcessingPower(),
				0);
		/*
		 * No input connection, no required processing capacity.
		 */
		Assert.assertEquals("Expected source procPower: (1) ", 1, source.inbws.getPerTickProcessingPower(), 0);
		/*
		 * There there is a transfer => maximum bandwidth.
		 */
		Assert.assertEquals("Expected target procPower: ", 10, target.inbws.getPerTickProcessingPower(), 0);

		// simulateGivenTime(5);
	}

	/*
	 * After 6, there is no more consumption. New Power => 0.1
	 */
	@Test
	public void changedFreqTest() throws NetworkException {
		setupTransfer(dataToBeStored, source, target, 6);
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
}
