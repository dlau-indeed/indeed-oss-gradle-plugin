package com.indeed.ossgradle.internal;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.AbstractCompile;

public class IndeedSpotlessPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.getPlugins().withType(JavaPlugin.class, p -> applySpotlessJava(project));

        project.getPlugins().withId("kotlin", p -> applySpotlessKotlin(project));
        project.getPlugins().withId("kotlin-android", p -> applySpotlessKotlin(project));
        // TODO: Cleanup
        /*
        project.getPlugins().withId("org.jetbrains.kotlin.js", p -> applySpotlessKotlin(project));
        project.getPlugins().withId( "org.jetbrains.kotlin.multiplatform", p -> applySpotlessKotlin(project));
         */
    }

    public void applySpotlessJava(final Project project) {
        project.getPlugins().apply(SpotlessPlugin.class);
        final SpotlessExtension ext = project.getExtensions().getByType(SpotlessExtension.class);
        ext.setEnforceCheck(false);
        ext.java(
                java -> {
                    java.toggleOffOn();
                    java.targetExclude(
                            project.fileTree(
                                    project.getBuildDir(), tree -> tree.include("**/*.java")));
                    java.removeUnusedImports();
                    java.trimTrailingWhitespace();
                    java.endWithNewline();
                    java.googleJavaFormat("1.7").aosp();
                    java.importOrder("", "javax", "java", "\\#");
                    java.replaceRegex(
                            "Remove extra line between javax and java",
                            "^(import javax\\..*)\n\n(import java\\..*)",
                            "$1\n$2");
                });
        if (IndeedOssLibraryRootPlugin.getCiWorkspace(project) == null) {
            project.getTasks()
                    .withType(AbstractCompile.class)
                    .configureEach(
                            compile -> {
                                compile.finalizedBy("spotlessApply");
                            });
        }
    }

    public void applySpotlessKotlin(final Project project) {
        project.getPlugins().apply(SpotlessPlugin.class);
        final SpotlessExtension ext = project.getExtensions().getByType(SpotlessExtension.class);
        ext.setEnforceCheck(false);
        ext.kotlin(
                kotlin -> {
                    kotlin.toggleOffOn();
                    kotlin.targetExclude(
                            project.fileTree(
                                    project.getBuildDir(), tree -> tree.include("**/*.kt")));
                    kotlin.target("src/**/*.kt");
                    kotlin.trimTrailingWhitespace();
                    kotlin.endWithNewline();
                    kotlin.ktlint();
                });

        if (IndeedOssLibraryRootPlugin.getCiWorkspace(project) == null) {
            project.getTasks()
                    .matching(task -> task.getName().equals("compileKotlin"))
                    .configureEach(
                            compile -> {
                                compile.finalizedBy("spotlessApply");
                            });
        }
    }
}
