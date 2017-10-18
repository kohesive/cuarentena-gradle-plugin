package uy.kohesive.cuarentena.gradle

class CuarentenaConfigurationExtension {
    String name

    String kotlinVersion

    ScanMode scanMode = ScanMode.SAFE
    Boolean excludeKotlin = true

    List<String> excludePackages = Collections.emptyList()
    List<String> excludeClasses = Collections.emptyList()

    Boolean verbose = false
    String policyPackage = "uy.kohesive.cuarentena"

    CuarentenaConfigurationExtension(String name) {
        this.name = name
    }
}