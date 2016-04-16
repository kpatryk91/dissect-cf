package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import java.util.LinkedList;
import java.util.List;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;

public class BehaviourFactory {

	public SpreaderBehaviour getPhysicalMachineBehaviour(PhysicalMachineBeh pmb, String behaviour) {
		
		if(behaviour.equals("DVFS")) {
			PhysicalMachineDVFS beh = new PhysicalMachineDVFS(pmb);
			pmb.subscribeStateChangeEvents(beh);
			pmb.behaviours.add(beh);
		}
		
		if(behaviour.equals("CoreOnOff")) {
			PhysicalMachineCoreOnOff beh = new PhysicalMachineCoreOnOff(pmb);
			pmb.subscribeStateChangeEvents(beh);
			pmb.behaviours.add(beh);
		}
		throw new IllegalStateException("ERROR: Wrong behaviour mode!");
	}

	public SpreaderBehaviour getVirtualMachineBehaviour(VirtualMachine vm, String behaviour) {
		return null;
	}

	public List<SpreaderBehaviour> getNetworkNodeBehaviour(NetworkNode nn, String behaviour) {
		if (behaviour.equals("NN_DVFS")) {
			List<SpreaderBehaviour> list = new LinkedList<SpreaderBehaviour>();
			list.add(new NetworkNodeDVFS(nn.diskinbws, nn.getMAX_INBW()));
			list.add(new NetworkNodeDVFS(nn.diskoutbws, nn.getMAX_OUTBW()));
			return list;
		}
		throw new IllegalStateException("ERROR: Wrong behaviour mode!");
	}

}
