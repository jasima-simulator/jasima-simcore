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

/**
 * Simple test class that contains a property of every primitive type.
 */
public class ExpTestPrimitives extends Experiment {

	private static final long serialVersionUID = -5887015972864118660L;

	private boolean exceptionDuringExecution;
	
	private boolean exceptionWhenSetToTrue= false;

	private boolean bool1;
	private Boolean bool2;
	private int int1;
	private Integer int2;
	private byte byte1;
	private Byte byte2;
	private short short1;
	private Short short2;
	private float float1;
	private Float float2;
	private double double1;
	private Double double2;
	private long long1;
	private Long long2;
	private char char1;
	private Character char2;
	private Number number;
	private String string;

	private int field = 1;

	public ExpTestPrimitives() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		field = 2;
	}

	@Override
	protected void performRun() {
		if (exceptionDuringExecution)
			throw new RuntimeException("some run error");
		field = 3;
	}

	@Override
	protected void produceResults() {
		super.produceResults();

		resultMap.put("field", field);
	}

	public boolean isBool1() {
		return bool1;
	}

	public void setBool1(boolean bool1) {
		this.bool1 = bool1;
	}

	public Boolean getBool2() {
		return bool2;
	}

	public void setBool2(Boolean bool2) {
		this.bool2 = bool2;
	}

	public int getInt1() {
		return int1;
	}

	public void setInt1(int int1) {
		this.int1 = int1;
	}

	public Integer getInt2() {
		return int2;
	}

	public void setInt2(Integer int2) {
		this.int2 = int2;
	}

	public byte getByte1() {
		return byte1;
	}

	public void setByte1(byte byte1) {
		this.byte1 = byte1;
	}

	public Byte getByte2() {
		return byte2;
	}

	public void setByte2(Byte byte2) {
		this.byte2 = byte2;
	}

	public short getShort1() {
		return short1;
	}

	public void setShort1(short short1) {
		this.short1 = short1;
	}

	public Short getShort2() {
		return short2;
	}

	public void setShort2(Short short2) {
		this.short2 = short2;
	}

	public float getFloat1() {
		return float1;
	}

	public void setFloat1(float float1) {
		this.float1 = float1;
	}

	public Float getFloat2() {
		return float2;
	}

	public void setFloat2(Float float2) {
		this.float2 = float2;
	}

	public double getDouble1() {
		return double1;
	}

	public void setDouble1(double double1) {
		this.double1 = double1;
	}

	public Double getDouble2() {
		return double2;
	}

	public void setDouble2(Double double2) {
		this.double2 = double2;
	}

	public long getLong1() {
		return long1;
	}

	public void setLong1(long long1) {
		this.long1 = long1;
	}

	public Long getLong2() {
		return long2;
	}

	public void setLong2(Long long2) {
		this.long2 = long2;
	}

	public char getChar1() {
		return char1;
	}

	public void setChar1(char char1) {
		this.char1 = char1;
	}

	public Character getChar2() {
		return char2;
	}

	public void setChar2(Character char2) {
		this.char2 = char2;
	}

	public Number getNumber() {
		return number;
	}

	public void setNumber(Number number) {
		this.number = number;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public boolean isExceptionDuringExecution() {
		return exceptionDuringExecution;
	}

	public void setExceptionDuringExecution(boolean exceptionDuringExecution) {
		this.exceptionDuringExecution = exceptionDuringExecution;
	}

	public boolean isExceptionWhenSetToTrue() {
		return exceptionWhenSetToTrue;
	}

	public void setExceptionWhenSetToTrue(boolean exceptionWhenSetToTrue) {
		if (exceptionWhenSetToTrue) {
			throw new IllegalArgumentException("Can't be set to true.");
		}
		this.exceptionWhenSetToTrue = exceptionWhenSetToTrue;
	}

}
