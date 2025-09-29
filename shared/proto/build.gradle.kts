plugins {
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    // gRPC 관련 의존성
    api("io.grpc:grpc-stub:1.58.0")
    api("io.grpc:grpc-protobuf:1.58.0")
    api("io.grpc:grpc-kotlin-stub:1.4.0")
    api("com.google.protobuf:protobuf-kotlin:3.24.4")
    
    // 코루틴 지원
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    
    // 필요한 경우 javax.annotation
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.4"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}

// 생성된 소스 디렉터리 설정
sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
            srcDirs("build/generated/source/proto/main/java")
            srcDirs("build/generated/source/proto/main/kotlin")
        }
    }
}