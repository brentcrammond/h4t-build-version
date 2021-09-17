import com.github.zafarkhaja.semver.Version;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This MOJO will update the build version and timestamp, in the specified application.properties file.
 * <p>
 * The build.version will bump the patch and build number (semver based) and wil update the build.timestamp.
 */
@Mojo(name = "BumpBuildVersion", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class BumpBuildVersion extends AbstractMojo {
    @Parameter(required = true)
    private String propertyFilePath;

    @Parameter(defaultValue = "build.version")
    private String buildVersionProperty;

    @Parameter(defaultValue = "build.short.version")
    private String buildShortVersionProperty;

    @Parameter(defaultValue = "build.timestamp")
    private String buildTimestampProperty;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            var file = new File(propertyFilePath);
            List<String> lns = FileUtils.readLines(file, "UTF-8");

            var ver = lns.stream()
                    .map(this::findVersion)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(Version.valueOf("1.0.0+build.1000"));

            lns = lns.stream()
                    .map(ln -> updateVersion(ver, ln))
                    .map(ln -> updateShortVersion(ver, ln))
                    .map(this::updateTimestamp)
                    .collect(Collectors.toList());

            FileUtils.writeLines(file, "UTF-8", lns);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to execute plugin", e);
        }
    }

    //
    // Internal Methods...
    //

    private Version findVersion(String ln) {
        Pattern pat = Pattern.compile("^(" + buildVersionProperty + "[ ]*=[ ]*)(.*)$");
        var m = pat.matcher(ln);
        if (m.matches()) {
            try {
                var lead = m.group(1);
                var semver = m.group(2);
                var v = Version.valueOf(semver);
                var preRel = v.getPreReleaseVersion();
                var buildNo = v.getBuildMetadata();
                v = v.incrementPatchVersion();
                if (preRel != null && !"".equals(preRel)) {
                    v = v.setPreReleaseVersion(preRel);
                }
                if (buildNo != null && !"".equals(buildNo)) {
                    v = v.setBuildMetadata(buildNo);
                    v = v.incrementBuildMetadata();
                }
                getLog().info("New Build Version  : " + v.toString());
                return v;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private String updateVersion(Version v, String ln) {
        Pattern pat = Pattern.compile("^(" + buildVersionProperty + "[ ]*=[ ]*)(.*)$");
        if (pat.matcher(ln).matches()) {
            ln = buildVersionProperty + "=" + v.toString();
        }
        return ln;
    }

    private String updateShortVersion(Version v, String ln) {
        Pattern pat = Pattern.compile("^(" + buildShortVersionProperty + "[ ]*=[ ]*)(.*)$");
        if (pat.matcher(ln).matches()) {
            ln = buildShortVersionProperty + "=" + v.toString();
        }
        return ln;
    }

    private String updateTimestamp(String ln) {
        Pattern pat = Pattern.compile("^(" + buildTimestampProperty + "[ ]*=[ ]*)(.*)$");
        var m = pat.matcher(ln);
        if (m.matches()) {
            var lead = m.group(1);
            var ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE d-MMM-yyyy HH:mm"));
            ln = lead + ts;
            getLog().info("Build Timestamp: " + ts);
        }
        return ln;
    }
}
