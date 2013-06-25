package jasima.shopSim.util.modelDef;

import jasima.shopSim.core.WorkStation;

public class WorkstationDef extends PropertySupport {

	public static final String PROP_MACHINES = "machines";
	public static final String PROP_NAME = "name";
	public static final String PROP_SETUP_STATES = "setupStates";
	public static final String PROP_SETUP_TIMES = "setupTimes";

	private String name;
	private IndividualMachineDef[] machines;;
	private String[] setupStates = { WorkStation.DEF_SETUP_STR };
	private double[][] setupTimes = { { 0.0 } };

	public WorkstationDef() {
		super();
		setMachines(new IndividualMachineDef[] { new IndividualMachineDef() });
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROP_NAME, this.name, this.name = name);
	}

	public IndividualMachineDef[] getMachines() {
		return machines;
	}

	public void setMachines(IndividualMachineDef[] ms) {
		if (ms != null)
			for (IndividualMachineDef m : ms) {
				m.setWorkstation(this);
			}
		if (machines != null)
			for (IndividualMachineDef m : machines) {
				m.setWorkstation(null);
			}
		firePropertyChange(PROP_MACHINES, this.machines, this.machines = ms);
	}

	public String[] getSetupStates() {
		return setupStates;
	}

	public void setSetupStates(String[] setupStates) {
		firePropertyChange(PROP_SETUP_STATES, this.setupStates,
				this.setupStates = setupStates);
	}

	public double[][] getSetupTimes() {
		return setupTimes;
	}

	public void setSetupTimes(double[][] setupTimes) {
		firePropertyChange(PROP_SETUP_TIMES, this.setupTimes,
				this.setupTimes = setupTimes);
	}

}
