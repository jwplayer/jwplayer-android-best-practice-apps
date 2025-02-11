plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
}

android {
	namespace = "com.jwplayer.customui"
	compileSdk = 34

	defaultConfig {
		minSdk = 24
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"
		applicationId = "com.jwplayer.opensourcedemo"
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
		viewBinding = true
	}
}

dependencies {
	val JWPlayerVersion = "4.19.0"
	val media3version = "1.1.1"

	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")

	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("com.google.android.material:material:1.11.0")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")

	implementation("com.jwplayer:jwplayer-core:$JWPlayerVersion")
	implementation("com.jwplayer:jwplayer-common:$JWPlayerVersion")

	implementation("androidx.media3:media3-exoplayer:$media3version")
	implementation("androidx.media3:media3-exoplayer-dash:$media3version")
	implementation("androidx.media3:media3-exoplayer-hls:$media3version")
	implementation("androidx.media3:media3-exoplayer-smoothstreaming:$media3version")
	implementation("androidx.media3:media3-ui:$media3version")

	implementation("com.squareup.picasso:picasso:2.71828")
	implementation("com.android.volley:volley:1.2.1")
	implementation("androidx.recyclerview:recyclerview:1.3.1")
}