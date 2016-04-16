package at.ac.uibk.dps.cloud.simulator.test.simple;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;

public class NetworkNodeDVFSTest {

	private NetworkNode nn = new NetworkNode("1", 100, 100, 100, new HashMap<String, Integer>() );
	
	@Test
	public void test() {
		//nn.changeNodeIOCapacity(-1);
		assertTrue(true);
	}

}
