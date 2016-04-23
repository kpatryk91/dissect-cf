package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

class NetworkNodeDVFS extends SpreaderBehaviour {

	public NetworkNodeDVFS(ResourceSpreader spr, long maximumCapacity) {
		super(spr, maximumCapacity);

	}
	
	@Override
	public void tick(long fires) {

		
	}

}
