package ebd.etcsLauncher.backend.utils.etcsModuleUtils;

import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * An extension to the {@link ArrayList} class that provides useful functions for checking the existence of
 * {@link ETCSModule}s and getting a specific {@link ETCSModule}. Also provides a function that inserts
 * new {@link ETCSModule}s in a sorted fashion. The sorting corresponds to the compareTo function from the
 * {@link ETCSModule} class.
 *
 * @author Lukas Geyer
 */
public class SortedETCSModuleArrayList extends ArrayList<ETCSModule> {

    public SortedETCSModuleArrayList() {super();}

    /**
     * Inserts a new {@link ETCSModule} in this {@link ArrayList} that is at the correct place according to the compareTo
     * function within the {@link ETCSModule} class.
     *
     * @param etcsModule
     *         the {@link ETCSModule} to insert
     */
    public void insertSorted(ETCSModule etcsModule) {
        int i = Collections.binarySearch(this, etcsModule);
        add(i < 0 ? -i - 1 : i, etcsModule);
    }

    /**
     * Checks if a version of this module exists in this {@link ArrayList}
     *
     * @param moduleName
     *         the name of the {@link ETCSModule}
     * @param version
     *         the version of the {@link ETCSModule}
     *
     * @return true if this version of this module exists in the {@link ArrayList}
     */
    public boolean contains(ETCSModuleNames.ModuleName moduleName, ETCSModuleVersion version) {
        return !this.stream().filter(etcsModule -> etcsModule.getModuleName().equals(moduleName) &&
                                                   etcsModule.getVersion().equals(version)).collect(Collectors.toSet()).isEmpty();
    }

    /**
     * Returns the {@link ETCSModule} in this {@link ArrayList} that matches the given name and version
     *
     * @param moduleName
     *         the name of the {@link ETCSModule}
     * @param version
     *         the version of the {@link ETCSModule}
     *
     * @return the {@link ETCSModule} in this {@link ArrayList} that matches given the name and version
     *
     * @throws java.util.NoSuchElementException
     *         if there is no {@link ETCSModule} in this {@link ArrayList} that
     *         matches the given name and version
     */
    public @NotNull ETCSModule get(ETCSModuleNames.ModuleName moduleName, ETCSModuleVersion version) {
        return this.stream().filter(etcsModule -> etcsModule.getModuleName().equals(moduleName) &&
                                                  etcsModule.getVersion().equals(version)).findFirst().orElseThrow();
    }

    @Override
    public String toString() {
        if(this.isEmpty()) {
            return "";
        }
        else {
            StringBuilder            availableModulesFormatted = new StringBuilder();
            ListIterator<ETCSModule> moduleListIterator        = this.stream().toList().listIterator();
            while(moduleListIterator.hasNext()) {
                availableModulesFormatted.append(moduleListIterator.next());
                if(moduleListIterator.hasNext()) {
                    availableModulesFormatted.append(", ");
                }
            }
            return availableModulesFormatted.toString();
        }
    }

}
