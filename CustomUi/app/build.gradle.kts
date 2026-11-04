plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
}

// INFO: insert your license here or set JWPLAYER_LICENSE_KEY as environment variable
val licenseKey: String = System.getenv("JWPLAYER_LICENSE_KEY") ?: ""

android {
	namespace = "com.jwplayer.demo.customui"
	compileSdk = 36

	defaultConfig {
		minSdk = 24
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"
		applicationId = "com.jwplayer.demo.customui"

		buildConfigField("String", "JWPLAYER_LICENSE_KEY", "\"$licenseKey\"")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	kotlinOptions {
		jvmTarget = "1.8"
	}

	buildFeatures {
		buildConfig = true
	}
}

dependencies {
	val JWPlayerVersion = "4.28.0"

	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("com.google.android.material:material:1.11.0")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")

	implementation("com.jwplayer:jwplayer-core:$JWPlayerVersion")
	implementation("com.jwplayer:jwplayer-common:$JWPlayerVersion")

	implementation("com.squareup.picasso:picasso:2.71828")
}