plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "org.egon12.renderscripttutorial"
        minSdkVersion(23)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {

            postprocessing {
                isRemoveUnusedCode = false
                isRemoveUnusedResources = false
                isObfuscate = false
                isOptimizeCode = false
            }
        }
    }
}

dependencies {
    //    implementation(fileTree(dir= "libs", include =  ['*.jar'])
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.0")

    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.0.0")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.1.1")
    androidTestImplementation("androidx.test:rules:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1")
}

tasks.create < Exec > ("pie") {
    val emulator = android.sdkDirectory.path + "/emulator/emulator"
    val detach = "> /dev/null 2> /dev/null < /dev/null &"
    commandLine(
            "sh",
            "-c",
            "$emulator @pie $detach"
    )
}

tasks.create < Exec > ("hsv") {
    dependsOn("installDebug")
    commandLine(
            android.adbExecutable.path,
            "shell",
            "am",
            "start-activity",
            "-n",
            "org.egon12.renderscripttutorial/.MainActivity",
            "-d hsv"
    )
}

tasks.create < Exec > ("rs_input") {
    dependsOn("installDebug")
    commandLine(
            android.adbExecutable.path,
            "shell",
            "am",
            "start-activity",
            "-n",
            "org.egon12.renderscripttutorial/.MainActivity",
            "-d uv_hue"
    )
}

tasks.create < Exec > ("composite") {
    dependsOn("installDebug")
    commandLine(
            android.adbExecutable.path,
            "shell",
            "am",
            "start-activity",
            "-n",
            "org.egon12.renderscripttutorial/.MainActivity",
            "-d composite"
    )
}

tasks.create < Exec > ("convolution") {
    dependsOn("installDebug")
    commandLine(
            android.adbExecutable.path,
            "shell",
            "am",
            "start-activity",
            "-n",
            "org.egon12.renderscripttutorial/.MainActivity",
            "-d convolution"
    )
}

tasks.create < Exec > ("colorfilter") {
    dependsOn("installDebug")
    commandLine(
            android.adbExecutable.path,
            "shell",
            "am",
            "start-activity",
            "-n",
            "org.egon12.renderscripttutorial/.MainActivity",
            "-d colorfilter"
    )
}



tasks.create < Exec > ("stop") {
    commandLine(
            android.adbExecutable.path,
            "shell",
            "am",
            "force-stop",
            "org.egon12.renderscripttutorial"

    )
}
