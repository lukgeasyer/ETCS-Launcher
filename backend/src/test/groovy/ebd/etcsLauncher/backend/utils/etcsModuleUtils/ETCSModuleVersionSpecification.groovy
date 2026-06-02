package ebd.etcsLauncher.backend.utils.etcsModuleUtils


import spock.lang.Specification

class ETCSModuleVersionSpecification extends Specification {
    def "specify correct behavior of constructor for version"() {
        when:
        new ETCSModuleVersion("invalidVersion")

        then:
        thrown(IllegalArgumentException)
    }

    def "specify correct number of allowed points in version"() {
        when:
        new ETCSModuleVersion("1.2.3.4")

        then:
        thrown(IllegalArgumentException)
    }

    def "specify correct behaviour of compareTo()"() {
        given:
        ArrayList<ETCSModuleVersion> versionList = new ArrayList<>()
        versionList.add(new ETCSModuleVersion("1.0.0"))
        versionList.add(new ETCSModuleVersion("1.1.0"))
        versionList.add(new ETCSModuleVersion("1.0.1"))
        versionList.add(new ETCSModuleVersion("0.9.0"))
        versionList.add(new ETCSModuleVersion("1.0.0"))

        when:
        def comparison = versionList.get(i) <=> versionList.get(j)

        then:
        comparison == expectedComparison

        where:
        i | j | expectedComparison
        0 | 0 | 0
        0 | 1 | -1
        0 | 2 | -1
        3 | 2 | -1
        2 | 3 | 1
        1 | 4 | 1
        0 | 4 | 0
    }

}
