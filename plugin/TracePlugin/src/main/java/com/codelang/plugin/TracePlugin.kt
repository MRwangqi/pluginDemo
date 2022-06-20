package com.codelang.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TracePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
//        androidComponents.onVariants { variant ->
//            variant.instrumentation.transformClassesWith(TimeCostTransform::class.java,
//                    InstrumentationScope.PROJECT) {}
//            variant.instrumentation.setAsmFramesComputationMode(
//                    FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
//            )
//        }
    }
}