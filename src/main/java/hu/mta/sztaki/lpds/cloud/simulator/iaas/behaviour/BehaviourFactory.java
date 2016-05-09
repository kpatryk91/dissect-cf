package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import java.util.LinkedList;
import java.util.List;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;

public class BehaviourFactory {

	/**
	 * 
	 * @param pmb this value cannot be null.
	 * @param behaviour value: DVFS dynamic scaling behaviour<br>
	 * value: CoreOnOff core 
	 * @return
	 */
	public SpreaderBehaviour getPhysicalMachineBehaviour(PhysicalMachineBeh pmb, String behaviour) {

		if(pmb == null || !(pmb instanceof PhysicalMachineBeh)) {
			throw new IllegalStateException("ERROR: PM cannot be null!");
		}
		
		if (behaviour.equalsIgnoreCase("DVFS")) {
			PhysicalMachineDVFS beh = new PhysicalMachineDVFS(pmb, pmb.getMaximumCapacity().getTotalProcessingPower());
			pmb.subscribeStateChangeEvents(beh);
			pmb.behaviours.add(beh);
			return beh;
		}

		if (behaviour.equalsIgnoreCase("CoreOnOff")) {
			PhysicalMachineCoreOnOff beh = new PhysicalMachineCoreOnOff(pmb,
					pmb.getMaximumCapacity().getTotalProcessingPower());
			pmb.subscribeStateChangeEvents(beh);
			pmb.behaviours.add(beh);
			return beh;
		}
		throw new IllegalStateException("ERROR: Wrong behaviour mode!");
	}

	/**
	 * 
	 * @param vm this value cannot be null.
	 * @param behaviour value: DVFS dynamic scalng behaviour
	 * @return 
	 */
	public SpreaderBehaviour getVirtualMachineBehaviour(VirtualMachine vm, String behaviour) {
		if (vm == null || !(vm instanceof VirtualMachine)) {
			throw new IllegalStateException("ERROR: VirtualMachine cannot be null!");
		}
		if (behaviour.equalsIgnoreCase("DVFS")) {
			VirtualMachineDVFS vmbeh= new VirtualMachineDVFS(vm);
			vm.subscribeStateChange(vmbeh);
			return new VirtualMachineDVFS(vm);
		}
		return null;
	}

	public List<SpreaderBehaviour> getNetworkNodeBehaviour(NetworkNode nn, String behaviour) {
		
		if(nn == null || !(nn instanceof NetworkNode)) {
			throw new IllegalStateException("ERROR: NetworkNode cannot be null!");
		}
		
		if (behaviour.equalsIgnoreCase("NN_DVFS")) {
			List<SpreaderBehaviour> list = new LinkedList<SpreaderBehaviour>();
			list.add(new SpreaderBehaviour(nn.inbws, nn.getMAX_INBW(), true));
			list.add(new SpreaderBehaviour(nn.outbws, nn.getMAX_OUTBW(), true));
			return list;
		}
		throw new IllegalStateException("ERROR: Wrong behaviour mode!");
	}

}
