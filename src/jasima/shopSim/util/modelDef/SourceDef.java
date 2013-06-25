package jasima.shopSim.util.modelDef;

public class SourceDef extends PropertySupport {
	
	public static final String PROP_SHOP = "shop";
	public static final String PROP_NAME = "name";

	private String name;
	private ShopDef shop;

	public ShopDef getShop() {
		return shop;
	}

	public void setShop(ShopDef shop) {
		firePropertyChange(PROP_SHOP, this.shop, this.shop = shop);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROP_NAME, this.name, this.name = name);
	}

}
