pluginManagement {
    repositories {
        maven(url = "https://maven.aliyun.com/repository/public") // 阿里云镜像
        maven(url = "https://maven.aliyun.com/repository/central") // Central 仓库镜像
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin") // Gradle 插件镜像
        maven(url = "https://repo.huaweicloud.com/repository/maven/")// 华为云镜像
        maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")// 腾讯云镜像
        maven(url = "https://mirrors.163.com/maven/repository/maven-public/")// 网易镜像
        maven(url = "https://maven.oscs.oschina.net/content/groups/public/")// 首都在线
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://maven.aliyun.com/repository/public") // 阿里云镜像
        maven(url = "https://maven.aliyun.com/repository/central") // Central 仓库镜像
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin") // Gradle 插件镜像
        maven(url = "https://repo.huaweicloud.com/repository/maven/")// 华为云镜像
        maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")// 腾讯云镜像
        maven(url = "https://mirrors.163.com/maven/repository/maven-public/")// 网易镜像
        maven(url = "https://maven.oscs.oschina.net/content/groups/public/")// 首都在线
        google()
        mavenCentral()
    }
}

rootProject.name = "KWSExample"
include(":app")
 