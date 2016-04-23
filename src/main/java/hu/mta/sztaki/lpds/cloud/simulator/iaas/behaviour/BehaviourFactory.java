package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import java.util.LinkedList;
import java.util.List;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;

public class BehaviourFactory {

	public SpreaderBehaviour getPhysicalMachineBehaviour(PhysicalMachineBeh pmb, String behaviour) {

		if(pmb == null) {
			throw new IllegalStateException("ERROR: PM cannot be null!");
		}
		
		if (behaviour.equals("DVFS")) {
			PhysicalMachineDVFS beh = new PhysicalMachineDVFS(pmb, pmb.getMaximumCapacity().getTotalProcessingPower());
			pmb.subscribeStateChangeEvents(beh);
			pmb.behaviours.add(beh);
		}

		if (behaviour.equals("CoreOnOff")) {
			PhysicalMachineCoreOnOff beh = new PhysicalMachineCoreOnOff(pmb,
					pmb.getMaximumCapacity().getTotalProcessingPower());
			pmb.subscribeStateChangeEvents(beh);
			pmb.behaviours.add(beh);
		}
		throw new IllegalStateException("ERROR: Wrong behaviour mode!");
	}

	public SpreaderBehaviour getVirtualMachineBehaviour(VirtualMachine vm, String behaviour) {
		if (vm == null) {
			throw new IllegalStateException("ERROR: VirtualMachine cannot be null!");
		}
		if (behaviour.equals("DVFS")) {
			VirtualMachineDVFS vmbeh= new VirtualMachineDVFS(vm);
			vm.subscribeStateChange(vmbeh);
			return new VirtualMachineDVFS(vm);
		}
		return null;
	}

	public List<SpreaderBehaviour> getNetworkNodeBehaviour(NetworkNode nn, String behaviour) {
		
		if(nn == null) {
			throw new IllegalStateException("ERROR: NetworkNode cannot be null!");
		}
		
		if (behaviour.equals("NN_DVFS")) {
			List<SpreaderBehaviour> list = new LinkedList<SpreaderBehaviour>();
			list.add(new SpreaderBehaviour(nn.diskinbws, nn.getMAX_INBW(), true));
			list.add(new SpreaderBehaviour(nn.diskoutbws, nn.getMAX_OUTBW(), true));
			return list;
		}
		throw new IllegalStateException("ERROR: Wrong behaviour mode!");
	}

}
