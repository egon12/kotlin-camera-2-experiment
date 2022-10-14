plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "org.egon12.renderscripttutorial"
        minSdk = 23
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
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

fun createTask(name: String) {
    tasks.create < Exec > (name) {
        dependsOn("installDebug")
        commandLine(
                android.adbExecutable.path,
                "shell",
                "am",
                "start",
                "-a android.intent.action.MAIN",
                "-e fragment $name",
                "-n org.egon12.renderscripttutorial/.MainActivity",
        )
    }
}


createTask("rs_input")
createTask("hsv")
createTask("colorfilter")
createTask("composite")
createTask("convolution")
createTask("pt")
createTask("simpleBlur")
createTask("rotateY")
createTask("glsl")

tasks.create < Exec > ("stop") {
    commandLine(
            android.adbExecutable.path,
            "shell",
            "am",
            "force-stop",
            "org.egon12.renderscripttutorial"

    )
}

tasks.create < Exec > ("uninstall") {
    commandLine(
        android.adbExecutable.path,
        "uninstall",
        "org.egon12.renderscripttutorial"
    )

}
