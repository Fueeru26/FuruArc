buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
    // Force all Kotlin artifacts (including the compiler) to 1.6.10 (must exactly match Compose compiler 1.1.1).
    // The log showed the compiler actually in use was Kotlin 1.4.x, not 1.6.x.
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion("1.6.10")
            }
        }
    }
}
