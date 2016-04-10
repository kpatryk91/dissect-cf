package hu.mta.sztaki.lpds.cloud.simulator.iaas.behaviour;

import java.rmi.activation.Activatable;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;

public class PhysicalMachineBehaviourFactory {

	/**
	 * Behaviour factory method.
	 * 
	 * @param behaviour
	 *            <br>
	 *            DVFS : Voltage/Frequency scaling. <br>
	 *            CoreOnOff: Capacity change.<br>
	 * @return
	 */
	public BaseBehaviour getBehaviour(String behaviour) {

		if (behaviour.equals("DVFS")) {
			return new DVFS();
		}

		if (behaviour.equals("CoreOnOff")) {
			return new CoreOnOff();
		}
		throw new IllegalStateException("ERROR: Wrong behavior mode!");
	}

	private static class DVFS extends BaseBehaviour {

		private long number;

		public DVFS() {
			this.number = BaseBehaviour.number;
			BaseBehaviour.number++;
		}

		@Override
		public void tick(long fires) {

			if (fires == lastNotficationTime) {
				return;
			}
			/**
			 * DVFS elv: A kulonbseg alapjan beallitani a magonkenti
			 * teljesitmenyt. 4 cpu / 10 percCorePower => 40 totalCapacity lett
			 * 13 diff => diff * 1.2 maradjon tartalek ceil(diff/cpuszam) =>
			 * ceil(3,25) => 4 ujtotal proc 4*4 = 16
			 */
			PowerState powerState = observed.getCurrentPowerBehavior();
			ResourceConstraints actualCap = observed.getCapacities();
			double perCoreWatt = powerState.getConsumptionRange() / actualCap.getRequiredCPUs();
			// uj frekvencia kiszamitasa

			double diff = observed.getTotalProcessed() - lastTotalProcessing; // kulonbseg

			double perCoreDiff = Math.ceil(diff * 1.2 / actualCap.getRequiredCPUs()); // magonkenti
																						// (uj)teljesitmeny

			double newProcPower = perCoreDiff * actualCap.getRequiredCPUs(); // uj
																				// teljes
																				// telj
			/*
			 * Ha nagyobb az ertek, mint a maximum kapacitas, akkor a maximum
			 * kapacitas lesz az uj ertek.
			 */
			if (newProcPower > observed.getMaximumCapacity().getTotalProcessingPower()) {
				newProcPower = observed.getMaximumCapacity().getTotalProcessingPower();
			}
			double oldPerTick = actualCap.getRequiredProcessingPower(); // elmentes
																		// a
																		// powerhez
			observed.setCapacity(actualCap.getRequiredCPUs(), newProcPower / actualCap.getRequiredCPUs());
			// totalCapacities -nak új érték

			// allapot frissites
			lastNotficationTime = fires;
			lastTotalProcessing = observed.getTotalProcessed();

			/**
			 * Energy elv: CPU alapfogyasztás ala nem mehet, valtozik a
			 * magonkenti teljesitmeny => változik a range Van 4 cpu / 10
			 * pertickProcPower => 40 totalProcPower Fogyasztas: alap 10 W/cpu +
			 * 5W nov igy 40 W min + 20 W range megvaltozik a perCorePower 10 =>
			 * 5 1) Range / Core => 5 W per core 2) 5W * (mostaniPercTick /
			 * RegiPercTick) => 2.5 3) ujrange 2.5 * cpu-k szama => 2,5 * 4 =>
			 * 10.
			 */

			double newPerCoreWatt = powerState.getConsumptionRange()
					* (actualCap.getRequiredProcessingPower() / oldPerTick);
			powerState.setConsumptionRange(newPerCoreWatt * actualCap.getRequiredCPUs());
		}

		@Override
		public void addobservedPM(PhysicalMachine pm) {
			// TODO Auto-generated method stub

			observed = pm;
			lastNotficationTime = getFireCount();
			lastTotalProcessing = pm.getTotalProcessed();

			subscribe(pm.getStateObserverSyncer().getFrequency());
		}

		@Override
		public void removingBehaviour() {
			// TODO Auto-generated method stub
			unsubscribe();
			observed = null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (number ^ (number >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DVFS other = (DVFS) obj;
			if (number != other.number)
				return false;
			return true;
		}

	}

	private static class CoreOnOff extends BaseBehaviour {

		private long number;

		public CoreOnOff() {
			this.number = BaseBehaviour.number;
			BaseBehaviour.number++;
		}

		@Override
		public void tick(long fires) {
			// TODO Auto-generated method stub

			if (lastNotficationTime == getFireCount()) {
				return;
			}
			
			ResourceConstraints actualCap = observed.getCapacities();
			PowerState state = observed.getCurrentPowerBehavior();
			double oldCpu = actualCap.getRequiredCPUs();
			double oldPerCoreMin = state.getMinConsumption() / oldCpu;
			double oldPerCoreRange = state.getConsumptionRange() / oldCpu;
			/**
			 * CoreOnOff elv:
			 * 4 cpu / 10 perCorePower / 40 totalPower
			 * 25 consumption
			 */
			
			double actualConsumption = 0;
			for (ResourceConsumption con : observed.underProcessing) {
				actualConsumption += con.getRealLimit();
			}
			double newcpus = Math.ceil(actualConsumption / actualCap.getRequiredCPUs());
			
			if (newcpus > observed.getMaximumCapacity().getRequiredCPUs()) {
				newcpus = observed.getMaximumCapacity().getRequiredCPUs();
			}
			
			
			
			observed.setCapacity(newcpus, actualCap.getRequiredProcessingPower());			
			/**
			 * Energy elv: CPU kiesik, aranyosan mindket ertek csökken. Van 4
			 * cpu / 10 pertickProcPower => 40 totalProcPower Fogyasztas: alap
			 * 10 W/cpu + 5W növ igy 40 W min + 20 W range 1 cpu kiesik => lesz
			 * 30 W min + 15 W range, arányosan.
			 */
			state.setMinConsumption(newcpus * oldPerCoreMin);
			state.setConsumptionRange(newcpus * oldPerCoreRange);
			
		}

		@Override
		public void addobservedPM(PhysicalMachine pm) {
			// TODO Auto-generated method stub
			observed = pm;
			lastNotficationTime = getFireCount();
			lastTotalProcessing = pm.getTotalProcessed();

			subscribe(pm.getStateObserverSyncer().getFrequency());
		}

		@Override
		public void removingBehaviour() {
			// TODO Auto-generated method stub
			unsubscribe();
			observed = null;

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (number ^ (number >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CoreOnOff other = (CoreOnOff) obj;
			if (number != other.number)
				return false;
			return true;
		}

	}

	public static abstract class BaseBehaviour extends Timed {

		protected static long number = 0;

		protected PhysicalMachine observed;

		protected long lastNotficationTime;

		protected double lastTotalProcessing;

		public abstract void addobservedPM(PhysicalMachine pm);

		public abstract void removingBehaviour();

	}

}
