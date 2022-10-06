package com.hiber.test.base;

class MigrationScriptVersion {
	private final int major;
	private final int minor;
	private final int fix;

	MigrationScriptVersion(final int major, final int minor, final int fix) {
		this.major = major;
		this.minor = minor;
		this.fix = fix;
	}

	static MigrationScriptVersion fromDotSeparatedString(final String versionAsString) {
		final String[] fragmentedVersion = versionAsString.split("\\.");
		if (fragmentedVersion.length != 3)
			throw new RuntimeException("Version should contain major, minor and fix. Example: '2.10.3'");

		return new MigrationScriptVersion(
				Integer.parseInt(fragmentedVersion[0]),
				Integer.parseInt(fragmentedVersion[1]),
				Integer.parseInt(fragmentedVersion[2])
		);
	}

	String toDotSeparatedString() {
		return major + "." + minor + "." + fix;
	}

	String toUnderscoreSeparatedString() {
		return major + "_" + minor + "_" + fix;
	}

	String format() {
		return major + "." + minor + "_" + fix;
	}

	MigrationScriptVersion getPrevious() {
		if (fix == 0)
			throw new RuntimeException("Cannot get previous version for fix == 0");
		return new MigrationScriptVersion(major, minor, fix - 1);
	}
}
