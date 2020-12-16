package jasima.core.simulation;

import static jasima.core.simulation.SimContext.waitFor;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.rules.Timeout;

import jasima.core.simulation.Simulation.SimulationFailed;

public class TestComponentNames {
//	@Rule
	public Timeout globalTimeout = new Timeout(5000);

	@Test
	public void testGetComponentByName() {
		SimComponentContainerBase c = createTree();
		
		assertEquals("sub1",c.getChildByName("sub1").getName());
		assertEquals("b",((SimComponentContainer)c.getChildByName("sub2")).getChildByName("b").getName());
	}

	@Test
	public void testGetComponentByNameNonExisting() {
		SimComponentContainerBase c = createTree();
		assertEquals(null,c.getChildByName("subXXX"));
	}

	@Test
	public void testGetComponentByHierarchicalNameSub() {
		SimComponentContainerBase c = createTree();
		
		SimComponent cmp = c.getByHierarchicalName("main.sub1");
		assertEquals("sub1", cmp.getName());
		assertEquals(".main.sub1", cmp.getHierarchicalName());
	}

	@Test
	public void testGetComponentByHierarchicalNameSubSub() {
		SimComponentContainerBase c = createTree();
		
		SimComponent cmp = c.getByHierarchicalName("main.sub2.b");
		assertEquals("b",cmp.getName());
		assertEquals(".main.sub2.b",cmp.getHierarchicalName());
	}

	@Test
	public void testGetComponentByHierarchicalNameFromSimulation() {
		SimComponentContainerBase c = createTree();
		
		SimContext.of(sim->{
			sim.addComponent(c);
			
			SimComponent cmp = sim.getComponentByHierarchicalName(".main.sub2.b");
			assertEquals("b",cmp.getName());
			assertEquals(".main.sub2.b",cmp.getHierarchicalName());
		});
	}

	private SimComponentContainerBase createTree() {
		SimComponentContainerBase c = new SimComponentContainerBase("main");
		c.addChild(new SimComponentBase("sub1"));
		SimComponentContainerBase sub2 = new SimComponentContainerBase("sub2");
		sub2.addChild(new SimComponentBase("a")).addChild(new SimComponentBase("b"));
		c.addChild(sub2);
		return c;
	}

}
