package com.codelang.upload;

import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication;


public class UploadPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        println("groovy UploadPlugin")

        if (project.plugins.hasPlugin("com.android.application")) {
            // application 不参与
            return
        }

        if (!project.plugins.hasPlugin("maven-publish")) {
            project.plugins.apply("maven-publish")
        }

        // 是否是 android module
        def isAndroid = project.plugins.hasPlugin('com.android.library')

        project.afterEvaluate {
            project.publishing.publications {
                MavenAndroid(MavenPublication) {
                    groupId = "com.github.MRwangqi"
                    artifactId = "uploadPlugin"
                    version = "1.0.0"
//                    artifact Utils.getBundleFile(realVersion, target, upInfo.bundleRelease, bundleRelease, bundleDebug)
//                    if (upInfo.sourceCode)
//                        artifact Utils.createJavaSourcesJar(target)
//                    recordDeveloper(pom)
//                    if (upInfo.hasPomDepend) {
//                        makeDependency(pom, target)
//                    }

                }
            }
            project.publishing.repositories {
                maven {
                    url project.repositories.mavenLocal().url
                }
            }
        }
    }
}
