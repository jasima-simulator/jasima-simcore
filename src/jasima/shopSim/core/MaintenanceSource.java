package jasima.shopSim.core;

/**
 * This class can be used to model scheduled maintenance. Maintenance intervals
 * and duration are modeled using the {@code timeBetweenFailures} and
 * {@code timeToRepair} settings. The difference to {@link DowntimeSource} is
 * that {@code timeBetweenFailures} uses the <b>beginning</b> of the last maintenance
 * as the reference, whereas {@link DowntimeSource} uses the <b>end</b> of the last
 * downtime.
 * <p>
 * A simple example: consider a machine that is going to be down every 24 hours
 * for a duration of 1 hour, i.e., it is available for processing for 23 hours.
 * Using {@code MaintenanceSource}, this is modeled setting
 * {@code timeBetweenFailures} to 24 hours and using a {@code timeToRepair} of 1
 * hour.
 * 
 * @see DowntimeSource
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2014-04-16
 * @version "$Id$"
 */
public class MaintenanceSource extends DowntimeSource {

	private double lastDeactivate;

	public MaintenanceSource(IndividualMachine machine) {
		super(machine);
	}

	@Override
	public void init() {
		super.init();
		lastDeactivate = getMachine().workStation.shop().simTime();
	}

	@Override
	protected double calcDeactivateTime(JobShop shop) {
		lastDeactivate = lastDeactivate + getTimeBetweenFailures().nextDbl();
		return lastDeactivate;
	}

}
