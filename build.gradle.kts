plugins {
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
}

val loader = property("loom.platform") as String
val isFabric = loader == "fabric"
val mcVersion = property("deps.minecraft") as String

version = "${property("mod.version")}-$mcVersion"
group = "dev.imb11"

base {
    archivesName.set("${property("mod.name")}-$loader")
}

sourceSets {
    main {
        resources {
            srcDirs(file("src/main/generated"))
        }
    }
}

afterEvaluate {
    stonecutter {
        val platform = property("loom.platform") as String
        const("fabric", platform == "fabric")
        const("forge", platform == "forge")
        const("neoforge", platform == "neoforge")
    }
}

val accessWidenerFilePath = if (stonecutter.eval(stonecutter.current.version, ">=1.21.6")) {
    "accesswidener/fog_1.21.6_and_up.accesswidener"
} else {
    "accesswidener/fog_1.21.5_and_below.accesswidener"
}

val mixinConfiguration = when {
    stonecutter.eval(stonecutter.current.version, ">=1.21.6") -> "mixin/fog_1.21.6_and_up.mixins.json"
    stonecutter.eval(stonecutter.current.version, ">=1.21.2 <=1.21.5") -> "mixin/fog_1.21.2_to_1.21.5.mixins.json"
    else -> "mixin/fog_1.21.1_and_below.mixins.json"
}

loom {
    accessWidenerPath.set(rootProject.file("src/main/resources/$accessWidenerFilePath"))

    if (loader == "forge") {
        forge {
            convertAccessWideners.set(true)
            mixinConfig(mixinConfiguration)
        }
    }

    if (loader == "fabric" && mcVersion == "1.21.1") {
        runs {
            create("datagenClient") {
                client()
                name("Data Generation Client")
                vmArg("-Dfabric-api.datagen")
                vmArg("-Dfabric-api.datagen.output-dir=${rootDir.toPath().resolve("src/main/generated")}")
                vmArg("-Dfabric-api.datagen.modid=fog")

                ideConfigGenerated(false)
                runDir("build/datagen")
            }
        }
    }

    runs {
        remove(getByName("server"))
    }

    runConfigs.configureEach {
        ideConfigGenerated(true)
        runDir("../../run")
    }
}

repositories {
    maven("https://maven.neoforged.net/releases/")
    mavenCentral()
    mavenLocal()
    maven("https://mvn.devos.one/snapshots/")
    maven("https://maven.wispforest.io")
    maven("https://maven.imb11.dev/releases")
    maven {
        name = "Kotlin For Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge")
        content { includeGroup("thedarkcolour") }
    }
    maven {
        name = "Xander Maven"
        url = uri("https://maven.isxander.dev/releases")
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://maven.terraformersmc.com/releases")
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

val buildAndCollect by tasks.registering(Copy::class) {
    group = "build"
    from(tasks.remapJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    mappings(loom.layered {
        mappings("net.fabricmc:yarn:${property("deps.yarn")}:v2")
        mappings("dev.architectury:yarn-mappings-patch-neoforge:1.21+build.4")
    })

    modImplementation("dev.architectury:architectury-$loader:${property("deps.arch_api")}")

    modImplementation("dev.imb11:mru:${property("deps.mru")}") {
        isTransitive = false
    }

    modCompileOnly("maven.modrinth:polytone:${property("deps.polytone")}")
    modCompileOnly("maven.modrinth:iris:${property("deps.iris")}")

    if (isFabric) {
        println(loader)
        modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
        modImplementation("maven.modrinth:modmenu:${property("deps.mod_menu")}")

        modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}-$loader")
    } else {
        // YACL stuff
        modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}-$loader")

        if (loader == "forge") {
            "forge"("net.minecraftforge:forge:$mcVersion-${property("deps.fml")}")
            compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.0")!!)
            implementation(include("io.github.llamalad7:mixinextras-forge:0.4.0")!!)
        } else {
            "neoForge"("net.neoforged:neoforge:${property("deps.neoforge")}")
        }
    }
}

tasks.processResources {
    val props = mutableMapOf(
            "mod_version" to version,
            "target_minecraft" to project.property("mod.target"),
            "target_mru" to project.property("deps.mru") as String
    )

    if (loader == "forge" || loader == "neoforge") {
        props["target_loader"] = project.property("fml.target")
        props["loader"] = loader
        props["mandatory_inclusion_field"] = if (loader == "forge") "mandatory = true" else "type = \"required\""
    }

    props.forEach { (key, value) -> inputs.property(key, value) }

    if (loader == "fabric") {
        props["access_widener_file_path"] = accessWidenerFilePath
        props["mixin_configuration"] = mixinConfiguration
        filesMatching("fabric.mod.json") { expand(props) }
        exclude("META-INF/neoforge.mods.toml")
    }

    if (loader == "neoforge") {
        props["mixin_configuration"] = mixinConfiguration
        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
        exclude("fabric.mod.json")
    }
}

java {
    val version = if (stonecutter.eval(mcVersion, ">1.20.4")) {
        JavaVersion.VERSION_21
    } else {
        JavaVersion.VERSION_17
    }

    sourceCompatibility = version
    targetCompatibility = version
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}
