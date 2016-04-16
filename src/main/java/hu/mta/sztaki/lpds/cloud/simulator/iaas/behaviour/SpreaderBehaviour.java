package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceSpreader;

/**
 * This is the base class of all the behaviour classes. This class provides the
 * common base to implement further functions.
 * 
 * @author Patrik
 *
 */
public abstract class SpreaderBehaviour extends Timed {

	/*
	 * This filed is used to give the next number to the actual object.
	 */
	private static long LineNumber = 0;

	/*
	 * This field is used to identify the individual behaviour objects.
	 */
	private final long number;

	/*
	 * The last notification time when the class got a tick.
	 */
	protected long lastNotficationTime;

	/*
	 * The total processing in the last tick.
	 */
	protected double lastTotalProcessing;

	/*
	 * The observed spreader.
	 */
	protected final ResourceSpreader observed;

	/**
	 * 
	 * @param spr
	 */
	public SpreaderBehaviour(ResourceSpreader spr) {
		observed = spr;
		lastNotficationTime = Timed.getFireCount();
		lastTotalProcessing = spr.getTotalProcessed();
		number = LineNumber;
		LineNumber++;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (number ^ (number >>> 32));
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpreaderBehaviour other = (SpreaderBehaviour) obj;
		if (number != other.number)
			return false;
		return true;
	}

	@Override
	public abstract void tick(long fires);

}
