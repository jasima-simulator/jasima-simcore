package jasima.shopSim.util.modelDef;

public class RouteDef extends PropertySupport {

	public static final String PROP_NAME = "name";
	public static final String PROP_OPERATIONS = "operations";
	
	private String name;
	private OperationDef[] operations;
	private ShopDef shop;

	public OperationDef[] getOperations() {
		return operations;
	}

	public void setOperations(OperationDef[] operations) {
		firePropertyChange(PROP_OPERATIONS, this.operations,
				this.operations = operations);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROP_NAME, this.name, this.name = name);
	}

	public ShopDef getShop() {
		return shop;
	}

	public void setShop(ShopDef shop) {
		firePropertyChange("shop", this.shop, this.shop = shop);
	}

}
