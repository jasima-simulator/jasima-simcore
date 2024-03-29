/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.core.experiment;

public class ExpTestControlFlow extends Experiment {
	private static final long serialVersionUID = 1L;
	
	public static class ComplexPropertyHolder {
		private int test;

		public int getTest() {
			return test;
		}

		public void setTest(int test) {
			this.test = test;
		}
		
	}

	private boolean fail = true;
	private boolean abort = false;
	private long runtimeMillis = -1;
	private int dummy = 23;
	private int nonNegativeProperty = 0;
	private ComplexPropertyHolder complex = new ComplexPropertyHolder();

	@Override
	protected void performRun() {
		if (runtimeMillis>0) {
			try {
				Thread.sleep(runtimeMillis);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		checkCancelledOrInterrupted();
		
		if (isFail()) {
			throw new MyRuntimeException();
		}
		
		if (isAbort()) {
			aborted = 1;
		}
	}

	@Override
	protected void produceResults() {
		super.produceResults();
		resultMap.put("results", getDummy() * getDummy());
	}

	public boolean isFail() {
		return fail;
	}

	public void setFail(boolean fail) {
		this.fail = fail;
	}

	public int getDummy() {
		return dummy;
	}

	public void setDummy(int dummy) {
		this.dummy = dummy;
	}

	public long getRuntimeMillis() {
		return runtimeMillis;
	}

	public void setRuntimeMillis(long runtime) {
		this.runtimeMillis = runtime;
	}

	public boolean isAbort() {
		return abort;
	}

	public void setAbort(boolean abort) {
		this.abort = abort;
	}

	public int getNonNegativeProperty() {
		return nonNegativeProperty;
	}

	public void setNonNegativeProperty(int nonNegativeProperty) {
		if (nonNegativeProperty<0) {
			throw new IllegalArgumentException();
		}
		this.nonNegativeProperty = nonNegativeProperty;
	}

	public ComplexPropertyHolder getComplex() {
		return complex;
	}

	public void setComplex(ComplexPropertyHolder complex) {
		this.complex = complex;
	}
}