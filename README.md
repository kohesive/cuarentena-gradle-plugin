# cuarentena-gradle-plugin
Gradle plugin for generating Cuarentena policy files

## Usage

The plugin uses gradle's dependency configurations as an input to generate Cuarentena policy (set of allowances) files,
here's an example:

```
apply plugin: 'uy.kohesive.cuarentena'

configurations {
    kotlinLogging
}

dependencies {
    kotlinLogging group: 'io.github.microutils', name: 'kotlin-logging', version: '1.4.6'
}

cuarentena {
    kotlinLogging {
        scanMode = 'SAFE'
        excludePackages = ['org.slf4j.helpers']
        excludeClasses = ['org.slf4j.MDC']
        verbose = true
        kotlinVersion = '1.1.1'
        policyPackage = 'com.some.thing'
    }
}
```

The `cuarentena` block defines a set of configs, each corresponding to a dependency configuration by its name, and having the following parameters:

1. `kotlinVersion` (required) — kotlin standard library version to check the code against
2. `scanMode` (optional) — defines whether the code from the dependency configuration JARs would be checked against the base Java+Kotlin policy (`SAFE` mode) or will be whitelisted (`ALL` mode) no matter what. `SAFE` by default
3. `excludePackages` (optional) — a list of packages to be black-listed (no allowances would be generated) 
4. `excludeClasses` (optional) — a list of classes to be black-listed (no allowances would be generated) 
5. `verbose` (optional) — a flag to configure the plugin output verbosity (`false` by default) 
6.  `policyPackage` (optional) — defines a path within `generated-src/policy` where a policy `.ctena` file would be written to. `uy.kohesive.cuarentena` by default 