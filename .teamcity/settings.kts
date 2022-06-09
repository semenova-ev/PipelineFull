import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.sshExec
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.04"

project {

    vcsRoot(AppVcs)
    vcsRoot(HttpsGithubComSemenovaEvPipelineFullGitRefsHeadsMaster)

    buildType(TestReport)
    buildType(App)
    buildType(Deploy)
    buildType(Test)
    buildTypesOrder = arrayListOf(App, Test, TestReport, Deploy)
}

object App : BuildType({
    name = "App"

    artifactRules = "build/libs/app.jar"

    vcs {
        root(AppVcs, "-:docker")

        cleanCheckout = true
    }

    steps {
        gradle {
            tasks = "clean build"
            buildFile = ""
            gradleWrapperPath = ""
        }
    }
})

object Deploy : BuildType({
    name = "Deploy"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    steps {
        sshExec {
            commands = "echo 'Hello!!!'"
            targetUrl = "51.250.97.59"
            authMethod = uploadedKey {
                username = "temino"
                passphrase = "cksfe381a519311442e24a125e69b158dd9qxdVIanIclLyx57xQMGfew=="
                key = "id_rsaa"
            }
        }
    }

    triggers {
        vcs {
            branchFilter = ""
            watchChangesInDependencies = true
        }
    }

    dependencies {
        snapshot(TestReport) {
        }
        artifacts(App) {
            buildRule = lastSuccessful()
            artifactRules = "app.jar => build/libs/app.jar"
        }
    }
})

object Test : BuildType({
    name = "Test"

    vcs {
        root(AppVcs, "+:test1=>.")

        cleanCheckout = true
    }

    steps {
        gradle {
            tasks = "test"
            buildFile = "build.gradle"
        }
    }

    dependencies {
        snapshot(App) {
        }
    }
})

object TestReport : BuildType({
    name = "TestReport"

    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(AppVcs)

        showDependenciesChanges = true
    }

    dependencies {
        snapshot(Test) {
        }
    }
})

object AppVcs : GitVcsRoot({
    name = "AppVcs"
    url = "https://github.com/semenova-ev/Build-Chain-Project"
    branch = "master"
    checkoutPolicy = GitVcsRoot.AgentCheckoutPolicy.USE_MIRRORS
})

object HttpsGithubComSemenovaEvPipelineFullGitRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/semenova-ev/PipelineFull.git#refs/heads/master"
    url = "https://github.com/semenova-ev/PipelineFull.git"
    branch = "refs/heads/master"
    branchSpec = "refs/heads/*"
})
