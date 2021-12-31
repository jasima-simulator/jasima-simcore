package jasima.core.run;

import static jasima.core.run.ConsoleRunner.run;
import static jasima.core.util.converter.ArgListTokenizer.quoteString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import jasima.core.experiment.ExpTestControlFlow;
import jasima.core.experiment.Experiment;
import jasima.core.util.FileFormat;
import jasima.core.util.XmlUtil;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;

public class ConsoleRunnerTest {

	private static String fullPathExpSuccess;
	private static String fullPathExpError;

	@SuppressWarnings("serial")
	static class ExitCalledError extends Error {
	}

	static class ConsoleRunnerOverride extends ConsoleRunner {

		Integer errorCode = null;

		ConsoleRunnerOverride() {
			super();
		}

		ConsoleRunnerOverride(Experiment expTemplate) {
			super(expTemplate);
		}

		@Override
		// override exit() to make calls to System.exit testable
		void exit(int errorCode) {
			this.errorCode = errorCode;
			throw new ExitCalledError(); // stop control flow as does the original System.exit call
		}

	}

	@BeforeClass
	public static void prepareExperimentXml() throws Exception {
		File tmpExpFileSuccess;
		File tmpExpFileError;
		tmpExpFileSuccess = File.createTempFile("success", ".jasima");
		tmpExpFileSuccess.deleteOnExit();

		tmpExpFileError = File.createTempFile("error", ".jasima");
		tmpExpFileError.deleteOnExit();

		ExpTestControlFlow exp = new ExpTestControlFlow();
		exp.setName("testExperiment");
		exp.setDummy(42);

		exp.setFail(false);
		XmlUtil.saveXML(FileFormat.JASIMA_BEAN, exp, tmpExpFileSuccess);

		exp.setFail(true);
		XmlUtil.saveXML(FileFormat.JASIMA_BEAN, exp, tmpExpFileError);

		fullPathExpSuccess = quoteString(tmpExpFileSuccess.getAbsolutePath());
		fullPathExpError = quoteString(tmpExpFileError.getAbsolutePath());
	}

	@Test
	public void normalExecutionShouldNotCallExit() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(null);

		Map<String, Object> res = cr.runWith(fullPathExpSuccess);

		assertThat("errorCode", cr.errorCode, nullValue());
		assertThat("result value", res.get("results"), is(42 * 42));
	}

	@Test
	public void normalExecutionFromXmlShouldAllowSettingParameters() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(null);

		Map<String, Object> res = cr.runWith(fullPathExpSuccess, "--dummy=23");

		assertThat("errorCode", cr.errorCode, nullValue());
		assertThat("result value", res.get("results"), is(23 * 23));
	}

	@Test
	public void normalExecutionWithFullClassnameShouldAllowSettingParameters() {
		Map<String, Object> res = run(ExpTestControlFlow.class.getCanonicalName(), "--dummy=23",
				"--fail=false");

		assertThat("result value", res.get("results"), is(23 * 23));
	}

	@Test
	public void testExecutionFromPackageSearchPath() {
		String className = ExpTestControlFlow.class.getSimpleName();
		String packageName = ExpTestControlFlow.class.getPackage().getName();

		Map<String, Object> res = run(className, //
				"--dummy=23", //
				"--fail=false", //
				"--p=" + packageName);

		assertThat("result value", res.get("results"), is(23 * 23));
	}

	@Test
	public void testNormalExecutionFromTemplateExperiment() {
		Map<String, Object> res = run(new ExpTestControlFlow(), "--dummy=23", "--fail=false");

		assertThat("result value", res.get("results"), is(23 * 23));
	}

	@Test
	@Ignore
	public void testNormalExecutionFromComplexProperty() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(new ExpTestControlFlow());
		Map<String, Object> res = cr.runWith("--dummy=23", "--fail=false", "--complex.test=23");
		assertThat("result value", res.get("results"), is(23 * 23));
	}

	@Test(expected = ExitCalledError.class)
	public void templateAndExpSpecShouldNotWorkSimultaneously() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(new ExpTestControlFlow());

		try {
			cr.runWith(ExpTestControlFlow.class.getCanonicalName(), "--dummy=23", "--fail=false");
		} finally {
			assertThat("errorCode", cr.errorCode, is(1));
		}
	}

	@Test
	public void normalExecutionShouldAllowSettingComplexParameters() {
		Map<String, Object> res = run(new DynamicShopExperiment(),
				"--sequencingRule=ATC(k=2; tieBreaker=TieBreakerFASFS)");
		assertThat("numTardy", res.get("numTardy"), is(610));
	}

	@Test(expected = ExitCalledError.class)
	public void errorInExpSpecShouldSetExitCode1() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(null);
		try {
			cr.runWith("\"strangeExpDef");
		} finally {
			assertThat("errorCode", cr.errorCode, is(1));
		}
	}

	@Test(expected = ExitCalledError.class)
	public void noExpSpecShouldSetExitCode1() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride();
		try {
			cr.runWith(/* nothing */);
		} finally {
			assertThat("errorCode", cr.errorCode, is(1));
		}
	}

	@Test(expected = ExitCalledError.class)
	public void errorInParameterNameShouldSetExitCode1() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(null);
		try {
			cr.runWith(fullPathExpSuccess, "--unknownProperty=42");
		} finally {
			assertThat("errorCode", cr.errorCode, is(1));
		}
	}

	@Test(expected = ExitCalledError.class)
	public void errorInParameterValueShouldSetExitCode1() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(null);
		try {
			cr.runWith(fullPathExpSuccess, "--dummy=thisIsNotAnInt");
		} finally {
			assertThat("errorCode", cr.errorCode, is(1));
		}
	}

	@Test(expected = ExitCalledError.class)
	public void errorInParameterSetterShouldSetExitCode1() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(null);
		try {
			cr.runWith(fullPathExpSuccess, "--nonNegativeProperty=-1");
		} finally {
			assertThat("errorCode", cr.errorCode, is(1));
		}
	}

	@Test(expected = ExitCalledError.class)
	public void unhandledExceptionDuringExperimentShouldSetExitCode2() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(null);
		try {
			cr.runWith(fullPathExpError);
		} finally {
			assertThat("errorCode", cr.errorCode, is(2));
		}
	}

	@Test
	public void abortingExperimentShouldProduceNoError() {
		Map<String, Object> res = run(fullPathExpSuccess, "--abort=true", "--dummy=21");

		assertThat("result value", res.get("results"), is(21 * 21));
		assertThat("aborted", res.get(Experiment.EXP_ABORTED), is(1));
	}

	@Test(expected = ExitCalledError.class)
	public void runningWithHelpShouldExitWithCode0() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(null);
		try {
			cr.runWith("--help");
		} finally {
			assertThat("errorCode", cr.errorCode, is(0));
		}
	}

	@Test(expected = ExitCalledError.class)
	public void runningFromTemplateWithHelpShouldExitWithCode0() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(new ExpTestControlFlow());
		try {
			cr.runWith("--help");
		} finally {
			assertThat("errorCode", cr.errorCode, is(0));
		}
	}

	@Test(expected = ExitCalledError.class)
	public void helpForComplexExperimentShouldExitWithCode0() {
		ConsoleRunnerOverride cr = new ConsoleRunnerOverride(new DynamicShopExperiment());
		try {
			cr.runWith("--help");
		} finally {
			assertThat("errorCode", cr.errorCode, is(0));
		}
	}

}
