plugins {
    id("java")
    id("application")
}

group = "com.test"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.2.0")
    
    // JSON processing
    implementation("org.json:json:20240303")
    
    // BCrypt for password hashing
    implementation("org.mindrot:jbcrypt:0.4")
    
    // SQLite JDBC (for ScoreRepository if needed)
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.snakegame.SnakeGame")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.snakegame.SnakeGame"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

// Task to run test game without Firebase
tasks.register<JavaExec>("runTestGame") {
    group = "application"
    description = "Run the game in test mode (without Firebase authentication)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.snakegame.TestGame")
    standardInput = System.`in`
}

// Task to run only the game (skip login)
tasks.register<JavaExec>("runGameOnly") {
    group = "application"
    description = "Run game directly with test user"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.snakegame.TestGame")
}