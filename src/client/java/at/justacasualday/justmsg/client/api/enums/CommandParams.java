package at.justacasualday.justmsg.client.api.enums;

public enum CommandParams {
	GROUP("GroupName"), TARGET("target"), SRCGROUP("srcGroup"), DESTGROUP("destGroup"), ALIAS("alias");

	private final String argName;

	CommandParams(String argName) {
		this.argName = argName;
	}

	public String getArgName() {
		return argName;
	}
}
