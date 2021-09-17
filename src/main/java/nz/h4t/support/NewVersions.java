package nz.h4t.support;

public class NewVersions {
    private final String shortVersion;
    private final String version;

    public NewVersions(String shortVersion, String version) {
        this.shortVersion = shortVersion;
        this.version = version;
    }

    public String getShortVersion() {
        return shortVersion;
    }

    public String getVersion() {
        return version;
    }
}
