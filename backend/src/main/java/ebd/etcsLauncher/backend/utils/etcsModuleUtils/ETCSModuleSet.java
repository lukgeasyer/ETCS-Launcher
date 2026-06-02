package ebd.etcsLauncher.backend.utils.etcsModuleUtils;

import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;

import java.util.HashSet;

/**
 * An extension to the {@link HashSet} class that provides useful functions for checking the existence of
 * {@link ETCSModule}s and getting a specific {@link ETCSModule}.
 *
 * @author Lukas Geyer
 */
public class ETCSModuleSet extends HashSet<ETCSModule> {

    public ETCSModuleSet() {super();}

    /**
     * Specifies the add function. An {@link ETCSModule} can not be added if another version (or the same version)
     * of this {@link ETCSModule} is already in the {@link HashSet}.
     *
     * @param etcsModule
     *         element whose presence in this collection is to be ensured
     *
     * @return true if the {@link ETCSModule} was added to the {@link HashSet}, false otherwise
     */
    @Override
    public boolean add(ETCSModule etcsModule) {
        if(this.contains(etcsModule.getModuleName())) {return false;}
        return super.add(etcsModule);
    }

    /**
     * Checks if the {@link HashSet} contains this {@link ETCSModule}
     *
     * @param moduleName
     *         the name of this {@link ETCSModule}
     *
     * @return true if the {@link HashSet} contains a version of this {@link ETCSModule}, false otherwise
     */
    public Boolean contains(ETCSModuleNames.ModuleName moduleName) {
        return this.stream().anyMatch(etcsModule -> etcsModule.getModuleName().equals(moduleName));
    }

    /**
     * Returns the {@link ETCSModule} within this {@link HashSet} that matches the
     * {@link ETCSModuleNames.ModuleName}
     *
     * @param moduleName
     *         the name of the {@link ETCSModule} to get
     *
     * @return the {@link ETCSModule} in this {@link HashSet} that matches the given name
     *
     * @throws java.util.NoSuchElementException
     *         is there is no {@link ETCSModule} within this {@link HashSet} that matches the name
     */
    public ETCSModule get(ETCSModuleNames.ModuleName moduleName) {
        return this.stream()
                   .filter(etcsModule -> etcsModule.getModuleName() == moduleName)
                   .findFirst()
                   .orElseThrow();
    }

    @Override
    public String toString() {
        if(this.isEmpty()) {
            return "";
        }
        else {
            StringBuilder availableModulesFormatted = new StringBuilder();
            availableModulesFormatted.append("\n");
            this.forEach(etcsModule -> availableModulesFormatted.append(etcsModule.toString()).append("\n"));
            return availableModulesFormatted.toString();
        }
    }

}
