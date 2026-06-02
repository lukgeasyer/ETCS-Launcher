package ebd.etcsLauncher.backend.utils.etcsModuleUtils;

import com.fasterxml.jackson.annotation.JsonValue;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a wrapper around the version of an ETCS module. It implements Comparable for comparison of versions.
 *
 * @author Lukas Geyer
 */
public class ETCSModuleVersion implements Comparable<ETCSModuleVersion> {

    private final String version;

    public ETCSModuleVersion(String version) {
        if(version == null) {throw new IllegalArgumentException("Version can not be null");}
        if(!version.matches("[0-9]+(\\.[0-9]+){0,2}")) {throw new IllegalArgumentException("Invalid version format");}
        this.version = version;
    }

    @JsonValue
    public final String get() {
        return this.version;
    }

    /**
     * The ordering is as you would expect, so 1.3.4 > 1.3.3, 1.2.4 > 1.1.4 and 2.0.0 > 1.9.9 for example.
     *
     * @param that
     *         the object to be compared.
     *
     * @return negative integer, zero, or a positive integer as this {@link ETCSModuleVersion} is less than, equal to, or greater
     * than that {@link ETCSModuleVersion}.
     */
    @Override
    public int compareTo(@NotNull ETCSModuleVersion that) {
        int leadingVersionOther = Integer.parseInt(that.get().split("\\.")[0]);
        int leadingVersionThis  = Integer.parseInt(this.get().split("\\.")[0]);

        if(leadingVersionOther == leadingVersionThis) {
            int secondLeadingVersionOther = Integer.parseInt(that.get().split("\\.")[1]);
            int secondLeadingVersionThis  = Integer.parseInt(this.get().split("\\.")[1]);
            if(secondLeadingVersionOther == secondLeadingVersionThis) {
                int thirdLeadingVersionOther = Integer.parseInt(that.get().split("\\.")[2]);
                int thirdLeadingVersionThis  = Integer.parseInt(this.get().split("\\.")[2]);
                return thirdLeadingVersionThis - thirdLeadingVersionOther;
            }
            else {
                return secondLeadingVersionThis - secondLeadingVersionOther;
            }
        }
        else {
            return leadingVersionThis - leadingVersionOther;
        }
    }

    @Override
    public boolean equals(Object that) {
        if(this == that) {return true;}
        if(that == null) {return false;}
        if(this.getClass() != that.getClass()) {return false;}
        return this.compareTo((ETCSModuleVersion) that) == 0;
    }

}
