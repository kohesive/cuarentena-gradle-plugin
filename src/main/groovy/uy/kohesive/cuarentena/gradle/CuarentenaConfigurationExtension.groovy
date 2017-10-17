package uy.kohesive.cuarentena.gradle

class CuarentenaConfigurationExtension {
    String name

    ScanMode scanMode = ScanMode.SAFE
    Boolean excludeKotlin = true

    List<String> excludePackages = Collections.emptyList()
    List<String> excludeClasses = Collections.emptyList()

    CuarentenaConfigurationExtension(String name) {
        this.name = name
    }
}