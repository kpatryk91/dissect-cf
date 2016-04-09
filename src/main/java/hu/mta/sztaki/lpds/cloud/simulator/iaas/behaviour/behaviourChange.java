package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

public class behaviourChange {

	private String message;
	private double capacityChange;

	public behaviourChange(String message, double capacityChange) {
		super();
		this.message = message;

		this.capacityChange = capacityChange;
	}

	public String getMessage() {
		return message;
	}

	public double getCapacityChange() {
		return capacityChange;
	}

}
