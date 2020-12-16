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
		
		assertEquals("sub1",c.getComponentByName("sub1").getName());
		assertEquals("b",((SimComponentContainer)c.getComponentByName("sub2")).getComponentByName("b").getName());
	}

	@Test
	public void testGetComponentByNameNonExisting() {
		SimComponentContainerBase c = createTree();
		assertEquals(null,c.getComponentByName("subXXX"));
	}

	@Test
	public void testGetComponentByHierarchicalNameSub() {
		SimComponentContainerBase c = createTree();
		
		SimComponent cmp = c.getComponentByHierarchicalName("main.sub1");
		assertEquals("sub1", cmp.getName());
		assertEquals(".main.sub1", cmp.getHierarchicalName());
	}

	@Test
	public void testGetComponentByHierarchicalNameSubSub() {
		SimComponentContainerBase c = createTree();
		
		SimComponent cmp = c.getComponentByHierarchicalName("main.sub2.b");
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
		c.addComponent(new SimComponentBase("sub1"));
		SimComponentContainerBase sub2 = new SimComponentContainerBase("sub2");
		sub2.addComponent(new SimComponentBase("a")).addComponent(new SimComponentBase("b"));
		c.addComponent(sub2);
		return c;
	}

}
