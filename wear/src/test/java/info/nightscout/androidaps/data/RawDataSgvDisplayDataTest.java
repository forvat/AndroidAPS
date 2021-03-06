buildscript {
    repositories {
        jcenter()
        maven { url "https://plugins.gradle.org/m2/" } // jacoco 0.2
    }

    dependencies {
        //classpath 'com.dicedmelon.gradle:jacoco-android:0.1.4'
        classpath 'com.hiya:jacoco-android:0.2'
    }
}
apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'
apply plugin: 'kotlin-kapt'
apply plugin: 'com.google.gms.google-services'
//apply plugin: 'jacoco-android'
apply plugin: 'com.hiya.jacoco-android'
apply plugin: 'com.google.firebase.crashlytics'

jacoco {
    toolVersion = "0.8.3"
}

ext {
    powermockVersion = "2.0.7"
    dexmakerVersion = "1.2"
    retrofit2Version = '2.9.0'
    okhttp3Version = '4.8.1'
}


repositories {
    jcenter { url "https://jcenter.bintray.com/" }
    mavenCentral()
    google()
}

def generateGitBuild = { ->
    StringBuilder stringBuilder = new StringBuilder()
    try {
        def stdout = new ByteArrayOutputStream()
        exec {
            commandLine 'git', 'describe', '--always'
            standardOutput = stdout
        }
        String commitObject = stdout.toString().trim()
        stringBuilder.append(commitObject)
    } catch (ignored) {
        stringBuilder.append('NoGitSystemAvailable')
    }
    return stringBuilder.toString()
}

def generateGitRemote = { ->
    StringBuilder stringBuilder = new StringBuilder()
    try {
        def stdout = new ByteArrayOutputStream()
        exec {
            commandLine 'git', 'remote', 'get-url', 'origin'
            standardOutput = stdout
        }
        String commitObject = stdout.toString().trim()
        stringBuilder.append(commitObject)
    } catch (ignored) {
        stringBuilder.append('NoGitSystemAvailable')
    }
    return stringBuilder.toString()
}

def generateDate = { ->
    StringBuilder stringBuilder = new StringBuilder()
    stringBuilder.append((new Date()).format('yyyy.MM.dd-HH:mm'))
    return stringBuilder.toString()
}

def isMaster = { ->
    return !version.contains('-')
}

def gitAvailable = { ->
    StringBuilder stringBuilder = new StringBuilder()
    try {
        def stdout = new ByteArrayOutputStream()
        exec {
            commandLine 'git', '--version'
            standardOutput = stdout
        }
        String commitObject = stdout.toString().trim()
        stringBuilder.append(commitObject)
    } catch (ignored) {
        return false // NoGitSystemAvailable
    }
    return !stringBuilder.toString().isEmpty()

}

def allCommited = { ->
    StringBuilder stringBuilder = new StringBuilder()
    try {
        def stdout = new ByteArrayOutputStream()
        exec {
            commandLine 'git', 'status', '-s'
            standardOutput = stdout
        }
        String commitObject = stdout.toString().trim()
        stringBuilder.append(commitObject)
    } catch (ignored) {
        return false // NoGitSystemAvailable
    }
    return stringBuilder.toString().isEmpty()

}

tasks.matching { it instanceof Test }.all {
    testLogging.events = ["failed", "skipped", "started"]
    testLogging.exceptionFormat = "full"
}

android {
    compileSdkVersion 28
    ndkVersion "21.1.6352462"

    defaultConfig {
        minSdkVersion 24
        targetSdkVersion 28
        multiDexEnabled true
        versionCode 1500
        version "2.6.7-dev"
        buildConfigField "String", "VERSION", '"' + version + '"'
        buildConfigField "String", "BUILDVERSION", '"' + generateGitBuild() + '-' + generateDate() + '"'
        buildConfigField "String", "REMOTE", '"' + generateGitRemote() + '"'
        buildConfigField "String", "HEAD", '"' + generateGitBuild() + '"'
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        // if you change minSdkVersion to less than 11, you need to change executeTask for wear

        ndk {
            moduleName "BleCommandUtil"
        }
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
    lintOptions {
        checkReleaseBuilds false
        disable 'MissingTranslation'
        disable 'ExtraTranslation'
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
        debug {
            testCoverageEnabled(project.hasProperty('coverage'))
        }
        firebaseDisable {
            System.setProperty("disableFirebase", "true")
            ext.enableCrashlytics = false
        }
    }
    productFlavors {
        flavorDimensions "standard"
        full {
            applicationId "info.nightscout.androidaps"
            dimension "standard"
            resValue "string", "app_name", "AndroidAPS"
            versionName version
            manifestPlaceholders = [
                    appIcon     : "@mipmap/ic_launcher